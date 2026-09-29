package com.example.gastrack.ui.presentation

/**
 * Single source of truth for cylinder type -> GLB model.
 * [label] must match the UI labels used in ARChooseScreen.
 * [realHeightMeters] is the approximate real-world height; tune if a model looks off.
 */
enum class GasTankType(
    val label: String,
    val assetPath: String,
    val realHeightMeters: Float
) {
    GASUL_2_7KG("2.7kg Camping", "models/Gasul 2.7kg.glb", 0.28f),
    PETRON_11KG("11kg Standard", "models/petron_gas11kg.glb", 0.50f),
    PETRON_50KG("50kg Commercial", "models/petron_gas50kg.glb", 1.25f);

    companion object {
        fun fromUiLabel(label: String): GasTankType =
            entries.firstOrNull { it.label == label } ?: PETRON_11KG
    }
}