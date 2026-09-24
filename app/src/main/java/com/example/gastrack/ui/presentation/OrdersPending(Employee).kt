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
fun OrdersPendingTab(
    onNavigateToMaps: (String) -> Unit,
    onStartDelivery: (String) -> Unit,
) {
    val pendingOrders = listOf(
        EmployeeOrder("ORD-2023-081", "10:00 AM - 12:00 PM", "Juan Dela Cruz", "1 × 11kg Standard", "45 Mabini St, Brgy. San Jose", "Pending"),
        EmployeeOrder("ORD-2023-085", "1:00 PM - 3:00 PM", "Maria Clara Res.", "2 × 50kg Commercial", "45 Mabini St, Brgy. San Jose", "Pending"),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(pendingOrders) { order ->
            EmployeeOrderCard(
                order = order,
                onViewMaps = { onNavigateToMaps(order.id) },
                onStartDelivery = { onStartDelivery(order.id) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrdersPendingTabPreview() {
    OrdersPendingTab(onNavigateToMaps = {}, onStartDelivery = {})
}
