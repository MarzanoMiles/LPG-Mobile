package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.EmployeeDashboardResponse

class DashboardRepository {
    private val api = RetrofitClient.instance

    suspend fun getEmployeeDashboard(): ApiResult<EmployeeDashboardResponse> {
        return try {
            val response = api.getEmployeeDashboard()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load dashboard")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}