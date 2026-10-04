package com.example.gastrack.ui.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gastrack.R
import com.example.gastrack.ui.theme.ButtonOrange
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.GasTrackRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

private const val EA_TAG = "ARScanEasyAr"

/**
 * EasyAR mode: EasyAR motion tracking + floor hit test + GLB cylinder + hazard detection.
 * Falls back to the camera-overlay screen when EasyAR is not ready (emulator, no license key, unsupported phone).
 */
@Composable
fun ARScanEasyArScreen(
    tankType: GasTankType,
    onScanComplete: () -> Unit
) {
    if (!EasyArSupport.status.ready) {
        LaunchedEffect(Unit) {
            Log.w(EA_TAG, "EasyAR unavailable (${EasyArSupport.status.message}); using camera-overlay mode")
        }
        ARScanCameraScreen(tankType = tankType, onScanComplete = onScanComplete)
        return
    }

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
            EasyArContent(tankType = tankType, onScanComplete = onScanComplete)
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
private fun EasyArContent(tankType: GasTankType, onScanComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val analyzer = remember { HazardAnalyzer(context) }
    DisposableEffect(analyzer) { onDispose { analyzer.close() } }
    val hazardUi by analyzer.state.collectAsState()

    val uiFlow = remember { MutableStateFlow(EasyArUiState()) }
    val ui by uiFlow.collectAsState()

    val scene = remember { EasyArScene(analyzer) { uiFlow.value = it } }
    val glView = remember { EasyArGLView(context, scene) }

    var modelError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(tankType) {
        try {
            val data = withContext(Dispatchers.Default) {
                GlbParser.parse(context, tankType.assetPath, tankType.realHeightMeters)
            }
            scene.setModel(data)
            modelError = null
        } catch (t: Throwable) {
            Log.e(EA_TAG, "Model load failed", t)
            modelError = "Could not load the ${tankType.label} model."
        }
    }

    DisposableEffect(lifecycleOwner, glView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glView.resumeView()
                Lifecycle.Event.ON_PAUSE -> glView.pauseView()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            glView.finishAndPause()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { glView }, modifier = Modifier.fillMaxSize())

        if (hazardUi.state == HazardState.POTENTIAL_HAZARD) {
            val pulse = rememberInfiniteTransition(label = "pulse")
            val alpha by pulse.animateFloat(
                initialValue = 0.25f, targetValue = 0.95f,
                animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                label = "pulseAlpha"
            )
            Box(Modifier.fillMaxSize().border(8.dp, Color(0xFFFF3B30).copy(alpha = alpha)))
        }

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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val hint = ui.error ?: modelError ?: when {
                !ui.tracking -> "Move your phone slowly so tracking can start"
                ui.placed && ui.estimated -> "Placed on an estimated floor · Drag to rotate · Pinch to resize"
                ui.placed -> "Tap to move · Drag to rotate · Pinch to resize"
                ui.searching -> "Looking for the floor… keep the phone steady, sweeping slowly"
                ui.noSurface -> "No floor found. Aim at a textured floor 1–2 m ahead and move slowly, or use Estimated floor"
                ui.floorReady -> "Floor found. Tap it to place the ${tankType.label} cylinder"
                else -> "Point at the floor 1–2 m ahead and move slowly side to side"
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
                if (!ui.placed && ui.tracking) {
                    OutlinedButton(
                        onClick = { scene.requestEstimatedPlacement() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Estimated floor") }
                }

                OutlinedButton(
                    onClick = { scene.clearPlacement() },
                    enabled = ui.placed,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Reset") }
                Button(
                    onClick = onScanComplete,
                    enabled = ui.placed,
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