package com.example.gastrack.util

import com.example.gastrack.R

fun resolveProductImageRes(productName: String): Int = when {
    productName.contains("50", ignoreCase = true) -> R.drawable.tank_50kg
    productName.contains("11", ignoreCase = true) -> R.drawable.tank_11kg
    productName.contains("22", ignoreCase = true) -> R.drawable.tank_11kg
    else -> R.drawable.tank_2_7kg
}