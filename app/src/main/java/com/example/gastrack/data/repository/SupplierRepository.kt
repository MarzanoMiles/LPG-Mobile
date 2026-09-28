package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.*

class SupplierRepository {
    private val api = RetrofitClient.instance

    suspend fun getSuppliers(): ApiResult<List<SupplierDto>> {
        return try {
            val response = api.getSuppliers()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load suppliers")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun createSupplier(request: CreateSupplierRequest): ApiResult<CreateSupplierResponse> {
        return try {
            val response = api.createSupplier(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to add supplier")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateSupplier(id: Int, request: UpdateSupplierRequest): ApiResult<Unit> {
        return try {
            val response = api.updateSupplier(id, request)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update supplier")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun deactivateSupplier(id: Int): ApiResult<Unit> {
        return try {
            val response = api.deleteSupplier(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to remove supplier")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}