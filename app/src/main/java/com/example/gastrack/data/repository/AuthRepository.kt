package com.example.gastrack.data.repository

import com.example.gastrack.data.local.TokenManager
import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.*

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

class AuthRepository(private val tokenManager: TokenManager) {
    private val api = RetrofitClient.instance

    suspend fun employeeLogin(email: String, password: String): ApiResult<EmployeeLoginResponse> {
        return try {
            val response = api.employeeLogin(EmployeeLoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveSession(
                    token = body.token,
                    userType = "employee",
                    userName = "${body.user.firstName} ${body.user.lastName}",
                    userId = body.user.userId,
                    warehouseId = body.user.warehouseId
                )
                RetrofitClient.setAuthToken(body.token)
                ApiResult.Success(body)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun customerLogin(email: String, password: String): ApiResult<CustomerLoginResponse> {
        return try {
            val response = api.customerLogin(CustomerLoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveSession(
                    token = body.token,
                    userType = "customer",
                    userName = body.customer.fullName,
                    userId = body.customer.customerId
                )
                RetrofitClient.setAuthToken(body.token)
                ApiResult.Success(body)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun customerRegister(request: CustomerRegisterRequest): ApiResult<CustomerRegisterResponse> {
        return try {
            val response = api.customerRegister(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveSession(
                    token = body.token,
                    userType = "customer",
                    userName = request.fullName,
                    userId = body.customerId
                )
                RetrofitClient.setAuthToken(body.token)
                ApiResult.Success(body)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Registration failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() {
        tokenManager.clearSession()
        RetrofitClient.setAuthToken(null)
    }

    suspend fun restoreSession(): Boolean {
        val token = tokenManager.getToken()
        return if (token != null) {
            RetrofitClient.setAuthToken(token)
            true
        } else false
    }
}