package com.example.gastrack.data.remote.dto

data class InventoryDto(
    val InventoryID: Int,
    val WarehouseID: Int,
    val ProductID: Int,
    val StockOnHand: Int,
    val ProductName: String,
    val Unit: String,
    val ReorderLevel: Int,
    val UnitPrice: String?,
    val WarehouseName: String
)

data class StockTransactionRequest(
    val warehouseId: Int,
    val productId: Int,
    val transactionType: String,
    val quantity: Int,
    val reason: String? = null,
    val referenceNo: String? = null,
    val remarks: String? = null
)

data class StockTransactionResponse(val message: String, val stockOnHand: Int)