package com.example.gastrack.ui.presentation

enum class HazardState { SCANNING, NO_HAZARD, POTENTIAL_HAZARD, LOW_CONFIDENCE, ERROR }

data class HazardUiState(
    val state: HazardState,
    val objectName: String? = null,
    val confidence: Float = 0f,
    val message: String? = null
)

data class Prediction(
    val label: String,
    val confidence: Float,
    val scores: List<Pair<String, Float>>
)

/**
 * Which model labels count as potential hazards. Keys are matched against labels.txt
 * (case-insensitive, "_" treated as space). Labels not listed here never raise a warning.
 */
object HazardCatalog {
    private val hazards = mapOf(
        "candle" to "Candle",
        "match box" to "Match box",
        "rice cooker" to "Rice cooker",
        "cooking oil" to "Cooking oil"
    )

    private fun normalize(label: String) = label.replace('_', ' ').trim().lowercase()

    fun hazardNameFor(label: String): String? = hazards[normalize(label)]

    fun prettify(label: String) = label.replace('_', ' ').trim()
}

/**
 * Temporal smoothing so the warning doesn't flicker frame to frame.
 * - Enter POTENTIAL_HAZARD after [enterFrames] consecutive confident frames of the same hazard.
 * - Leave it only after [exitFrames] consecutive non-hazard frames.
 * - Predictions under [threshold] are never treated as reliable detections.
 * Not thread-safe: call from one thread (the analyzer's executor).
 */
class HazardSmoother(
    private val threshold: Float = 0.75f,
    private val enterFrames: Int = 3,
    private val exitFrames: Int = 4
) {
    private enum class Kind { HAZARD, CLEAR, LOW }

    private var current = HazardUiState(HazardState.SCANNING)
    private var streakLabel: String? = null
    private var hazardStreak = 0
    private var smoothedConf = 0f
    private var clearStreak = 0
    private var lowStreak = 0
    private var nonHazardStreak = 0

    fun update(p: Prediction): HazardUiState {
        val hazardName = HazardCatalog.hazardNameFor(p.label)
        val kind = when {
            p.confidence < threshold -> Kind.LOW
            hazardName != null -> Kind.HAZARD
            else -> Kind.CLEAR
        }

        when (kind) {
            Kind.HAZARD -> {
                clearStreak = 0; lowStreak = 0; nonHazardStreak = 0
                if (p.label == streakLabel) {
                    hazardStreak++
                    smoothedConf = 0.6f * smoothedConf + 0.4f * p.confidence
                } else {
                    streakLabel = p.label
                    hazardStreak = 1
                    smoothedConf = p.confidence
                }
                if (hazardStreak >= enterFrames) {
                    current = HazardUiState(HazardState.POTENTIAL_HAZARD, hazardName, smoothedConf)
                }
            }
            Kind.CLEAR -> {
                resetHazardStreak()
                lowStreak = 0; clearStreak++; nonHazardStreak++
                if (current.state == HazardState.POTENTIAL_HAZARD) {
                    if (nonHazardStreak >= exitFrames) current = HazardUiState(HazardState.NO_HAZARD)
                } else if (clearStreak >= enterFrames) {
                    current = HazardUiState(HazardState.NO_HAZARD)
                }
            }
            Kind.LOW -> {
                resetHazardStreak()
                clearStreak = 0; lowStreak++; nonHazardStreak++
                val low = HazardUiState(
                    HazardState.LOW_CONFIDENCE,
                    HazardCatalog.prettify(p.label),
                    p.confidence
                )
                if (current.state == HazardState.POTENTIAL_HAZARD) {
                    if (nonHazardStreak >= exitFrames) current = low
                } else if (lowStreak >= enterFrames) {
                    current = low
                }
            }
        }
        return current
    }

    private fun resetHazardStreak() {
        streakLabel = null; hazardStreak = 0; smoothedConf = 0f
    }
}