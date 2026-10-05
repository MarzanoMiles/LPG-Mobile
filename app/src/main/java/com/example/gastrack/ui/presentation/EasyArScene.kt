package com.example.gastrack.ui.presentation

import android.opengl.GLES30
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import cn.easyar.CameraParameters
import cn.easyar.DelayedCallbackScheduler
import cn.easyar.Image
import cn.easyar.InputFrameToOutputFrameAdapter
import cn.easyar.Matrix44F
import cn.easyar.MotionTrackerCameraDevice
import cn.easyar.MotionTrackerCameraDeviceFPS
import cn.easyar.MotionTrackerCameraDeviceFocusMode
import cn.easyar.MotionTrackerCameraDeviceResolution
import cn.easyar.MotionTrackingStatus
import cn.easyar.OutputFrameBuffer
import cn.easyar.PixelFormat
import cn.easyar.Vec2F
import java.nio.ByteBuffer

private const val SCENE_TAG = "EasyArScene"

/** Pinch-to-resize breaks real-world size. Set to true if you want to allow it. */
const val ALLOW_PINCH_RESIZE = true

private const val FALLBACK_CAMERA_HEIGHT_M = 1.3f // only used until EasyAR has detected a real floor
private const val TAP_SEARCH_MS = 2500L           // how long a tap keeps searching for the floor
private const val FLOOR_RECENT_MS = 1500L         // a floor hit this recent counts as "floor found"

data class EasyArUiState(
    val tracking: Boolean = false,
    val weak: Boolean = false,
    val placed: Boolean = false,
    val noSurface: Boolean = false,
    val floorReady: Boolean = false,
    val searching: Boolean = false,
    val guessed: Boolean = false,
    val error: String? = null
)

/**
 * Port of EasyAR's HelloARMotionTracking render loop (EasyAR motion tracker only, no ARCore),
 * plus: floor placement via hit test, GLB drawing, and hand-off of camera frames to the hazard detector.
 * initialize / start / stop / render / dispose / recreateContext run on the GL thread.
 * The other methods are called from the UI thread.
 */
class EasyArScene(
    private val analyzer: HazardAnalyzer,
    private val onState: (EasyArUiState) -> Unit
) {
    private var scheduler: DelayedCallbackScheduler? = null
    private var camera: MotionTrackerCameraDevice? = null
    private var adapter: InputFrameToOutputFrameAdapter? = null
    private var oFrameBuffer: OutputFrameBuffer? = null
    private var bgRenderer: BGRenderer? = null
    private val glb = GlbGlRenderer()
    private var previousInputFrameIndex = -1
    private var imageBytes: ByteArray? = null

    @Volatile private var modelData: GlbData? = null
    @Volatile private var modelDirty = false

    private var placed = false
    private var guessed = false
    private var noSurface = false
    private var floorReady = false
    private var floorY = Float.NaN          // world Y of the last detected horizontal plane
    private var lastFloorSeenMs = 0L
    private var probeIndex = 0
    private var px = 0f
    private var py = 0f
    private var pz = 0f
    @Volatile private var pendingTap: FloatArray? = null
    @Volatile private var tapStartedMs = 0L
    @Volatile private var estimatedRequested = false
    @Volatile private var clearRequested = false
    @Volatile private var yawDeg = 0f
    @Volatile private var userScale = 1f
    private var lastPublished = EasyArUiState()
    private var lastStatus = -1
    private var frameCount = 0

    private val model = FloatArray(16)
    private val tmp = FloatArray(16)
    private val mvp = FloatArray(16)

    private val probePoints = arrayOf(
        floatArrayOf(0.5f, 0.60f), floatArrayOf(0.5f, 0.75f),
        floatArrayOf(0.3f, 0.70f), floatArrayOf(0.7f, 0.70f)
    )

    // ---- UI-thread API ----
    fun setModel(data: GlbData) { modelData = data; modelDirty = true }
    fun requestPlacement(nx: Float, ny: Float) {
        tapStartedMs = SystemClock.elapsedRealtime()
        pendingTap = floatArrayOf(nx, ny)
    }
    fun requestEstimatedPlacement() { estimatedRequested = true }
    fun rotateBy(deg: Float) { yawDeg = (yawDeg + deg) % 360f }
    fun scaleBy(f: Float) { userScale = (userScale * f).coerceIn(0.3f, 3f) }
    fun clearPlacement() { clearRequested = true }

    // ---- GL-thread API ----
    fun recreateContext() {
        bgRenderer?.dispose()
        bgRenderer = null
        glb.forget()
        previousInputFrameIndex = -1
        bgRenderer = BGRenderer()
        glb.init()
        modelDirty = modelData != null
    }

    fun initialize() {
        scheduler = DelayedCallbackScheduler()
        recreateContext()
        val fb = OutputFrameBuffer.create()
        val ad = InputFrameToOutputFrameAdapter.create()
        val cam = MotionTrackerCameraDevice()
        oFrameBuffer = fb
        adapter = ad
        camera = cam
        cam.inputFrameSource().connect(ad.input())
        ad.output().connect(fb.input())
        cam.setFrameRateType(MotionTrackerCameraDeviceFPS.Camera_FPS_60)
        cam.setFocusMode(MotionTrackerCameraDeviceFocusMode.Continousauto)
        cam.setFrameResolutionType(MotionTrackerCameraDeviceResolution.Resolution_1280)
        // CameraDevice and rendering each require an additional buffer
        cam.setBufferCapacity(fb.bufferRequirement() + 2)
    }

    fun start(): Boolean {
        val ok = camera?.start() ?: false
        if (!ok) onState(EasyArUiState(error = "EasyAR camera could not start"))
        return ok
    }

    fun stop() {
        camera?.stop()
    }

    fun dispose() {
        bgRenderer?.dispose()
        bgRenderer = null
        glb.dispose()
        camera?.dispose()
        camera = null
        scheduler?.dispose()
        scheduler = null
    }

    private fun glMatrix(m: Matrix44F): FloatArray {
        val d = m.data
        return floatArrayOf(d[0], d[4], d[8], d[12], d[1], d[5], d[9], d[13], d[2], d[6], d[10], d[14], d[3], d[7], d[11], d[15])
    }

    private fun publish(tracking: Boolean, weak: Boolean) {
        val s = EasyArUiState(
            tracking = tracking,
            weak = weak,
            placed = placed,
            noSurface = noSurface,
            floorReady = floorReady,
            searching = pendingTap != null,
            guessed = guessed,
            error = null
        )
        if (s != lastPublished) {
            lastPublished = s
            onState(s)
        }
    }

    /** Real horizontal-plane hit at a screen point (0..1). Returns the world position or null. */
    private fun hitFloor(
        camParams: CameraParameters, aspect: Float, screenRotation: Int, nx: Float, ny: Float
    ): FloatArray? {
        val cam = camera ?: return null
        val imagePoint = camParams.imageCoordinatesFromScreenCoordinates(
            aspect, screenRotation, true, false, Vec2F(nx, ny)
        )
        val hits = cam.hitTestAgainstHorizontalPlane(imagePoint)
        return if (hits.isEmpty()) null else hits[0].data
    }

    /**
     * Intersects the view ray through a screen point with the horizontal plane y = planeY.
     * Needs the camera to point downward at that spot. Assumes world Y is up.
     */
    private fun rayPlanePoint(nx: Float, ny: Float, proj: FloatArray, camToWorld: FloatArray, planeY: Float): FloatArray? {
        val invProj = FloatArray(16)
        if (!Matrix.invertM(invProj, 0, proj, 0)) return null
        val ndc = floatArrayOf(2f * nx - 1f, 1f - 2f * ny, 1f, 1f)
        val c = FloatArray(4)
        Matrix.multiplyMV(c, 0, invProj, 0, ndc, 0)
        if (c[3] == 0f) return null
        val dirCam = floatArrayOf(c[0] / c[3], c[1] / c[3], c[2] / c[3], 0f)
        val dirWorld = FloatArray(4)
        Matrix.multiplyMV(dirWorld, 0, camToWorld, 0, dirCam, 0)
        if (dirWorld[1] >= -1e-3f) return null // not looking downward
        val t = (planeY - camToWorld[13]) / dirWorld[1]
        if (t <= 0f) return null
        return floatArrayOf(
            camToWorld[12] + dirWorld[0] * t,
            planeY,
            camToWorld[14] + dirWorld[2] * t
        )
    }

    private fun place(p: FloatArray, wasGuessed: Boolean) {
        px = p[0]; py = p[1]; pz = p[2]
        placed = true
        guessed = wasGuessed
        noSurface = false
        pendingTap = null
    }

    fun render(width: Int, height: Int, screenRotation: Int) {
        while (scheduler?.runOne() == true) { }

        GLES30.glViewport(0, 0, width, height)
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        if (modelDirty) {
            modelData?.let { glb.upload(it) }
            modelDirty = false
        }
        if (clearRequested) {
            clearRequested = false
            placed = false
            guessed = false
            noSurface = false
            pendingTap = null
            userScale = 1f
            yawDeg = 0f
        }

        val oframe = oFrameBuffer?.peek() ?: return
        val iframe = oframe.inputFrame()
        if (iframe == null) { oframe.dispose(); return }
        val camParams = iframe.cameraParameters()
        if (camParams == null) { oframe.dispose(); iframe.dispose(); return }
        val aspect = width.toFloat() / height.toFloat()
        val imageProjection = camParams.imageProjection(aspect, screenRotation, true, false)
        val image = iframe.image()

        try {
            if (iframe.index() != previousInputFrameIndex) {
                val buffer = image.buffer()
                try {
                    var bytes = imageBytes
                    if (bytes == null || bytes.size != buffer.size()) {
                        bytes = ByteArray(buffer.size())
                        imageBytes = bytes
                    }
                    buffer.copyToByteArray(bytes)
                    bgRenderer?.upload(
                        image.format(), image.width(), image.height(),
                        image.pixelWidth(), image.pixelHeight(), ByteBuffer.wrap(bytes)
                    )
                } finally {
                    buffer.dispose()
                }
                previousInputFrameIndex = iframe.index()
                maybeAnalyze(image, camParams, screenRotation)
            }
            bgRenderer?.render(imageProjection)

            val status = iframe.trackingStatus()
            val tracking = status != MotionTrackingStatus.NotTracking
            val weak = status == MotionTrackingStatus.Limited
            if (status != lastStatus) {
                lastStatus = status
                Log.i(SCENE_TAG, "trackingStatus=$status (0=NotTracking, 1=Limited, 2=Tracking)")
            }
            frameCount++
            val now = SystemClock.elapsedRealtime()
            val proj = glMatrix(camParams.projection(0.01f, 500f, aspect, screenRotation, true, false))
            val camToWorld = glMatrix(iframe.cameraTransform())

            // 1) Floor probe: remember the real floor height while the user scans
            if (!tracking) {
                floorReady = false
            } else if (!placed) {
                if (frameCount % 6 == 0) {
                    val pt = probePoints[probeIndex % probePoints.size]
                    probeIndex++
                    val hit = hitFloor(camParams, aspect, screenRotation, pt[0], pt[1])
                    if (hit != null) {
                        floorY = hit[1]
                        lastFloorSeenMs = now
                    }
                }
                floorReady = now - lastFloorSeenMs < FLOOR_RECENT_MS
            }

            // 2) Tap: real plane hit, retried for TAP_SEARCH_MS; then the remembered floor height
            val tap = pendingTap
            if (tap != null) {
                if (tracking && frameCount % 3 == 0) {
                    val hit = hitFloor(camParams, aspect, screenRotation, tap[0], tap[1])
                    if (hit != null) {
                        floorY = hit[1]
                        lastFloorSeenMs = now
                        place(hit, false)
                        Log.i(SCENE_TAG, "placed on detected floor at (${hit[0]}, ${hit[1]}, ${hit[2]})")
                    }
                }
                if (pendingTap != null && now - tapStartedMs > TAP_SEARCH_MS) {
                    pendingTap = null
                    val p = if (tracking && !floorY.isNaN()) rayPlanePoint(tap[0], tap[1], proj, camToWorld, floorY) else null
                    if (p != null) {
                        place(p, false)
                        Log.i(SCENE_TAG, "placed using remembered floor height y=$floorY")
                    } else {
                        noSurface = true
                        Log.i(SCENE_TAG, "tap gave up: no floor found (tracking=$tracking, knownFloorY=$floorY)")
                    }
                }
            }

            // 3) "Estimated floor" button
            if (estimatedRequested) {
                estimatedRequested = false
                if (tracking) {
                    val known = !floorY.isNaN()
                    val planeY = if (known) floorY else camToWorld[13] - FALLBACK_CAMERA_HEIGHT_M
                    val p = rayPlanePoint(0.5f, 0.7f, proj, camToWorld, planeY)
                    if (p != null) {
                        place(p, !known)
                        Log.i(SCENE_TAG, "estimated placement: floorHeightKnown=$known y=$planeY")
                    } else {
                        noSurface = true
                        Log.i(SCENE_TAG, "estimated placement failed (camera must point downward)")
                    }
                } else {
                    noSurface = true
                }
            }

            if (tracking && placed) {
                val view = FloatArray(16)
                if (Matrix.invertM(view, 0, camToWorld, 0)) {
                    Matrix.setIdentityM(model, 0)
                    Matrix.translateM(model, 0, px, py, pz)
                    Matrix.rotateM(model, 0, yawDeg, 0f, 1f, 0f)
                    val s = userScale
                    Matrix.scaleM(model, 0, s, s, s)
                    Matrix.multiplyMM(tmp, 0, proj, 0, view, 0)
                    Matrix.multiplyMM(mvp, 0, tmp, 0, model, 0)
                    glb.draw(mvp, model)
                }
            }
            publish(tracking, weak)
        } finally {
            iframe.dispose()
            oframe.dispose()
            camParams.dispose()
            image.dispose()
        }
    }

    // ---- hazard detection from EasyAR frames ----
    private fun maybeAnalyze(image: Image, camParams: CameraParameters, screenRotation: Int) {
        if (!analyzer.canSubmit()) return
        val bytes = imageBytes ?: return
        val format = image.format()
        val w = image.width()
        val h = image.height()
        val pw = image.pixelWidth()
        val ph = image.pixelHeight()
        val rotation = camParams.imageOrientation(screenRotation) // clockwise degrees to align with the screen
        val copy = bytes.copyOf() // the render loop reuses imageBytes, so hand over a copy
        analyzer.submit { d -> classifyFrame(d, format, copy, w, h, pw, ph, rotation) }
    }

    private fun classifyFrame(
        d: HazardDetector, format: Int, bytes: ByteArray,
        w: Int, h: Int, pw: Int, ph: Int, rotation: Int
    ): Prediction? {
        val plane = pw * ph
        fun slice(offset: Int): ByteBuffer = ByteBuffer.wrap(bytes, offset, bytes.size - offset).slice()
        return when (format) {
            PixelFormat.YUV_NV21 -> d.infer(slice(0), slice(plane + 1), slice(plane), pw, 1, pw, 2, w, h, rotation)
            PixelFormat.YUV_NV12 -> d.infer(slice(0), slice(plane), slice(plane + 1), pw, 1, pw, 2, w, h, rotation)
            PixelFormat.YUV_I420 -> d.infer(slice(0), slice(plane), slice(plane * 5 / 4), pw, 1, pw / 2, 1, w, h, rotation)
            PixelFormat.YUV_YV12 -> d.infer(slice(0), slice(plane * 5 / 4), slice(plane), pw, 1, pw / 2, 1, w, h, rotation)
            else -> {
                Log.w(SCENE_TAG, "Hazard detection skipped: unsupported pixel format $format")
                null
            }
        }
    }
}