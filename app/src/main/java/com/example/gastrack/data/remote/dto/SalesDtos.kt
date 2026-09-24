package com.example.gastrack.data.remote.dto

data class CheckoutItem(val productId: Int, val quantity: Int)

data class CheckoutRequest(
    val customerId: Int,
    val warehouseId: Int,
    val orderType: String, // "Walk-in" | "Delivery"
    val items: List<CheckoutItem>,
    val paymentMethod: String,
    val amountPaid: Double? = null,
    val salesDiscount: Double? = null,
    val deliveryAddress: String? = null,
    val deliveryCharge: Double? = null
)

data class CheckoutResponse(
    val orderId: Int,
    val orderNo: String,
    val saleId: Int,
    val saleNo: String,
    val totalAmount: Double
)