package com.example.gastrack.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OrdersInTransitTab() {
    val inTransitOrders = listOf(
        EmployeeOrder("ORD-2023-081", "10:00 AM - 12:00 PM", "Juan Dela Cruz", "1 × 11kg Standard", "45 Mabini St, Brgy. San Jose", "In Transit"),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(inTransitOrders) { order ->
            EmployeeOrderCard(
                order = order,
                badgeText = "Pending", // Image shows "Pending" badge even on Transit tab
            ) { /* Handle In Transit Click */ }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrdersInTransitTabPreview() {
    OrdersInTransitTab()
}
