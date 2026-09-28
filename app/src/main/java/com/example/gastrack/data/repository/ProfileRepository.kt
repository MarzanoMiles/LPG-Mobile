package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.CustomerProfileDto
import com.example.gastrack.data.remote.dto.UpdateProfileRequest

class ProfileRepository {
    private val api = RetrofitClient.instance

    suspend fun getProfile(): ApiResult<CustomerProfileDto> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load profile")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<Unit> {
        return try {
            val response = api.updateProfile(request)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}