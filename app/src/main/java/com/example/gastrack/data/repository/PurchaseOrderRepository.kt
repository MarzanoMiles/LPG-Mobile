package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.ConvertRestockResponse
import com.example.gastrack.data.remote.dto.PurchaseOrderDto
import com.example.gastrack.data.remote.dto.UpdatePurchaseOrderStatusRequest

class PurchaseOrderRepository {
    private val api = RetrofitClient.instance

    suspend fun getPurchaseOrders(): ApiResult<List<PurchaseOrderDto>> {
        return try {
            val response = api.getPurchaseOrders()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load purchase orders")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun convertRestockToPO(restockId: Int): ApiResult<ConvertRestockResponse> {
        return try {
            val response = api.convertRestockToPO(restockId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to generate purchase order")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateStatus(purchaseOrderId: Int, status: String): ApiResult<Unit> {
        return try {
            val response = api.updatePurchaseOrderStatus(purchaseOrderId, UpdatePurchaseOrderStatusRequest(status))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update purchase order")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}