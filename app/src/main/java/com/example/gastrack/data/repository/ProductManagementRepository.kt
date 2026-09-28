package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.*

class ProductManagementRepository {
    private val api = RetrofitClient.instance

    // status = null -> backend returns Active AND Inactive products (management view)
    suspend fun getAllProducts(): ApiResult<List<ProductDto>> {
        return try {
            val response = api.getProducts(status = null)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load products")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getCategories(): ApiResult<List<CategoryDto>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load categories")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getBrands(): ApiResult<List<BrandDto>> {
        return try {
            val response = api.getBrands()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load brands")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun createProduct(request: CreateProductRequest): ApiResult<CreateProductResponse> {
        return try {
            val response = api.createProduct(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to add product")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateProduct(id: Int, request: UpdateProductRequest): ApiResult<Unit> {
        return try {
            val response = api.updateProduct(id, request)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update product")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun deactivateProduct(id: Int): ApiResult<Unit> {
        return try {
            val response = api.deactivateProduct(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to deactivate product")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}