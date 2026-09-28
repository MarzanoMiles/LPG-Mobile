package com.example.gastrack.data.remote.dto

data class SalesListItemDto(
    val SaleID: Int,
    val OrderID: Int,
    val CustomerID: Int,
    val UserID: Int,
    val SaleNo: String,
    val SalesDiscount: String,
    val TotalAmount: String,
    val SaleDate: String,
    val CustomerName: String,
    val FirstName: String,
    val LastName: String
)

data class SalesStatsDto(
    val todaysSalesTotal: Double,
    val todaysTransactionCount: Int,
    val averageOrderValue: Double,
    val peakHourLabel: String
)