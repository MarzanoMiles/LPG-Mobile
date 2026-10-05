package com.example.gastrack.ui.presentation

import android.opengl.GLES30
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import cn.easyar.CameraDevice
import cn.easyar.CameraDeviceFocusMode
import cn.easyar.CameraDevicePreference
import cn.easyar.CameraDeviceSelector
import cn.easyar.CameraDeviceType
import cn.easyar.CameraParameters
import cn.easyar.DelayedCallbackScheduler
import cn.easyar.FeedbackFrameFork
import cn.easyar.FunctorOfVoidFromTargetAndBool
import cn.easyar.Image
import cn.easyar.ImageTarget
import cn.easyar.ImageTracker
import cn.easyar.ImageTrackerResult
import cn.easyar.InputFrameFork
import cn.easyar.InputFrameThrottler
import cn.easyar.InputFrameToFeedbackFrameAdapter
import cn.easyar.InputFrameToOutputFrameAdapter
import cn.easyar.Matrix44F
import cn.easyar.OutputFrameBuffer
import cn.easyar.OutputFrameFork
import cn.easyar.OutputFrameJoin
import cn.easyar.PixelFormat
import cn.easyar.StorageType
import cn.easyar.Target
import cn.easyar.Vec2I
import java.nio.ByteBuffer
import kotlin.math.acos
import kotlin.math.sqrt

private const val MARKER_TAG = "EasyArMarker"

const val MARKER_ASSET = "gastrack_marker.jpg"  // in app/src/main/assets
const val MARKER_WIDTH_M = 0.18f                // printed width of the marker square, in meters
private const val HOLD_MS = 300L                // keep drawing this long after a lost frame to avoid flicker

data class MarkerUiState(
    val loaded: Boolean = false,
    val visible: Boolean = false,
    val everSeen: Boolean = false,
    val error: String? = null
)

/**
 * Adaptive low-pass filter for the marker pose (target -> camera, column-major 4x4).
 * Small changes (jitter) are smoothed strongly; large changes (real movement) pass through quickly.
 */
private class PoseSmoother {
    private var has = false
    private val t = FloatArray(3)
    private val q = FloatArray(4) // x y z w

    fun reset() { has = false }

    private fun alpha(delta: Float, threshold: Float, aMin: Float, aMax: Float): Float {
        val k = (delta / threshold).coerceIn(0f, 1f)
        return aMin + (aMax - aMin) * k
    }

    private fun quatFrom(m: FloatArray, out: FloatArray) {
        // columns of the rotation part, normalized (m is column-major)
        fun len(a: Float, b: Float, c: Float): Float = sqrt(a * a + b * b + c * c).let { if (it < 1e-6f) 1f else it }
        val l0 = len(m[0], m[1], m[2]); val l1 = len(m[4], m[5], m[6]); val l2 = len(m[8], m[9], m[10])
        val r00 = m[0] / l0; val r10 = m[1] / l0; val r20 = m[2] / l0
        val r01 = m[4] / l1; val r11 = m[5] / l1; val r21 = m[6] / l1
        val r02 = m[8] / l2; val r12 = m[9] / l2; val r22 = m[10] / l2
        val trace = r00 + r11 + r22
        val x: Float; val y: Float; val z: Float; val w: Float
        if (trace > 0f) {
            val s = sqrt(trace + 1f) * 2f
            w = 0.25f * s; x = (r21 - r12) / s; y = (r02 - r20) / s; z = (r10 - r01) / s
        } else if (r00 > r11 && r00 > r22) {
            val s = sqrt(1f + r00 - r11 - r22) * 2f
            w = (r21 - r12) / s; x = 0.25f * s; y = (r01 + r10) / s; z = (r02 + r20) / s
        } else if (r11 > r22) {
            val s = sqrt(1f + r11 - r00 - r22) * 2f
            w = (r02 - r20) / s; x = (r01 + r10) / s; y = 0.25f * s; z = (r12 + r21) / s
        } else {
            val s = sqrt(1f + r22 - r00 - r11) * 2f
            w = (r10 - r01) / s; x = (r02 + r20) / s; y = (r12 + r21) / s; z = 0.25f * s
        }
        val n = sqrt(x * x + y * y + z * z + w * w).let { if (it < 1e-6f) 1f else it }
        out[0] = x / n; out[1] = y / n; out[2] = z / n; out[3] = w / n
    }

    private val nq = FloatArray(4)

    /** [m] = raw pose; [out] receives the smoothed pose (both column-major 4x4). */
    fun update(m: FloatArray, out: FloatArray) {
        val nx = m[12]; val ny = m[13]; val nz = m[14]
        quatFrom(m, nq)
        if (!has) {
            t[0] = nx; t[1] = ny; t[2] = nz
            for (i in 0..3) q[i] = nq[i]
            has = true
        } else {
            val dx = nx - t[0]; val dy = ny - t[1]; val dz = nz - t[2]
            val aT = alpha(sqrt(dx * dx + dy * dy + dz * dz), 0.04f, 0.10f, 0.65f)
            t[0] += aT * dx; t[1] += aT * dy; t[2] += aT * dz

            var dot = q[0] * nq[0] + q[1] * nq[1] + q[2] * nq[2] + q[3] * nq[3]
            if (dot < 0f) { for (i in 0..3) nq[i] = -nq[i]; dot = -dot }
            val angle = 2f * acos(minOf(1f, dot))
            val aR = alpha(angle, 0.20f, 0.10f, 0.65f)
            for (i in 0..3) q[i] += aR * (nq[i] - q[i])
            val n = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]).let { if (it < 1e-6f) 1f else it }
            for (i in 0..3) q[i] /= n
        }
        val x = q[0]; val y = q[1]; val z = q[2]; val w = q[3]
        val xx = x * x; val yy = y * y; val zz = z * z
        val xy = x * y; val xz = x * z; val yz = y * z
        val wx = w * x; val wy = w * y; val wz = w * z
        out[0] = 1f - 2f * (yy + zz); out[1] = 2f * (xy + wz);       out[2] = 2f * (xz - wy);       out[3] = 0f
        out[4] = 2f * (xy - wz);       out[5] = 1f - 2f * (xx + zz); out[6] = 2f * (yz + wx);       out[7] = 0f
        out[8] = 2f * (xz + wy);       out[9] = 2f * (yz - wx);       out[10] = 1f - 2f * (xx + yy); out[11] = 0f
        out[12] = t[0]; out[13] = t[1]; out[14] = t[2]; out[15] = 1f
    }
}

/**
 * Port of EasyAR's HelloAR (image tracking) sample: camera -> ImageTracker -> pose of the marker.
 * The GLB cylinder is drawn standing on the marker (marker +Z is out of the paper, model +Y is up).
 * GL-thread methods: initialize / recreateContext / start / stop / render / dispose.
 */
class EasyArMarkerScene(
    private val analyzer: HazardAnalyzer,
    private val onState: (MarkerUiState) -> Unit
) : EasyArRenderer {

    private var scheduler: DelayedCallbackScheduler? = DelayedCallbackScheduler()
    private var camera: CameraDevice? = null
    private val trackers = ArrayList<ImageTracker>()
    private var throttler: InputFrameThrottler? = null
    private var inputFrameFork: InputFrameFork? = null
    private var join: OutputFrameJoin? = null
    private var oFrameBuffer: OutputFrameBuffer? = null
    private var i2OAdapter: InputFrameToOutputFrameAdapter? = null
    private var i2FAdapter: InputFrameToFeedbackFrameAdapter? = null
    private var outputFrameFork: OutputFrameFork? = null
    private var feedbackFrameFork: FeedbackFrameFork? = null
    private var bgRenderer: BGRenderer? = null
    private val glb = GlbGlRenderer()

    private var previousInputFrameIndex = -1
    private var imageBytes: ByteArray? = null

    @Volatile private var modelData: GlbData? = null
    @Volatile private var modelDirty = false
    @Volatile private var yawDeg = 0f
    @Volatile private var userScale = 1f
    @Volatile private var resetRequested = false

    private var targetLoaded = false
    private var loadError: String? = null
    private var everSeen = false
    private var lastSeenMs = 0L
    private var lastPose: FloatArray? = null
    private var lastPublished = MarkerUiState()
    private var lastTargetStatus = -1

    private val smoother = PoseSmoother()
    private val smoothed = FloatArray(16)
    private val standUp = FloatArray(16)
    private val yawM = FloatArray(16)
    private val tmp1 = FloatArray(16)
    private val tmp2 = FloatArray(16)
    private val mvp = FloatArray(16)

    // ---- UI-thread API ----
    fun setModel(data: GlbData) { modelData = data; modelDirty = true }
    override fun requestPlacement(nx: Float, ny: Float) { /* marker mode places automatically */ }
    override fun rotateBy(deg: Float) { yawDeg = (yawDeg + deg) % 360f }
    override fun scaleBy(f: Float) { userScale = (userScale * f).coerceIn(0.3f, 3f) }

    /** Restores spin, size and smoothing. */
    fun resetModel() {
        yawDeg = 0f
        userScale = 1f
        resetRequested = true
    }

    // ---- GL-thread API ----
    override fun recreateContext() {
        bgRenderer?.dispose()
        bgRenderer = null
        glb.forget()
        previousInputFrameIndex = -1
        bgRenderer = BGRenderer()
        glb.init()
        modelDirty = modelData != null
    }

    private fun loadMarker(tracker: ImageTracker) {
        val sch = scheduler ?: return
        val target = ImageTarget.createFromImageFile(MARKER_ASSET, StorageType.Assets, "gastrack_marker", "", "", MARKER_WIDTH_M)
        if (target == null) {
            Log.e(MARKER_TAG, "target create failed or key is not correct")
            loadError = "Marker image could not be loaded (check assets/$MARKER_ASSET and the license key)"
            return
        }
        tracker.loadTarget(target, sch, object : FunctorOfVoidFromTargetAndBool {
            override fun invoke(target: Target, status: Boolean) {
                Log.i(MARKER_TAG, "load target ($status): ${target.name()} (${target.runtimeID()})")
                targetLoaded = status
                if (!status) loadError = "EasyAR could not load the marker image"
            }
        })
        target.dispose()
    }

    override fun initialize() {
        recreateContext()

        val cam = CameraDeviceSelector.createCameraDevice(CameraDevicePreference.PreferObjectSensing)
        camera = cam
        val thr = InputFrameThrottler.create()
        val inFork = InputFrameFork.create(2)
        val j = OutputFrameJoin.create(2)
        val oBuf = OutputFrameBuffer.create()
        val i2o = InputFrameToOutputFrameAdapter.create()
        val i2f = InputFrameToFeedbackFrameAdapter.create()
        val outFork = OutputFrameFork.create(2)
        throttler = thr; inputFrameFork = inFork; join = j; oFrameBuffer = oBuf
        i2OAdapter = i2o; i2FAdapter = i2f; outputFrameFork = outFork

        val opened = cam.openWithPreferredType(CameraDeviceType.Back)
        cam.setSize(Vec2I(1280, 960))
        cam.setFocusMode(CameraDeviceFocusMode.Continousauto)
        if (!opened) {
            Log.e(MARKER_TAG, "camera open failed")
            loadError = "The camera could not be opened"
            return
        }

        val tracker = ImageTracker.create()
        loadMarker(tracker)
        trackers.add(tracker)

        val fbFork = FeedbackFrameFork.create(trackers.size)
        feedbackFrameFork = fbFork

        cam.inputFrameSource().connect(thr.input())
        thr.output().connect(inFork.input())
        inFork.output(0).connect(i2o.input())
        i2o.output().connect(j.input(0))

        inFork.output(1).connect(i2f.input())
        i2f.output().connect(fbFork.input())
        var k = 0
        var trackerBufferRequirement = 0
        for (t in trackers) {
            fbFork.output(k).connect(t.feedbackFrameSink())
            t.outputFrameSource().connect(j.input(k + 1))
            trackerBufferRequirement += t.bufferRequirement()
            k++
        }

        j.output().connect(outFork.input())
        outFork.output(0).connect(oBuf.input())
        outFork.output(1).connect(i2f.sideInput())
        oBuf.signalOutput().connect(thr.signalInput())

        // CameraDevice and rendering each require an additional buffer
        cam.setBufferCapacity(
            thr.bufferRequirement() + i2f.bufferRequirement() + oBuf.bufferRequirement() + trackerBufferRequirement + 2
        )
    }

    override fun start(): Boolean {
        var status = camera?.start() ?: false
        for (t in trackers) status = status and t.start()
        if (!status) loadError = loadError ?: "EasyAR image tracking could not start"
        return status
    }

    override fun stop() {
        camera?.stop()
        for (t in trackers) t.stop()
    }

    override fun dispose() {
        for (t in trackers) t.dispose()
        trackers.clear()
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

    private fun publish(visible: Boolean) {
        val s = MarkerUiState(targetLoaded, visible, everSeen, loadError)
        if (s != lastPublished) {
            lastPublished = s
            onState(s)
        }
    }

    private fun drawModel(proj: FloatArray, pose: FloatArray) {
        Matrix.setIdentityM(standUp, 0)
        Matrix.rotateM(standUp, 0, 90f, 1f, 0f, 0f)      // model +Y (up) -> marker +Z (out of the paper)
        Matrix.setRotateM(yawM, 0, yawDeg, 0f, 1f, 0f)   // user spin around the cylinder's own axis
        Matrix.multiplyMM(tmp1, 0, standUp, 0, yawM, 0)
        val s = userScale
        Matrix.scaleM(tmp1, 0, s, s, s)                  // user pinch size (Reset restores 1.0)
        Matrix.multiplyMM(tmp2, 0, pose, 0, tmp1, 0)     // pose is target -> camera
        Matrix.multiplyMM(mvp, 0, proj, 0, tmp2, 0)
        glb.draw(mvp, yawM)
    }

    override fun render(width: Int, height: Int, screenRotation: Int) {
        while (scheduler?.runOne() == true) { }

        GLES30.glViewport(0, 0, width, height)
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        if (modelDirty) {
            modelData?.let { glb.upload(it) }
            modelDirty = false
        }
        if (resetRequested) {
            resetRequested = false
            smoother.reset()
            lastPose = null
        }

        val oframe = oFrameBuffer?.peek() ?: run { publish(false); return }
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

            val proj = glMatrix(camParams.projection(0.01f, 1000f, aspect, screenRotation, true, false))
            var tracked = false
            for (r in oframe.results()) {
                val result = r as? ImageTrackerResult ?: continue
                for (ti in result.targetInstances()) {
                    val st = ti.status()
                    if (st != lastTargetStatus) {
                        lastTargetStatus = st
                        Log.i(MARKER_TAG, "target status=$st (EasyAR docs: 0 Unknown, 1 Undefined, 2 Detected, 3 Tracked)")
                    }
                    smoother.update(glMatrix(ti.pose()), smoothed)
                    tracked = true
                    lastPose = smoothed.copyOf()
                    drawModel(proj, smoothed)
                }
                result.dispose()
            }

            val now = SystemClock.elapsedRealtime()
            if (tracked) {
                everSeen = true
                lastSeenMs = now
            } else {
                val held = lastPose
                if (held != null && now - lastSeenMs < HOLD_MS) {
                    drawModel(proj, held)
                } else if (held != null) {
                    smoother.reset() // marker was lost: the next detection starts fresh instead of gliding
                }
            }
            publish(tracked || (everSeen && now - lastSeenMs < HOLD_MS))
        } finally {
            iframe.dispose()
            oframe.dispose()
            camParams.dispose()
            image.dispose()
        }
    }

    // ---- hazard detection from the camera frames ----
    private fun maybeAnalyze(image: Image, camParams: CameraParameters, screenRotation: Int) {
        if (!analyzer.canSubmit()) return
        val bytes = imageBytes ?: return
        val format = image.format()
        val w = image.width()
        val h = image.height()
        val pw = image.pixelWidth()
        val ph = image.pixelHeight()
        val rotation = camParams.imageOrientation(screenRotation)
        val copy = bytes.copyOf()
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
                Log.w(MARKER_TAG, "Hazard detection skipped: unsupported pixel format $format")
                null
            }
        }
    }
}