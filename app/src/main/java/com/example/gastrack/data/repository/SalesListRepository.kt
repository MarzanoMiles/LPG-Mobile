package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.SalesListItemDto
import com.example.gastrack.data.remote.dto.SalesStatsDto

class SalesListRepository {
    private val api = RetrofitClient.instance

    suspend fun getSalesList(): ApiResult<List<SalesListItemDto>> {
        return try {
            val response = api.getSalesList()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load sales records")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getStats(): ApiResult<SalesStatsDto> {
        return try {
            val response = api.getSalesStats()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load sales stats")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}