package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.DataActivityLogDto
import okhttp3.ResponseBody

class DataActivityRepository {
    private val api = RetrofitClient.instance

    suspend fun getLogs(): ApiResult<List<DataActivityLogDto>> {
        return try {
            val response = api.getDataActivityLog()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load activity log")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun exportData(dataType: String, range: String): ApiResult<ResponseBody> {
        return try {
            val response = api.exportData(dataType, range)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Export failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}