package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.ProductDto

class ProductRepository {
    private val api = RetrofitClient.instance

    suspend fun getProducts(): ApiResult<List<ProductDto>> {
        return try {
            val response = api.getProducts()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load products")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}