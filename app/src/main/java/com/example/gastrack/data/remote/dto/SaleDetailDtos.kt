package com.example.gastrack.data.remote.dto

data class SaleDetailItemDto(
    val OrderID: Int,
    val ProductID: Int,
    val ProductName: String,
    val Quantity: Int,
    val UnitPrice: String,
    val Subtotal: String
)

data class SaleDetailPaymentDto(
    val PaymentID: Int,
    val PaymentType: String,
    val SaleID: Int,
    val PaymentMethod: String,
    val AmountPaid: String,
    val PaymentDate: String
)

data class SaleDetailDto(
    val SaleID: Int,
    val OrderID: Int,
    val CustomerID: Int,
    val UserID: Int,
    val SaleNo: String,
    val SalesDiscount: String,
    val TotalAmount: String,
    val SaleDate: String,
    val items: List<SaleDetailItemDto>,
    val payments: List<SaleDetailPaymentDto>
)