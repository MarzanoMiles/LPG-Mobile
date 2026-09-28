package com.example.gastrack.data.remote.dto

data class PurchaseOrderDto(
    val PurchaseOrderID: Int,
    val SupplierID: Int,
    val RestockID: Int?,
    val CreatedByUserID: Int,
    val PONo: String,
    val OrderDate: String,
    val Status: String,
    val TotalAmount: String,
    val SupplierName: String
)