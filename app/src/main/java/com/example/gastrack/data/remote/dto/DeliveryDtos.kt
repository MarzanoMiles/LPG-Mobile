package com.example.gastrack.data.remote.dto

data class DeliveryDto(
    val DeliveryID: Int,
    val SaleID: Int,
    val OrderID: Int,
    val DRNo: String,
    val DeliveryCharge: String,
    val DeliveryAddress: String,
    val DeliveryStatus: String, // "Pending" | "In Transit" | "Delivered" | "Cancelled"
    val DeliveryDate: String?,
    val DeliveredByUserID: Int?,
    val SaleNo: String,
    val TotalAmount: String,
    val SaleDate: String,
    val CustomerName: String,
    val ItemCount: Int,
    val ItemSummary: String?
)

data class UpdateDeliveryStatusRequest(val deliveryStatus: String)