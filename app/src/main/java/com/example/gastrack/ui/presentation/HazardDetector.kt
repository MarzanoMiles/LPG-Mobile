package com.example.gastrack.ui.presentation

import android.content.Context
import android.media.Image
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "HazardDetector"

/**
 * TensorFlow Lite wrapper for a Teachable Machine image classifier.
 * Reads input/output tensor info from the model itself; nothing about size or type is assumed.
 */
class HazardDetector(
    context: Context,
    modelAsset: String = "model_unquant.tflite",
    labelsAsset: String = "labels.txt"
) : Closeable {

    val labels: List<String>
    val modelInfo: String

    private val interpreter: Interpreter
    private val inputW: Int
    private val inputH: Int
    private val inputType: DataType
    private val outputType: DataType
    private val outScale: Float
    private val outZero: Int
    private val outCount: Int
    private val inputBuffer: ByteBuffer
    private val floatOut: Array<FloatArray>
    private val byteOut: Array<ByteArray>

    init {
        labels = context.assets.open(labelsAsset).bufferedReader().readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.replace(Regex("^\\d+\\s+"), "") } // "2 Candle" -> "Candle"

        val bytes = context.assets.open(modelAsset).use { it.readBytes() }
        val modelBuffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        modelBuffer.put(bytes).rewind()

        interpreter = Interpreter(modelBuffer, Interpreter.Options().apply { setNumThreads(2) })
        try {
            val inTensor = interpreter.getInputTensor(0)
            val inShape = inTensor.shape()
            check(inShape.size == 4 && inShape[0] == 1 && inShape[3] == 3) {
                "Unexpected input shape ${inShape.toList()}; expected [1,H,W,3]"
            }
            inputH = inShape[1]
            inputW = inShape[2]
            inputType = inTensor.dataType()
            check(inputType == DataType.FLOAT32 || inputType == DataType.UINT8) {
                "Unsupported input type $inputType"
            }

            val outTensor = interpreter.getOutputTensor(0)
            val outShape = outTensor.shape()
            outCount = outShape.last()
            outputType = outTensor.dataType()
            check(outputType == DataType.FLOAT32 || outputType == DataType.UINT8 || outputType == DataType.INT8) {
                "Unsupported output type $outputType"
            }
            val q = outTensor.quantizationParams()
            outScale = if (q.scale == 0f) 1f else q.scale
            outZero = q.zeroPoint

            check(labels.size == outCount) {
                "labels.txt has ${labels.size} entries but the model outputs $outCount classes"
            }

            val bytesPerChannel = if (inputType == DataType.FLOAT32) 4 else 1
            inputBuffer = ByteBuffer.allocateDirect(inputW * inputH * 3 * bytesPerChannel)
                .order(ByteOrder.nativeOrder())
            floatOut = Array(1) { FloatArray(outCount) }
            byteOut = Array(1) { ByteArray(outCount) }

            modelInfo = "input=${inShape.toList()} $inputType, output=${outShape.toList()} " +
                    "$outputType (scale=$outScale zero=$outZero), labels=$labels"
            Log.i(TAG, modelInfo)
        } catch (t: Throwable) {
            interpreter.close()
            throw t
        }
    }

    /** ARCore camera frame (landscape sensor image, rotated 90° to portrait). Does NOT close [image]. */
    @Synchronized
    fun classify(image: Image): Prediction {
        val p = image.planes
        return infer(
            p[0].buffer, p[1].buffer, p[2].buffer,
            p[0].rowStride, p[0].pixelStride, p[1].rowStride, p[1].pixelStride,
            image.width, image.height, 90
        )
    }

    /** CameraX frame; uses the rotation CameraX reports. Does NOT close [image]. */
    @Synchronized
    fun classify(image: ImageProxy): Prediction {
        val p = image.planes
        return infer(
            p[0].buffer, p[1].buffer, p[2].buffer,
            p[0].rowStride, p[0].pixelStride, p[1].rowStride, p[1].pixelStride,
            image.width, image.height, image.imageInfo.rotationDegrees
        )
    }

    @Synchronized
    fun infer(
        yBuf: ByteBuffer, uBuf: ByteBuffer, vBuf: ByteBuffer,
        yRow: Int, yPix: Int, uvRow: Int, uvPix: Int,
        srcW: Int, srcH: Int, rotation: Int
    ): Prediction {
        fillInput(yBuf, uBuf, vBuf, yRow, yPix, uvRow, uvPix, srcW, srcH, rotation)
        val scores = FloatArray(outCount)
        when (outputType) {
            DataType.FLOAT32 -> {
                interpreter.run(inputBuffer, floatOut)
                floatOut[0].copyInto(scores)
            }
            DataType.UINT8 -> {
                interpreter.run(inputBuffer, byteOut)
                for (i in 0 until outCount) scores[i] = ((byteOut[0][i].toInt() and 0xFF) - outZero) * outScale
            }
            else -> { // INT8
                interpreter.run(inputBuffer, byteOut)
                for (i in 0 until outCount) scores[i] = (byteOut[0][i].toInt() - outZero) * outScale
            }
        }
        val ranked = labels.indices.map { labels[it] to scores[it] }.sortedByDescending { it.second }
        return Prediction(ranked[0].first, ranked[0].second, ranked.take(3))
    }

    /**
     * Rotates the frame upright ([rotation] = clockwise degrees needed: 0/90/180/270),
     * center-crops to a square, samples down to the model size, converts YUV -> RGB
     * straight into the input buffer. No intermediate Bitmap.
     */
    private fun fillInput(
        yBuf: ByteBuffer, uBuf: ByteBuffer, vBuf: ByteBuffer,
        yRow: Int, yPix: Int, uvRow: Int, uvPix: Int,
        srcW: Int, srcH: Int, rotation: Int
    ) {
        val upW: Int
        val upH: Int
        if (rotation == 90 || rotation == 270) { upW = srcH; upH = srcW } else { upW = srcW; upH = srcH }
        val side = minOf(upW, upH)
        val offX = (upW - side) / 2
        val offY = (upH - side) / 2
        val isFloat = inputType == DataType.FLOAT32

        inputBuffer.rewind()
        for (oy in 0 until inputH) {
            val uy = offY + oy * side / inputH
            for (ox in 0 until inputW) {
                val ux = offX + ox * side / inputW
                val sx: Int
                val sy: Int
                when (rotation) {
                    90 -> { sx = uy; sy = srcH - 1 - ux }
                    180 -> { sx = srcW - 1 - ux; sy = srcH - 1 - uy }
                    270 -> { sx = srcW - 1 - uy; sy = ux }
                    else -> { sx = ux; sy = uy }
                }

                val y = yBuf.get(sy * yRow + sx * yPix).toInt() and 0xFF
                val uvIdx = (sy shr 1) * uvRow + (sx shr 1) * uvPix
                val u = (uBuf.get(uvIdx).toInt() and 0xFF) - 128
                val v = (vBuf.get(uvIdx).toInt() and 0xFF) - 128

                val r = (y + 1.402f * v).toInt().coerceIn(0, 255)
                val g = (y - 0.344f * u - 0.714f * v).toInt().coerceIn(0, 255)
                val b = (y + 1.772f * u).toInt().coerceIn(0, 255)

                if (isFloat) {
                    // Teachable Machine float export expects [-1, 1]
                    inputBuffer.putFloat((r - 127.5f) / 127.5f)
                    inputBuffer.putFloat((g - 127.5f) / 127.5f)
                    inputBuffer.putFloat((b - 127.5f) / 127.5f)
                } else {
                    inputBuffer.put(r.toByte()); inputBuffer.put(g.toByte()); inputBuffer.put(b.toByte())
                }
            }
        }
        inputBuffer.rewind()
    }

    @Synchronized
    override fun close() {
        interpreter.close()
    }
}

/**
 * Owns the detector, throttling and smoothing.
 * - [analyze] with a CameraX ImageProxy: call from the CameraX analysis executor.
 * - [analyze] with an ARCore Frame: hands work to its own background thread.
 */
class HazardAnalyzer(
    context: Context,
    private val intervalMs: Long = 300L
) : Closeable {

    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor()
    private val busy = AtomicBoolean(false)
    private val smoother = HazardSmoother()
    private val _state = MutableStateFlow(HazardUiState(HazardState.SCANNING))
    val state: StateFlow<HazardUiState> = _state

    @Volatile private var detector: HazardDetector? = null
    @Volatile private var closed = false
    private var lastRunMs = 0L

    init {
        executor.execute {
            try {
                detector = HazardDetector(appContext)
            } catch (t: Throwable) {
                Log.e(TAG, "Detector init failed", t)
                _state.value = HazardUiState(HazardState.ERROR, message = t.message ?: "Model failed to load")
            }
        }
    }

    /** CameraX path. Always closes [proxy]. */
    fun analyze(proxy: ImageProxy) {
        try {
            val d = detector
            if (closed || d == null) return
            val now = SystemClock.elapsedRealtime()
            if (now - lastRunMs < intervalMs) return
            lastRunMs = now
            val p = d.classify(proxy)
            Log.d(TAG, "pred=${p.label} ${"%.2f".format(p.confidence)}")
            _state.value = smoother.update(p)
        } catch (t: Throwable) {
            Log.e(TAG, "Inference failed", t)
            _state.value = HazardUiState(HazardState.ERROR, message = t.message ?: "Inference failed")
        } finally {
            proxy.close()
        }
    }

    /** ARCore path. */
    fun analyze(frame: Frame) {
        if (closed) return
        val d = detector ?: return
        val now = SystemClock.elapsedRealtime()
        if (now - lastRunMs < intervalMs) return
        if (!busy.compareAndSet(false, true)) return  // keep-only-latest: drop if still busy

        val image: Image? = try {
            frame.acquireCameraImage()
        } catch (e: NotYetAvailableException) {
            null   // no camera image ready yet; skip this frame
        } catch (t: Throwable) {
            Log.w(TAG, "acquireCameraImage failed", t)
            null
        }
        if (image == null) { busy.set(false); return }
        lastRunMs = now

        try {
            executor.execute {
                try {
                    val p = d.classify(image)
                    Log.d(TAG, "pred=${p.label} ${"%.2f".format(p.confidence)}")
                    _state.value = smoother.update(p)
                } catch (t: Throwable) {
                    Log.e(TAG, "Inference failed", t)
                    _state.value = HazardUiState(HazardState.ERROR, message = t.message ?: "Inference failed")
                } finally {
                    image.close()
                    busy.set(false)
                }
            }
        } catch (e: RejectedExecutionException) {
            image.close(); busy.set(false)
        }
    }

    /** EasyAR path: true when a frame may be handed over (detector ready, not busy, throttle elapsed). */
    fun canSubmit(): Boolean =
        !closed && detector != null && !busy.get() &&
                SystemClock.elapsedRealtime() - lastRunMs >= intervalMs

    /** EasyAR path: runs [work] on the analyzer thread. Call only after [canSubmit] returned true. */
    fun submit(work: (HazardDetector) -> Prediction?) {
        val d = detector ?: return
        if (closed || !busy.compareAndSet(false, true)) return
        lastRunMs = SystemClock.elapsedRealtime()
        try {
            executor.execute {
                try {
                    val p = work(d)
                    if (p != null) {
                        Log.d(TAG, "pred=${p.label} ${"%.2f".format(p.confidence)}")
                        _state.value = smoother.update(p)
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Inference failed", t)
                    _state.value = HazardUiState(HazardState.ERROR, message = t.message ?: "Inference failed")
                } finally {
                    busy.set(false)
                }
            }
        } catch (e: RejectedExecutionException) {
            busy.set(false)
        }
    }
    override fun close() {
        closed = true
        try {
            executor.execute { detector?.close(); detector = null }
        } catch (_: RejectedExecutionException) { }
        executor.shutdown()
    }
}