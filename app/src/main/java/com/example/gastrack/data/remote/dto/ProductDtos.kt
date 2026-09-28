package com.example.gastrack.data.remote.dto

data class ProductDto(
    val ProductID: Int,
    val ProductName: String,
    val CategoryID: Int,
    val BrandID: Int,
    val SupplierID: Int,
    val Unit: String,
    val UnitPrice: String,
    val CostPrice: String,
    val ReorderLevel: Int,
    val ImageURL: String?,
    val Status: String,
    val Category: String,
    val Brand: String,
    val SupplierName: String,
    val SupplierLeadTimeDays: Int?
)