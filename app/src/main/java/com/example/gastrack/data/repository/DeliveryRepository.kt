package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.DeliveryDto
import com.example.gastrack.data.remote.dto.UpdateDeliveryStatusRequest

class DeliveryRepository {
    private val api = RetrofitClient.instance

    suspend fun getDeliveries(): ApiResult<List<DeliveryDto>> {
        return try {
            val response = api.getDeliveries()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load deliveries")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateStatus(deliveryId: Int, status: String): ApiResult<Unit> {
        return try {
            val response = api.updateDeliveryStatus(deliveryId, UpdateDeliveryStatusRequest(status))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update delivery")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}