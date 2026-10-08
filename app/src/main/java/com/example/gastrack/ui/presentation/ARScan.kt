package com.example.gastrack.ui.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.gastrack.R
import com.example.gastrack.ui.theme.ButtonOrange
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.GasTrackRed
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import android.util.Log
import com.google.ar.core.Session
private class FrameHolder {
    var frame: Frame? = null
    var session: Session? = null
}

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

@Composable
private fun ArPlacementContent(tankType: GasTankType, onScanComplete: () -> Unit, onArCoreFailed: () -> Unit) {
    val context = LocalContext.current

    // --- AR scene state ---
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val cameraNode = rememberARCameraNode(engine)
    val collisionSystem = rememberCollisionSystem(view)
    val childNodes = rememberNodes()
    val frameHolder = remember { FrameHolder() }

    var hasPlane by remember { mutableStateOf(false) }
    var placementError by remember { mutableStateOf<String?>(null) }
    var tapHint by remember { mutableStateOf<String?>(null) }
    val isPlaced = childNodes.isNotEmpty()
    val placeAnchor: (Anchor) -> Unit = { anchor ->
        try {
            childNodes += createTankAnchorNode(engine, modelLoader, anchor, tankType)
            placementError = null
            tapHint = null
            Log.i("ARScan", "placed ${tankType.label}")
        } catch (t: Throwable) {
            Log.e("ARScan", "Model placement failed", t)
            anchor.detach()
            placementError = "Could not load the ${tankType.label} model."
        }
    }

    // --- Hazard detection (created once, released on dispose) ---
    val analyzer = remember { HazardAnalyzer(context) }
    DisposableEffect(analyzer) { onDispose { analyzer.close() } }
    val hazardUi by analyzer.state.collectAsState()

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
            sessionConfiguration = { _, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
             // config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR //Some phones handle the HDR lighting badly, and the model can come out nearly black. If the tank shows up with this change, leave it at DISABLED.
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED //comment this laterr
                config.focusMode = Config.FocusMode.AUTO
            },
            onSessionFailed = { e ->
                Log.e("ARScan", "ARCore session failed", e)
                onArCoreFailed()
            },
            onSessionUpdated = { session, updatedFrame ->
                frameHolder.frame = updatedFrame
                frameHolder.session = session
                hasPlane = session.getAllTrackables(Plane::class.java).any {
                    it.trackingState == TrackingState.TRACKING && it.subsumedBy == null
                }
                analyzer.analyze(updatedFrame)   // throttled, off the UI thread
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, node ->
                    if (node == null && childNodes.isEmpty()) {
                        val frame = frameHolder.frame
                        var anchor = frame
                            ?.hitTest(motionEvent.x, motionEvent.y)
                            ?.firstOrNull { it.isValid(depthPoint = false, point = false) }
                            ?.createAnchorOrNull()
                        if (anchor == null) {
                            // No surface under the finger: fall back to instant placement 1.5 m ahead
                            anchor = frame
                                ?.hitTestInstantPlacement(motionEvent.x, motionEvent.y, 1.5f)
                                ?.firstOrNull()
                                ?.createAnchorOrNull()
                        }
                        if (anchor != null) {
                            placeAnchor(anchor)
                        } else {
                            Log.i("ARScan", "tap found nothing to place on")
                            tapHint = "No surface at that spot. Tap on the dotted grid, or use Place on floor"
                        }
                    }
                }
            )
        )

        // Pulsing frame-level warning (the classifier has no bounding boxes)
        if (hazardUi.state == HazardState.POTENTIAL_HAZARD) {
            val pulse = rememberInfiniteTransition(label = "pulse")
            val alpha by pulse.animateFloat(
                initialValue = 0.25f, targetValue = 0.95f,
                animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                label = "pulseAlpha"
            )
            Box(
                Modifier.fillMaxSize().border(8.dp, Color(0xFFFF3B30).copy(alpha = alpha))
            )
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
                isPlaced -> "Drag to move · Twist to rotate"
                hasPlane -> "Surface found — tap to place the ${tankType.label} cylinder"
                else -> "Move your phone slowly to detect the floor or a table"
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
                if (!isPlaced && hasPlane) {
                    Button(
                        onClick = {
                            val plane = largestFloorPlane(frameHolder.session)
                            val anchor = plane?.createAnchor(plane.centerPose)
                            if (anchor != null) placeAnchor(anchor)
                            else tapHint = "No floor found yet. Keep sweeping the phone slowly"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GasTrackBlue)
                    ) { Text("Place on floor", color = Color.White, fontWeight = FontWeight.Bold) }
                }
                OutlinedButton(
                    onClick = { clearPlacement(childNodes) },
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

/** Only the selected model is loaded, once, at placement time. Bottom of the model sits on the surface. */
private fun createTankAnchorNode(
    engine: Engine,
    modelLoader: ModelLoader,
    anchor: Anchor,
    type: GasTankType
): AnchorNode {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
    val modelNode = ModelNode(
        modelInstance = modelLoader.createModelInstance(assetFileLocation = type.assetPath),
        scaleToUnits = type.realHeightMeters,               // real-world size in meters
        centerOrigin = Position(x = 0f, y = -1f, z = 0f)    // origin at the bottom of the cylinder
    ).apply {
        isPositionEditable = true
        isRotationEditable = true
        isScaleEditable = ALLOW_PINCH_RESIZE
        //isScaleEditable = false   // keep real-world size; set true if you want pinch-to-scale
    }
    anchorNode.addChildNode(modelNode)
    return anchorNode
}

private fun clearPlacement(nodes: MutableList<Node>) {
    nodes.toList().forEach { n ->
        (n as? AnchorNode)?.anchor?.detach()
        n.destroy()
    }
    nodes.clear()
}

private fun largestFloorPlane(session: Session?): Plane? =
    session?.getAllTrackables(Plane::class.java)
        ?.filter {
            it.trackingState == TrackingState.TRACKING &&
                    it.subsumedBy == null &&
                    it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
        }
        ?.maxByOrNull { it.extentX * it.extentZ }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}