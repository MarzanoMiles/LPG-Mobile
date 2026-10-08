package com.example.gastrack.ui.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.gastrack.ui.theme.ButtonOrange
import com.google.ar.core.ArCoreApk
import kotlinx.coroutines.delay

private const val ROUTER_TAG = "ARScanRouter"

object ArBackendConfig {
    /** Set to true to skip ARCore and test the EasyAR modes on a phone that supports ARCore. */
    const val SKIP_ARCORE = false
}

private enum class ArChoice { CHECKING, ARCORE, NEEDS_INSTALL, EASYAR }

private tailrec fun routerFindActivity(c: Context): Activity? = when (c) {
    is Activity -> c
    is ContextWrapper -> routerFindActivity(c.baseContext)
    else -> null
}

/**
 * Picks the AR backend: ARCore -> EasyAR motion tracking -> EasyAR marker -> CameraX overlay.
 * (The last three are chosen inside ARScanEasyArScreen.)
 */
@Composable
fun ARScanRouterScreen(
    tankType: GasTankType,
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current
    var recheck by remember { mutableIntStateOf(0) }
    var choice by remember { mutableStateOf(ArChoice.CHECKING) }
    var arCoreFailed by remember { mutableStateOf(false) }

    // After the user installs/updates ARCore and comes back, check again.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { recheck++ }

    LaunchedEffect(recheck) {
        if (choice == ArChoice.ARCORE || choice == ArChoice.EASYAR) return@LaunchedEffect
        if (ArBackendConfig.SKIP_ARCORE) {
            choice = ArChoice.EASYAR
            return@LaunchedEffect
        }
        val api = ArCoreApk.getInstance()
        var a = api.checkAvailability(context)
        while (a.isTransient) {
            delay(200)
            a = api.checkAvailability(context)
        }
        choice = when {
            a == ArCoreApk.Availability.SUPPORTED_INSTALLED -> ArChoice.ARCORE
            a.isSupported -> ArChoice.NEEDS_INSTALL
            else -> ArChoice.EASYAR
        }
        Log.i(ROUTER_TAG, "ARCore availability=$a -> $choice")
    }

    when {
        arCoreFailed || choice == ArChoice.EASYAR ->
            ARScanEasyArScreen(tankType = tankType, onScanComplete = onScanComplete)

        choice == ArChoice.ARCORE ->
            ARScanScreen(
                tankType = tankType,
                onScanComplete = onScanComplete,
                onArCoreFailed = {
                    Log.w(ROUTER_TAG, "ARCore session failed; switching to EasyAR")
                    arCoreFailed = true
                }
            )

        choice == ArChoice.NEEDS_INSTALL ->
            ArInstallChoice(
                onInstall = {
                    try {
                        routerFindActivity(context)?.let { ArCoreApk.getInstance().requestInstall(it, true) }
                    } catch (_: Exception) { }
                },
                onUseFallback = { choice = ArChoice.EASYAR }
            )

        else -> Box(Modifier.fillMaxSize().background(Color.Black)) {
            CenterMessage("Checking AR support…")
        }
    }
}

@Composable
private fun ArInstallChoice(onInstall: () -> Unit, onUseFallback: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "This phone supports ARCore, but Google Play Services for AR\nneeds to be installed or updated.",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onInstall,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange)
            ) { Text("Install ARCore", color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onUseFallback,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) { Text("Use the other AR mode instead") }
        }
    }
}