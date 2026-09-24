package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.CheckoutRequest
import com.example.gastrack.data.remote.dto.CheckoutResponse

class SalesRepository {
    private val api = RetrofitClient.instance

    suspend fun checkout(request: CheckoutRequest): ApiResult<CheckoutResponse> {
        return try {
            val response = api.checkout(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Checkout failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}