package com.example.gastrack.data.remote.dto

data class CategoryDto(
    val CategoryID: Int,
    val Category: String
)

data class BrandDto(
    val BrandID: Int,
    val Brand: String
)

data class CreateProductRequest(
    val productName: String,
    val categoryId: Int,
    val brandId: Int,
    val supplierId: Int,
    val unit: String,
    val unitPrice: Double,
    val costPrice: Double,
    val reorderLevel: Int
)

data class UpdateProductRequest(
    val productName: String,
    val categoryId: Int,
    val brandId: Int,
    val supplierId: Int,
    val unit: String,
    val unitPrice: Double,
    val costPrice: Double,
    val reorderLevel: Int,
    val status: String
)

data class CreateProductResponse(val productId: Int)