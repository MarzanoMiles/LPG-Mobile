package com.example.gastrack.ui.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared row for label/value detail displays used across receipt and
 * confirmation dialogs (POS, Sales, Restocking, Suppliers, etc.).
 * Single source of truth — do not redeclare this in individual screen files.
 */
@Composable
fun DetailValueRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

/**
 * Shared row for the payment breakdown dialogs (POS PayPopup, etc.).
 * Single source of truth — do not redeclare this in individual screen files.
 */
@Composable
fun PaymentDetailRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 16.sp, color = color, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

/**
 * Shared row for cart/order summary displays (Cart, Checkout).
 * Single source of truth — do not redeclare this in individual screen files.
 */
@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = com.example.gastrack.ui.theme.TextGray, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = com.example.gastrack.ui.theme.TextDark)
    }
}