package com.example.gastrack.ui.presentation

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private const val REG_TAG = "HazardModels"

private fun normLabel(label: String) = label.replace('_', ' ').trim().lowercase()

/** Everything the app needs to know about one TensorFlow Lite model. */
class HazardModelSpec(
    val name: String,
    val modelAsset: String,
    val labelsAsset: String,
    val threshold: Float,
    /** true when the model has no background class, so it must not claim "no hazard" on its own */
    val secondary: Boolean,
    /** normalized label -> text to show; null = use the built-in HazardCatalog */
    private val hazards: Map<String, String>?
) {
    fun hazardNameFor(label: String): String? =
        if (hazards != null) hazards[normLabel(label)] else HazardCatalog.hazardNameFor(label)
}

/** Finds every model in assets/hazard_models and reads its optional <name>.json settings. */
object HazardModelRegistry {
    const val FOLDER = "hazard_models"

    fun discover(context: Context): List<HazardModelSpec> {
        val specs = ArrayList<HazardModelSpec>()
        val files: List<String> = try {
            context.assets.list(FOLDER)?.toList() ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }

        for (modelFile in files.filter { it.endsWith(".tflite", ignoreCase = true) }.sorted()) {
            val name = modelFile.substringBeforeLast('.')
            val labelsFile = listOf("$name.txt", "${name}_labels.txt", "labels_$name.txt").firstOrNull { it in files }
            if (labelsFile == null) {
                Log.w(REG_TAG, "Skipped $modelFile: no labels file ($name.txt, ${name}_labels.txt or labels_$name.txt)")
                continue
            }
            val cfg = readConfig(context, if ("$name.json" in files) "$FOLDER/$name.json" else null)
            if (cfg != null && !cfg.optBoolean("enabled", true)) {
                Log.i(REG_TAG, "Skipped $modelFile: disabled in $name.json")
                continue
            }
            specs += build(context, name, "$FOLDER/$modelFile", "$FOLDER/$labelsFile", cfg)
        }

        if (specs.isEmpty()) {
            // Legacy layout: kitchen.tflite + kitchen.txt in the assets root
            val root = try { context.assets.list("")?.toList() ?: emptyList() } catch (e: IOException) { emptyList() }
            if ("hazard_models/kitchen.tflite" in root && "hazard_models/kitchen.txt" in root) {
                Log.i(REG_TAG, "No models in assets/$FOLDER; using the legacy kitchen.tflite + kitchen.txt")
                specs += build(context, "model_unquant", "hazard_models/kitchen.tflite", "hazard_models/kitchen.txt", null)
            }
        }

        Log.i(REG_TAG, "Found ${specs.size} model(s): " + specs.joinToString { it.name })
        return specs
    }

    private fun build(context: Context, name: String, modelPath: String, labelsPath: String, cfg: JSONObject?): HazardModelSpec {
        val labels = readLabels(context, labelsPath)
        val hasBackground = labels.any { it.contains("background", ignoreCase = true) }
        val configured = cfg?.optDouble("threshold", Double.NaN) ?: Double.NaN
        val threshold = if (configured.isNaN()) {
            if (hasBackground) 0.75f else 0.90f
        } else {
            configured.toFloat()
        }
        Log.i(
            REG_TAG,
            "$name: ${labels.size} labels, hasBackground=$hasBackground, threshold=$threshold, " +
                    "customHazards=${cfg?.has("hazards") == true}"
        )
        return HazardModelSpec(name, modelPath, labelsPath, threshold, !hasBackground, parseHazards(cfg))
    }

    private fun readConfig(context: Context, path: String?): JSONObject? {
        if (path == null) return null
        return try {
            JSONObject(context.assets.open(path).bufferedReader().use { it.readText() })
        } catch (t: Throwable) {
            Log.w(REG_TAG, "Could not read $path: ${t.message}")
            null
        }
    }

    private fun readLabels(context: Context, path: String): List<String> = try {
        context.assets.open(path).bufferedReader().readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.replace(Regex("^\\d+\\s+"), "") } // "2 Candle" -> "Candle"
    } catch (t: Throwable) {
        emptyList()
    }

    private fun parseHazards(cfg: JSONObject?): Map<String, String>? {
        val h = cfg?.opt("hazards") ?: return null
        val out = HashMap<String, String>()
        when (h) {
            is JSONObject -> for (k in h.keys()) out[normLabel(k)] = h.optString(k, k.replace('_', ' '))
            is JSONArray -> for (i in 0 until h.length()) {
                val k = h.optString(i)
                if (k.isNotEmpty()) out[normLabel(k)] = k.replace('_', ' ')
            }
        }
        return out
    }
}