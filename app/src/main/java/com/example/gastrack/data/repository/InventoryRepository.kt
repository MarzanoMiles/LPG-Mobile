package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.InventoryDto
import com.example.gastrack.data.remote.dto.StockTransactionRequest
import com.example.gastrack.data.remote.dto.StockTransactionResponse

class InventoryRepository {
    private val api = RetrofitClient.instance

    suspend fun getInventory(warehouseId: Int?): ApiResult<List<InventoryDto>> {
        return try {
            val response = api.getInventory(warehouseId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load inventory")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun submitTransaction(request: StockTransactionRequest): ApiResult<StockTransactionResponse> {
        return try {
            val response = api.createStockTransaction(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to record transaction")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}