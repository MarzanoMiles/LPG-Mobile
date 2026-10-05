package com.example.gastrack.ui.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import cn.easyar.CameraDevice
import cn.easyar.ImageTracker
import com.example.gastrack.R
import com.example.gastrack.ui.theme.ButtonOrange
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.GasTrackRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

private const val MK_TAG = "ARScanMarker"

private tailrec fun markerFindActivity(c: Context): Activity? = when (c) {
    is Activity -> c
    is ContextWrapper -> markerFindActivity(c.baseContext)
    else -> null
}

/** Marker mode: EasyAR image tracking of a printed or on-screen marker, with the GLB cylinder standing on it. */
@Composable
fun ARScanMarkerScreen(
    tankType: GasTankType,
    onScanComplete: () -> Unit
) {
    val available = remember {
        try { CameraDevice.isAvailable() && ImageTracker.isAvailable() } catch (t: Throwable) { false }
    }
    if (!available) {
        LaunchedEffect(Unit) { Log.w(MK_TAG, "CameraDevice or ImageTracker not available; using camera-overlay mode") }
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
            MarkerContent(tankType = tankType, onScanComplete = onScanComplete)
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
private fun MarkerContent(tankType: GasTankType, onScanComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val analyzer = remember { HazardAnalyzer(context) }
    DisposableEffect(analyzer) { onDispose { analyzer.close() } }
    val hazardUi by analyzer.state.collectAsState()

    val uiFlow = remember { MutableStateFlow(MarkerUiState()) }
    val ui by uiFlow.collectAsState()

    val scene = remember { EasyArMarkerScene(analyzer) { uiFlow.value = it } }
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
            Log.e(MK_TAG, "Model load failed", t)
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

    var digital by remember { mutableStateOf(false) }
    var showMarker by remember { mutableStateOf(false) }
    val markerBitmap = remember {
        try {
            context.assets.open(MARKER_ASSET).use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        } catch (t: Throwable) { null }
    }

    // Full brightness while the marker is shown on this screen (so another phone can scan it)
    val activity = remember { markerFindActivity(context) }
    DisposableEffect(showMarker) {
        val window = activity?.window
        val previous = window?.attributes?.screenBrightness
        if (showMarker && window != null) {
            val p = window.attributes
            p.screenBrightness = 1f
            window.attributes = p
        }
        onDispose {
            if (window != null && previous != null) {
                val p = window.attributes
                p.screenBrightness = previous
                window.attributes = p
            }
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

        // On-screen target cursor: where to aim until the marker is found
        if (!ui.visible) {
            Image(
                painter = painterResource(id = R.drawable.marker_reticle),
                contentDescription = "Place target here",
                modifier = Modifier.align(Alignment.Center).size(180.dp).alpha(0.85f)
            )
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
                Text("Marker mode · ${tankType.label}", color = Color.White, fontSize = 13.sp)
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
                !ui.loaded -> "Loading the marker…"
                ui.visible -> "Marker found · Drag to rotate · Pinch to resize · Reset restores real size"
                digital -> "Show the marker on another screen at full brightness, then point this camera at it"
                else -> "Lay the printed A4 marker flat on the floor and point the camera at it"
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
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!digital) {
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = GasTrackBlue)) { Text("Physical marker") }
                    OutlinedButton(
                        onClick = { digital = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Digital marker") }
                } else {
                    OutlinedButton(
                        onClick = { digital = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Physical marker") }
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = GasTrackBlue)) { Text("Digital marker") }
                }
            }
            if (digital && markerBitmap != null) {
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { showMarker = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Show marker on this screen (for a second phone)") }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { scene.resetModel() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Reset") }
                Button(
                    onClick = onScanComplete,
                    enabled = ui.everSeen,
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

        // Full-screen marker for scanning from a second device
        if (showMarker && markerBitmap != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.White).clickable { showMarker = false },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = markerBitmap,
                    contentDescription = "GasTrack marker",
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp)
                )
                Text(
                    "Tap anywhere to close",
                    color = Color.Black,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(12.dp)
                )
            }
        }
    }
}