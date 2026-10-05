package com.example.gastrack.ui.presentation

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import cn.easyar.CalibrationDownloader
import cn.easyar.Engine
import cn.easyar.FunctorOfVoidFromCalibrationDownloadStatusAndOptionalOfString
import cn.easyar.ImmediateCallbackScheduler
import cn.easyar.MotionTrackerCameraDevice

object EasyArConfig {
    // Paste your EasyAR Sense license key here. Do not commit it to a public repository.
    const val LICENSE_KEY = "BRQLQQEHE10ZYTsHdVD75wLJ59bgkZvC98EjOjUmPWoBNjt3NTssOnp3P3kzeyxqITYzNjMsK1gnODlxLHs7dy13dDotNCtsJScTfTkcPDp6ZHQ6LDw7fS4mPWtibwNjYjctdiQ5PVEkJnoiGwh0OjY0KnEhOyxrYm8DOiM6NXU1OzFsOXcFNGIlNHk0MzdqLSZ6Iht3L3EuMTdvM3d0Oi00OzodeXp1LzEtdCUmeiIbdyt9LiY9Ngk4OX8lASp5Iz4xdid3dDozMDZrJXsbdC8gPEolNjd/LjwscS87ejRiJj12MzB2SiU2N2okPDZ/Ynl6ayU7K31uGjpyJTYsTDI0O3MpOz86bHcrfS4mPTYTICp+ITY9TDI0O3MpOz86bHcrfS4mPTYTJTlqMzALaCEhMXksGDloYnl6ayU7K31uGDdsKTo2TDI0O3MpOz86bHcrfS4mPTYEMDZrJQYoeTQ8OXQNNCg6bHcrfS4mPTYDFBxMMjQ7cyk7PzodeXp9OCUxaiUBMXUlBix5LSV6Ii4gNHRsdzFrDDo7eSx3Yn4hOSt9PXkjOiIgNnwsMBF8M3diQ2I2N3VuMCB5LSU0fW4yOWs0Jzl7K3cFNGIjOWopNDZsM3diQ2I2N3UtIDZxNCx6RWx3KHQhIT53MjgrOnoOenkuMSp3KTF6RWx3NXckIDR9M3diQ2ImPXYzMHZRLTQ/fRQnOXsrPDZ/Ynl6ayU7K31uFjR3NTEKfSM6P3YpITF3Lnd0OjMwNmslewp9IzoqfCk7Pzpsdyt9LiY9Ng83Mn0jIQxqITYzcS4yejRiJj12MzB2SzUnPnkjMAxqITYzcS4yejRiJj12MzB2SzA0KmslBih5NDw5dA00KDpsdyt9LiY9Ng06LHEvOwxqITYzcS4yejRiJj12MzB2XCU7K30TJTlsKTQ0VSElejRiJj12MzB2WwERDGohNjNxLjJ6RWx3PWAwPCp9FDw1fRMhOXUwd2J2NTk0NGI8K1QvNjl0Ym8+eSwmPWVsLnp6NTs8dCUcPGtibwM6Ygh0OjY0KnEhOyxrYm8DOiM6NXU1OzFsOXcFNGIlNHk0MzdqLSZ6Iht3MXczdwU0Yjg3fDU5PWtibwM6MzA2ayV7EXUhMj1MMjQ7cyk7Pzpsdyt9LiY9NgM5N20kBz17LzI2cTQ8N3ZieXprJTsrfW4HPXsvJzxxLjJ6NGImPXYzMHZXIj89ezQBKnkjPjF2J3d0OjMwNmslewttMjM5eyUBKnkjPjF2J3d0OjMwNmslewtoIScrfRMlOWwpNDRVISV6NGImPXYzMHZVLyExdy4BKnkjPjF2J3d0OjMwNmslexx9LiY9SzA0LHEhORV5MHd0OjMwNmslextZBAEqeSM+MXYndwU0YjAgaCknPUwpOD1LNDQ1aGJvNm0sOXQ6KSYUdyM0NDp6Mzl0MzAlRT0wzD1Lw1iGjvJCioZcJDoQO2ULoHUWBBPJwpVdBzczdKY1tAPYxBj4sBqXQLEATOFXun01MZwANjo8QUO7MY6HC0IVbjoWiqhDuSLHCNE0kchTmj02u6BmVbfyzb/VwJp4ABQAemXVrjY6q4Gvkl1rN0XnA19LAJysgbLt7QD7c5D8CQmrhe2lZx6ngD3EiHBayLo6mJhrLIMDYq92iH88N4sJ7WwtCR9cYRr3OuedvxQMjhxnPCmNxhWJ2r7b9I2pUUK3YigX71uXfhJi8mBpeu/rErEFshmr/E3x1GOAjx6YbnJlxlhtg1Rqtw+XyfXchGwW6TpfhoBxH2BAVVgY"

    // Set to true to test marker (image tracking) mode on a phone that supports motion tracking.
    const val FORCE_MARKER_MODE = false
}

enum class EasyArMode { MOTION_TRACKING, IMAGE_TRACKING, NONE }

data class EasyArStatus(val ready: Boolean, val message: String, val mode: EasyArMode = EasyArMode.NONE)

object EasyArSupport {
    private const val TAG = "EasyAR"

    @Volatile
    var status = EasyArStatus(false, "EasyAR not initialized")
        private set

    private var calibrationDownloader: CalibrationDownloader? = null

    fun isArmDevice(): Boolean = Build.SUPPORTED_ABIS.any { it.startsWith("arm") }

    /** Safe on any device: never crashes, and reports why EasyAR is unavailable. */
    fun initialize(activity: Activity) {
        if (status.ready) return
        status = try {
            if (!isArmDevice()) {
                EasyArStatus(false, "EasyAR needs an ARM device (this one is ${Build.SUPPORTED_ABIS.first()})")
            } else if (EasyArConfig.LICENSE_KEY.startsWith("PASTE")) {
                EasyArStatus(false, "EasyAR license key not set")
            } else {
                try {
                    System.loadLibrary("arcore_sdk_c") // the sample loads this before EasyAR
                } catch (e: UnsatisfiedLinkError) {
                    Log.w(TAG, "arcore_sdk_c not found: ${e.message}")
                }
                if (!Engine.initialize(activity, EasyArConfig.LICENSE_KEY)) {
                    EasyArStatus(false, "EasyAR initialize failed: ${Engine.errorMessage()}")
                } else if (EasyArConfig.FORCE_MARKER_MODE || !MotionTrackerCameraDevice.isAvailable()) {
                    EasyArStatus(
                        false,
                        "Motion tracking not supported (or marker mode forced); image tracking will be used",
                        EasyArMode.IMAGE_TRACKING
                    )
                } else {
                    downloadCalibration()
                    EasyArStatus(
                        true,
                        "EasyAR ready, motion tracking quality=${MotionTrackerCameraDevice.getQualityLevel()}",
                        EasyArMode.MOTION_TRACKING
                    )
                }
            }
        } catch (t: Throwable) { // includes UnsatisfiedLinkError when native libs are missing
            Log.e(TAG, "EasyAR init failed", t)
            EasyArStatus(false, "EasyAR failed to load: ${t.message}")
        }
        Log.i(TAG, status.message)
    }

    // Best effort, like the sample: improves tracking accuracy on some phones.
    private fun downloadCalibration() {
        val d = CalibrationDownloader()
        calibrationDownloader = d
        d.download(10000, ImmediateCallbackScheduler.getDefault(),
            object : FunctorOfVoidFromCalibrationDownloadStatusAndOptionalOfString {
                override fun invoke(status: Int, error: String?) {
                    Log.i(TAG, "Calibration download status=$status error=${error ?: ""}")
                    Handler(Looper.getMainLooper()).post {
                        d.dispose()
                        if (calibrationDownloader === d) calibrationDownloader = null
                    }
                }
            })
    }
}