package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.CreateCustomerRequest
import com.example.gastrack.data.remote.dto.CreateCustomerResponse
import com.example.gastrack.data.remote.dto.CustomerDto

class CustomerRepository {
    private val api = RetrofitClient.instance

    suspend fun searchCustomers(query: String?): ApiResult<List<CustomerDto>> {
        return try {
            val response = api.searchCustomers(query)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load customers")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getWalkInCustomer(): ApiResult<CustomerDto> {
        return try {
            val response = api.getWalkInCustomer()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load walk-in customer")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun createCustomer(name: String, phone: String, address: String): ApiResult<CreateCustomerResponse> {
        return try {
            val response = api.createCustomer(CreateCustomerRequest("Walk-in", name, phone, address))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to add customer")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}