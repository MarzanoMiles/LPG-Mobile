package com.example.gastrack.data.remote.dto

data class EmployeeDashboardResponse(
    val todaysOrders: Int,
    val delivered: Int,
    val todaysSalesTotal: Double,
    val lowStock: List<LowStockItem>
)
data class LowStockItem(
    val InventoryID: Int,
    val ProductName: String,
    val StockOnHand: Int,
    val ReorderLevel: Int
)

data class CustomerDashboardResponse(val daysSinceLastOrder: Int?)