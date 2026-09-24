package com.example.gastrack.ui.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FlashlightOn
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.gastrack.R
import com.example.gastrack.ui.theme.*
import kotlin.random.Random
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

enum class ARScanState {
    SCANNING, ANALYZING, ERROR, SUCCESS
}

@Composable
fun ARScanScreen(
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    var scanState by remember { mutableStateOf(_root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SCANNING) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("TIPS: MOVE AWAY") }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            _root_ide_package_.com.example.gastrack.ui.presentation.CameraPreview(modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Camera Permission Required", color = Color.White)
            }
        }

        // --- Top Header ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val headerText = when(scanState) {
                _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SCANNING -> "Ar Scan"
                _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ANALYZING -> "Ar Analyze"
                _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ERROR -> "Ar Error"
                _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SUCCESS -> "Ar Successful"
            }

            Text(
                text = headerText,
                color = Color.Gray,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Logo Overlay
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.favicon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = GasTrackBlue, fontWeight = FontWeight.Bold)) {
                            append("Gas")
                        }
                        withStyle(style = SpanStyle(color = GasTrackRed, fontWeight = FontWeight.Bold)) {
                            append("Track")
                        }
                    },
                    fontSize = 20.sp
                )
            }
            Text(
                text = "Optimize Your LPG, Effortlessly.",
                fontSize = 8.sp,
                color = GasTrackBlue,
                fontWeight = FontWeight.Medium
            )
        }

        // --- Scanning Frame ---
        if (scanState == _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SCANNING || scanState == _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ANALYZING) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.Center)
            ) {
                _root_ide_package_.com.example.gastrack.ui.presentation.ScanningFrame(modifier = Modifier.fillMaxSize())

                // Simulation trigger: Click frame to Analyze
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            if (scanState == _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SCANNING) scanState = _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ANALYZING
                        }
                )
            }
        }

        // --- Tank Overlay (Analyzing / Success / Error Background) ---
        if (scanState != _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SCANNING) {
            Image(
                painter = painterResource(id = R.drawable.tank_11kg), // Use a default or specific tank
                contentDescription = "Virtual Tank",
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .offset(y = (-20).dp)
                    .clickable {
                        // Simulate different outcomes when clicking the tank during analysis
                        if (scanState == _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ANALYZING) {
                            val outcomes = listOf(_root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SUCCESS, _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ERROR, _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ERROR)
                            val nextState = outcomes.random()
                            scanState = nextState
                            if (nextState == _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ERROR) {
                                errorMessage = if (Random.nextBoolean()) "TIPS: MOVE AWAY" else "NOTE: NOT SAFE"
                            }
                        }
                    }
            )
        }

        // --- Overlays (Dialogs) ---
        when (scanState) {
            _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.ERROR -> {
                _root_ide_package_.com.example.gastrack.ui.presentation.ARStatusDialog(
                    title = "ERROR",
                    subtext = errorMessage,
                    buttonText = "SCAN AGAIN",
                    onButtonClick = {
                        scanState =
                            _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SCANNING
                    }
                )
            }
            _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SUCCESS -> {
                _root_ide_package_.com.example.gastrack.ui.presentation.ARStatusDialog(
                    title = "SUCCESS",
                    subtext = "SCAN SUCCESSFUL",
                    buttonText = "CONTINUE",
                    onButtonClick = onScanComplete
                )
            }
            else -> {}
        }

        // --- Bottom Controls ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Flashlight Toggle
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clickable { isFlashlightOn = !isFlashlightOn },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = "Flashlight",
                    tint = if (isFlashlightOn) Color.Yellow else Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Manual Next Button (If successful but dialog not shown for some reason)
            if (scanState == _root_ide_package_.com.example.gastrack.ui.presentation.ARScanState.SUCCESS) {
                IconButton(
                    onClick = onScanComplete,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 40.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ARStatusDialog(
    title: String,
    subtext: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(2.dp, ButtonOrange)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtext,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ScanningFrame(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        val cornerSize = 40.dp
        val strokeWidth = 4.dp
        val color = Color.White

        _root_ide_package_.com.example.gastrack.ui.presentation.CornerShape(
            modifier = Modifier.align(
                Alignment.TopStart
            ), size = cornerSize, stroke = strokeWidth, color = color, isTop = true, isLeft = true
        )
        _root_ide_package_.com.example.gastrack.ui.presentation.CornerShape(
            modifier = Modifier.align(
                Alignment.TopEnd
            ), size = cornerSize, stroke = strokeWidth, color = color, isTop = true, isLeft = false
        )
        _root_ide_package_.com.example.gastrack.ui.presentation.CornerShape(
            modifier = Modifier.align(
                Alignment.BottomStart
            ), size = cornerSize, stroke = strokeWidth, color = color, isTop = false, isLeft = true
        )
        _root_ide_package_.com.example.gastrack.ui.presentation.CornerShape(
            modifier = Modifier.align(
                Alignment.BottomEnd
            ), size = cornerSize, stroke = strokeWidth, color = color, isTop = false, isLeft = false
        )
    }
}

@Composable
fun CornerShape(modifier: Modifier, size: Dp, stroke: Dp, color: Color, isTop: Boolean, isLeft: Boolean) {
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .align(if (isTop) Alignment.TopStart else Alignment.BottomStart)
                .fillMaxWidth(0.4f)
                .height(stroke)
                .background(color, RoundedCornerShape(stroke / 2))
                .let { if (!isLeft) it.align(if (isTop) Alignment.TopEnd else Alignment.BottomEnd) else it }
        )
        Box(
            modifier = Modifier
                .align(if (isLeft) Alignment.TopStart else Alignment.TopEnd)
                .fillMaxHeight(0.4f)
                .width(stroke)
                .background(color, RoundedCornerShape(stroke / 2))
                .let { if (!isTop) it.align(if (isLeft) Alignment.BottomStart else Alignment.BottomEnd) else it }
        )
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) { }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@ComposePreview(showBackground = true)
@Composable
fun ARScanScreenPreview() {
    _root_ide_package_.com.example.gastrack.ui.presentation.ARScanScreen(onScanComplete = {})
}
