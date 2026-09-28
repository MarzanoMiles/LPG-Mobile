package com.example.gastrack.data.remote.dto

data class RestockRecommendationDto(
    val RestockID: Int,
    val ProductID: Int,
    val SupplierID: Int,
    val StockOnHand: Int,
    val PredictedDemand: Int,
    val RecommendedQuantity: Int,
    val ForecastDate: String,
    val Status: String,
    val ProductName: String,
    val SupplierName: String
)

data class RestockGenerateResponse(val createdCount: Int, val ids: List<Int>)

data class ConvertRestockResponse(val purchaseOrderId: Int, val poNo: String)

data class UpdatePurchaseOrderStatusRequest(val status: String)