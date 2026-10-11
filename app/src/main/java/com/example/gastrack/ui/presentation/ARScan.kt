package com.example.gastrack.ui.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.opengl.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.gastrack.R
import com.example.gastrack.ui.theme.ButtonOrange
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.GasTrackRed
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val AR_TAG = "ARScan"

/** Depth mode makes taps hit real surfaces much sooner (the Infinix Note 30 supports it). */
private const val USE_DEPTH = true

/** If the tank looks too dark on some phone, try Config.LightEstimationMode.DISABLED here. */
private val ARCORE_LIGHT_MODE = Config.LightEstimationMode.ENVIRONMENTAL_HDR

private const val MIN_USER_SCALE = 0.5f
private const val MAX_USER_SCALE = 2.0f
private const val ASSUMED_PHONE_HEIGHT_M = 1.3f // only used until ARCore has found a real floor

@Composable
fun ARScanScreen(
    tankType: GasTankType,
    onScanComplete: () -> Unit,
    onArCoreFailed: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            ArCoreGate(tankType = tankType, onScanComplete = onScanComplete, onArCoreFailed = onArCoreFailed)
        } else {
            CenterMessage(
                message = "Camera permission is required\nto use AR hazard scanning.",
                buttonText = "Grant Permission",
                onButtonClick = { launcher.launch(Manifest.permission.CAMERA) }
            )
        }
    }
}

/** Checks ARCore availability before creating the AR session so we never crash on unsupported devices. */
@Composable
private fun ArCoreGate(tankType: GasTankType, onScanComplete: () -> Unit, onArCoreFailed: () -> Unit) {
    val context = LocalContext.current
    var recheck by remember { mutableIntStateOf(0) }
    var availability by remember { mutableStateOf<ArCoreApk.Availability?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { recheck++ }

    LaunchedEffect(recheck) {
        val api = ArCoreApk.getInstance()
        var a = api.checkAvailability(context)
        while (a.isTransient) {
            delay(200)
            a = api.checkAvailability(context)
        }
        availability = a
    }

    when (val a = availability) {
        null -> CenterMessage("Checking AR support…")
        ArCoreApk.Availability.SUPPORTED_INSTALLED ->
            ArPlacementContent(tankType = tankType, onScanComplete = onScanComplete, onArCoreFailed = onArCoreFailed)
        else -> if (a.isSupported) {
            CenterMessage(
                message = "Google Play Services for AR (ARCore)\nneeds to be installed or updated.",
                buttonText = "Install ARCore",
                onButtonClick = {
                    try {
                        context.findActivity()?.let { ArCoreApk.getInstance().requestInstall(it, true) }
                    } catch (_: Exception) { }
                }
            )
        } else {
            CenterMessage("This device does not support AR (ARCore).\nReason: ${a.name}")
        }
    }
}

private class ArHolder {
    var frame: Frame? = null
    var session: Session? = null
    var anchorNode: AnchorNode? = null
    var modelHolder: Node? = null   // parent of the model: carries the user's spin and size
    var frameCount = 0
    var floorY: Float? = null       // world height of the biggest detected floor
}

@Composable
private fun ArPlacementContent(tankType: GasTankType, onScanComplete: () -> Unit, onArCoreFailed: () -> Unit) {
    val context = LocalContext.current

    // --- AR scene ---
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val cameraNode = rememberARCameraNode(engine)
    val collisionSystem = rememberCollisionSystem(view)
    val childNodes = rememberNodes()
    val holder = remember { ArHolder() }

    // --- UI state ---
    var hasPlane by remember { mutableStateOf(false) }
    var isPlaced by remember { mutableStateOf(false) }
    var provisional by remember { mutableStateOf(false) } // placed without a real surface, will snap to the floor
    var placementError by remember { mutableStateOf<String?>(null) }
    var tapHint by remember { mutableStateOf<String?>(null) }
    var yaw by remember { mutableFloatStateOf(0f) }
    var userScale by remember { mutableFloatStateOf(1f) }
    var gestureTick by remember { mutableIntStateOf(0) }
    var gestureActive by remember { mutableStateOf(false) }
    var pinchedOnce by remember { mutableStateOf(false) }
    var coachVisible by remember { mutableStateOf(true) }
    var scanSeconds by remember { mutableIntStateOf(0) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    // --- Hazard detection ---
    val analyzer = remember { HazardAnalyzer(context) }
    DisposableEffect(analyzer) { onDispose { analyzer.close() } }
    val hazardUi by analyzer.state.collectAsState()

    // Scan timer + pinch coach + gesture highlight
    LaunchedEffect(isPlaced, hasPlane) {
        scanSeconds = 0
        while (!isPlaced && !hasPlane) {
            delay(1000)
            scanSeconds++
        }
    }
    LaunchedEffect(isPlaced) {
        coachVisible = true
        if (isPlaced) {
            delay(6000)
            coachVisible = false
        }
    }
    LaunchedEffect(gestureTick) {
        if (gestureTick > 0) {
            gestureActive = true
            delay(700)
            gestureActive = false
        }
    }

    // --- Helpers (declared before use) ---
    fun applyUserTransform() {
        holder.modelHolder?.let {
            it.rotation = Rotation(0f, yaw, 0f)
            it.scale = Scale(userScale, userScale, userScale)
        }
    }

    fun placeAnchor(anchor: Anchor, wasProvisional: Boolean) {
        try {
            val anchorNode = AnchorNode(engine = engine, anchor = anchor)
            val modelNode = ModelNode(
                modelInstance = modelLoader.createModelInstance(assetFileLocation = tankType.assetPath),
                scaleToUnits = tankType.realHeightMeters,
                centerOrigin = Position(x = 0f, y = -1f, z = 0f)
            )
            val modelHolder = Node(engine = engine)
            modelHolder.addChildNode(modelNode)
            anchorNode.addChildNode(modelHolder)
            holder.anchorNode = anchorNode
            holder.modelHolder = modelHolder
            childNodes += anchorNode
            isPlaced = true
            provisional = wasProvisional
            placementError = null
            tapHint = null
            applyUserTransform()
            Log.i(AR_TAG, "placed ${tankType.label} (provisional=$wasProvisional)")
        } catch (t: Throwable) {
            Log.e(AR_TAG, "Model placement failed", t)
            try { anchor.detach() } catch (_: Throwable) { }
            placementError = "Could not load the ${tankType.label} model."
        }
    }

    fun clearPlacement() {
        childNodes.toList().forEach { n ->
            (n as? AnchorNode)?.anchor?.detach()
            n.destroy()
        }
        childNodes.clear()
        holder.anchorNode = null
        holder.modelHolder = null
        isPlaced = false
        provisional = false
        yaw = 0f
        userScale = 1f
        tapHint = null
    }

    fun placeAt(x: Float, y: Float) {
        if (isPlaced) return
        val frame = holder.frame
        val session = holder.session
        if (frame == null || session == null) {
            tapHint = "AR is still starting. Try again in a moment"
            return
        }
        // 1) a real surface under the tap
        val hit = pickHit(frame, x, y)
        val hitAnchor = try { hit?.createAnchor() } catch (t: Throwable) { null }
        if (hitAnchor != null) {
            placeAnchor(hitAnchor, false)
            return
        }
        // 2) no surface yet: a free anchor ahead of the camera. It snaps to the floor once one is found.
        val pose = freePose(frame, x, y, viewSize.width, viewSize.height, holder.floorY)
        val freeAnchor = try { if (pose != null) session.createAnchor(pose) else null } catch (t: Throwable) { null }
        if (freeAnchor != null) {
            placeAnchor(freeAnchor, true)
        } else {
            tapHint = "Tracking hasn't started yet. Move the phone slowly and try again"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            cameraNode = cameraNode,
            planeRenderer = !isPlaced,
            sessionConfiguration = { session, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.lightEstimationMode = ARCORE_LIGHT_MODE
                config.focusMode = Config.FocusMode.AUTO
                if (USE_DEPTH && session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    config.depthMode = Config.DepthMode.AUTOMATIC
                }
            },
            onSessionFailed = { e ->
                Log.e(AR_TAG, "ARCore session failed", e)
                onArCoreFailed()
            },
            onSessionUpdated = { session, updatedFrame ->
                holder.frame = updatedFrame
                holder.session = session
                holder.frameCount++

                if (holder.frameCount % 10 == 0) {
                    val floor = largestFloorPlane(session)
                    hasPlane = floor != null
                    holder.floorY = floor?.centerPose?.ty()

                    // Snap a "provisional" tank onto the floor once one is detected
                    val node = holder.anchorNode
                    val modelHolder = holder.modelHolder
                    if (provisional && floor != null && node != null && modelHolder != null) {
                        val old = node.anchor
                        if (old.trackingState == TrackingState.TRACKING) {
                            val p = old.pose
                            val floorY = floor.centerPose.ty()
                            if (abs(floorY - p.ty()) < 0.8f) {
                                try {
                                    val newAnchor = session.createAnchor(
                                        Pose(floatArrayOf(p.tx(), floorY, p.tz()), floatArrayOf(0f, 0f, 0f, 1f))
                                    )
                                    val newNode = AnchorNode(engine = engine, anchor = newAnchor)
                                    node.removeChildNode(modelHolder)
                                    newNode.addChildNode(modelHolder)
                                    childNodes.remove(node)
                                    old.detach()
                                    node.destroy()
                                    childNodes += newNode
                                    holder.anchorNode = newNode
                                    provisional = false
                                    applyUserTransform()
                                    Log.i(AR_TAG, "snapped the tank onto the detected floor (y=$floorY)")
                                } catch (t: Throwable) {
                                    Log.w(AR_TAG, "snap to floor failed", t)
                                }
                            }
                        }
                    }
                }
                analyzer.analyze(updatedFrame) // throttled, off the UI thread
            }
        )

        // Gesture layer: tap to place, drag/twist to rotate, pinch to resize
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { viewSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { o -> if (!isPlaced) placeAt(o.x, o.y) })
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rot ->
                        if (!isPlaced) return@detectTransformGestures
                        if (zoom != 1f) {
                            val step = zoom.coerceIn(0.9f, 1.1f)
                            userScale = (userScale * step).coerceIn(MIN_USER_SCALE, MAX_USER_SCALE)
                            pinchedOnce = true
                        }
                        if (rot != 0f) yaw = (yaw - rot) % 360f            // two-finger twist
                        if (zoom == 1f && rot == 0f) yaw = (yaw + pan.x * 0.4f) % 360f // one-finger drag
                        applyUserTransform()
                        gestureTick++
                    }
                }
        )

        if (hazardUi.state == HazardState.POTENTIAL_HAZARD) {
            val pulse = rememberInfiniteTransition(label = "pulse")
            val alpha by pulse.animateFloat(
                initialValue = 0.25f, targetValue = 0.95f,
                animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                label = "pulseAlpha"
            )
            Box(Modifier.fillMaxSize().border(8.dp, Color(0xFFFF3B30).copy(alpha = alpha)))
        }

        // Pinch cue right after placement
        if (isPlaced && !pinchedOnce && coachVisible) {
            PinchCoach(Modifier.align(Alignment.Center))
        }

        // Live size / rotation badge (tap to reset the size)
        if (isPlaced) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .clickable {
                        userScale = 1f
                        applyUserTransform()
                    },
                shape = RoundedCornerShape(14.dp),
                color = if (gestureActive) GasTrackBlue.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.55f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Size ${(userScale * 100).roundToInt()}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Turn ${((yaw % 360f) + 360f).roundToInt() % 360}°", color = Color.White, fontSize = 12.sp)
                    Text("tap = reset size", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                }
            }
        }

        // --- Top: header + hazard banner ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.favicon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = GasTrackBlue, fontWeight = FontWeight.Bold)) { append("Gas") }
                        withStyle(SpanStyle(color = GasTrackRed, fontWeight = FontWeight.Bold)) { append("Track") }
                    },
                    fontSize = 18.sp
                )
                Spacer(Modifier.width(12.dp))
                Text("AR Scan · ${tankType.label}", color = Color.White, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
            HazardBanner(hazardUi)
        }

        // --- Bottom: hint, actions, disclaimer ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val hint = placementError ?: tapHint ?: when {
                isPlaced && provisional -> "Placed without a floor. Move slowly and it will snap to the floor when found"
                isPlaced -> "Drag or twist to rotate · Pinch to resize"
                hasPlane -> "Floor found. Tap to place, or press Place here"
                scanSeconds >= 3 -> "Still scanning… tap anywhere to place now, or keep sweeping slowly"
                else -> "Move your phone slowly to scan the floor, or tap anywhere to place now"
            }
            Surface(shape = RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = 0.55f)) {
                Text(
                    text = hint,
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isPlaced) {
                    Button(
                        onClick = { placeAt(viewSize.width / 2f, viewSize.height * 0.6f) },
                        colors = ButtonDefaults.buttonColors(containerColor = GasTrackBlue)
                    ) { Text("Place here", color = Color.White, fontWeight = FontWeight.Bold) }
                }
                OutlinedButton(
                    onClick = { clearPlacement() },
                    enabled = isPlaced,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Reset") }
                Button(
                    onClick = onScanComplete,
                    enabled = isPlaced,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange)
                ) { Text("Continue", color = Color.White, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "AI hazard detection is for prototype purposes only.\nAlways follow official LPG safety guidelines.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Animated "pinch to resize" cue. */
@Composable
private fun PinchCoach(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "pinch")
    val spread by t.animateFloat(
        initialValue = 18f, targetValue = 56f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "spread"
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(150.dp)) {
            val r = spread.dp.toPx()
            val a = Offset(center.x - r * 0.7f, center.y + r * 0.7f)
            val b = Offset(center.x + r * 0.7f, center.y - r * 0.7f)
            drawLine(Color.White.copy(alpha = 0.7f), a, b, strokeWidth = 4f)
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 14.dp.toPx(), center = a)
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 14.dp.toPx(), center = b)
        }
        Surface(shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.6f)) {
            Text(
                "Pinch to resize · Twist or drag to rotate",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/** Best hit under the finger: a detected horizontal plane first, then depth/feature points. */
private fun pickHit(frame: Frame, x: Float, y: Float): HitResult? {
    val hits = frame.hitTest(x, y)
    for (h in hits) {
        val t = h.trackable
        if (t is Plane && t.type == Plane.Type.HORIZONTAL_UPWARD_FACING && t.isPoseInPolygon(h.hitPose)) return h
    }
    for (h in hits) {
        val t = h.trackable
        if (t is DepthPoint) return h
        if (t is Point && t.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) return h
    }
    return null
}

/**
 * A pose on the estimated floor along the tap ray. Used when no surface has been detected yet.
 * The floor height is the real one if a floor was already seen, otherwise the phone height minus a guess.
 */
private fun freePose(frame: Frame, x: Float, y: Float, w: Int, h: Int, floorY: Float?): Pose? {
    if (w <= 0 || h <= 0) return null
    val cam = frame.camera
    if (cam.trackingState != TrackingState.TRACKING) return null

    val proj = FloatArray(16)
    cam.getProjectionMatrix(proj, 0, 0.05f, 100f)
    val viewM = FloatArray(16)
    cam.getViewMatrix(viewM, 0)
    val vp = FloatArray(16)
    Matrix.multiplyMM(vp, 0, proj, 0, viewM, 0)
    val inv = FloatArray(16)
    if (!Matrix.invertM(inv, 0, vp, 0)) return null

    fun unproject(z: Float): FloatArray {
        val ndc = floatArrayOf(2f * x / w - 1f, 1f - 2f * y / h, z, 1f)
        val o = FloatArray(4)
        Matrix.multiplyMV(o, 0, inv, 0, ndc, 0)
        return floatArrayOf(o[0] / o[3], o[1] / o[3], o[2] / o[3])
    }

    val near = unproject(-1f)
    val far = unproject(1f)
    var dx = far[0] - near[0]
    var dy = far[1] - near[1]
    var dz = far[2] - near[2]
    val len = sqrt(dx * dx + dy * dy + dz * dz)
    if (len < 1e-6f) return null
    dx /= len; dy /= len; dz /= len

    val t = cam.pose.translation
    val ox = t[0]; val oy = t[1]; val oz = t[2]
    val planeY = floorY ?: (oy - ASSUMED_PHONE_HEIGHT_M)

    var px: Float
    var pz: Float
    if (dy < -0.05f) {
        val d = (planeY - oy) / dy
        if (d in 0.3f..6f) {
            px = ox + dx * d
            pz = oz + dz * d
        } else {
            px = ox + dx * 1.5f
            pz = oz + dz * 1.5f
        }
    } else {
        // not looking downward: put it 1.5 m ahead on the estimated floor level
        val hl = sqrt(dx * dx + dz * dz)
        if (hl < 1e-3f) return null
        px = ox + dx / hl * 1.5f
        pz = oz + dz / hl * 1.5f
    }
    return Pose(floatArrayOf(px, planeY, pz), floatArrayOf(0f, 0f, 0f, 1f))
}

private fun largestFloorPlane(session: Session?): Plane? =
    session?.getAllTrackables(Plane::class.java)
        ?.filter {
            it.trackingState == TrackingState.TRACKING &&
                    it.subsumedBy == null &&
                    it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
        }
        ?.maxByOrNull { it.extentX * it.extentZ }

@Composable
fun HazardBanner(ui: HazardUiState) {
    val container: Color
    val title: String
    val lines: List<String>
    when (ui.state) {
        HazardState.SCANNING -> {
            container = Color(0xCC1F2937)
            title = "Analyzing environment…"
            lines = listOf("Point the camera at the intended placement area.")
        }
        HazardState.NO_HAZARD -> {
            container = Color(0xE60F766E)
            title = "No Recognized Hazard Detected"
            lines = listOf("No trained hazard was detected in the current view.")
        }
        HazardState.POTENTIAL_HAZARD -> {
            container = Color(0xEBB91C1C)
            title = "⚠ Potential Hazard Detected"
            lines = listOf(
                "Object: ${ui.objectName}",
                "Confidence: ${(ui.confidence * 100).roundToInt()}%",
                "Avoid placing the LPG cylinder near this hazard."
            )
        }
        HazardState.LOW_CONFIDENCE -> {
            container = Color(0xE6B45309)
            title = "Low Confidence"
            lines = listOf(
                "Best guess: ${ui.objectName} (${(ui.confidence * 100).roundToInt()}%) — not reliable.",
                "Try better lighting or a closer, steadier view."
            )
        }
        HazardState.ERROR -> {
            container = Color(0xE6374151)
            title = "Hazard detection unavailable"
            lines = listOf(ui.message ?: "Unknown error")
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = container
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            lines.forEach {
                Text(it, color = Color.White.copy(alpha = 0.92f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CenterMessage(
    message: String,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Color.White, textAlign = TextAlign.Center, fontSize = 16.sp)
            if (buttonText != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange)
                ) { Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}