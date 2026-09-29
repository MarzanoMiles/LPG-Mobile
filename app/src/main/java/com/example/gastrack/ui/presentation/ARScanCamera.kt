package com.example.gastrack.ui.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gastrack.R
import com.example.gastrack.ui.theme.ButtonOrange
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.GasTrackRed
import io.github.sceneview.Scene
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import java.util.concurrent.Executors

private const val CAM_TAG = "ARScanCamera"

/**
 * Camera-overlay mode (no ARCore): CameraX preview + TFLite hazard detection,
 * with the selected GLB cylinder drawn over the camera as a transparent 3D layer.
 * The cylinder is NOT anchored to the real floor; the user positions it by hand.
 */
@Composable
fun ARScanCameraScreen(
    tankType: GasTankType,
    onScanComplete: () -> Unit
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
            CameraOverlayContent(tankType = tankType, onScanComplete = onScanComplete)
        } else {
            CenterMessage(
                message = "Camera permission is required\nto use AR hazard scanning.",
                buttonText = "Grant Permission",
                onButtonClick = { launcher.launch(Manifest.permission.CAMERA) }
            )
        }
    }
}

@Composable
private fun CameraOverlayContent(tankType: GasTankType, onScanComplete: () -> Unit) {
    val context = LocalContext.current

    // --- Hazard detection (created once, released on dispose) ---
    val analyzer = remember { HazardAnalyzer(context) }
    DisposableEffect(analyzer) { onDispose { analyzer.close() } }
    val hazardUi by analyzer.state.collectAsState()

    // --- 3D layer state ---
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val cameraNode = rememberCameraNode(engine)
    val collisionSystem = rememberCollisionSystem(view)
    val childNodes = rememberNodes()

    var resetKey by remember { mutableIntStateOf(0) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Only the selected model is loaded. Runs again on Reset (re-centers the cylinder).
    LaunchedEffect(tankType, resetKey) {
        clearNodes(childNodes)
        try {
            childNodes += createCylinderNode(modelLoader, tankType)
            loadError = null
        } catch (t: Throwable) {
            Log.e(CAM_TAG, "Model load failed", t)
            loadError = "Could not load the ${tankType.label} model."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: camera
        CameraXPreview(modifier = Modifier.fillMaxSize(), analyzer = analyzer)

        // Frame-level warning border (the classifier has no bounding boxes)
        if (hazardUi.state == HazardState.POTENTIAL_HAZARD) {
            val pulse = rememberInfiniteTransition(label = "pulse")
            val alpha by pulse.animateFloat(
                initialValue = 0.25f, targetValue = 0.95f,
                animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                label = "pulseAlpha"
            )
            Box(Modifier.fillMaxSize().border(8.dp, Color(0xFFFF3B30).copy(alpha = alpha)))
        }

        // Layer 2: UI. The 3D view only occupies the middle band, so the banner and
        // buttons stay visible and clickable (the transparent 3D surface draws on top).
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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

            // 3D band
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Scene(
                    modifier = Modifier.fillMaxSize(),
                    engine = engine,
                    modelLoader = modelLoader,
                    view = view,
                    isOpaque = false,            // transparent background so the camera shows through
                    cameraNode = cameraNode,
                    cameraManipulator = null,    // no orbit camera; gestures move the model instead
                    collisionSystem = collisionSystem,
                    childNodes = childNodes
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = 0.55f)) {
                    Text(
                        text = loadError ?: "Drag to move · Twist to rotate · Pinch to resize\n(camera-overlay mode: not anchored to the floor)",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { resetKey++ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Reset") }
                    Button(
                        onClick = onScanComplete,
                        enabled = childNodes.isNotEmpty(),
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
}

/** CameraX preview + ImageAnalysis (keep-only-latest) feeding the hazard analyzer. */
@Composable
private fun CameraXPreview(modifier: Modifier, analyzer: HazardAnalyzer) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { analysisExecutor.shutdown() } }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // TextureView-based preview lives in the window, so the transparent 3D layer can sit above it.
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also { a -> a.setAnalyzer(analysisExecutor) { proxy -> analyzer.analyze(proxy) } }

                    val selector = if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                        CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                } catch (e: Exception) {
                    Log.e(CAM_TAG, "Camera bind failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

/** Bottom of the cylinder sits at the node origin; placed in front of the camera at a realistic size. */
private fun createCylinderNode(modelLoader: ModelLoader, type: GasTankType): ModelNode =
    ModelNode(
        modelInstance = modelLoader.createModelInstance(assetFileLocation = type.assetPath),
        scaleToUnits = type.realHeightMeters,
        centerOrigin = Position(x = 0f, y = -1f, z = 0f)
    ).apply {
        position = Position(x = 0f, y = -0.35f, z = -1.5f)
        isPositionEditable = true
        isRotationEditable = true
        isScaleEditable = true
    }

private fun clearNodes(nodes: MutableList<Node>) {
    nodes.toList().forEach { it.destroy() }
    nodes.clear()
}