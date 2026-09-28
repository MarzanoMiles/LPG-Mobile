package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.RestockGenerateResponse
import com.example.gastrack.data.remote.dto.RestockRecommendationDto

class RestockRepository {
    private val api = RetrofitClient.instance

    suspend fun getRecommendations(): ApiResult<List<RestockRecommendationDto>> {
        return try {
            val response = api.getRestockRecommendations()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load recommendations")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun generateRecommendations(): ApiResult<RestockGenerateResponse> {
        return try {
            val response = api.generateRestockRecommendations()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to generate recommendations")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}