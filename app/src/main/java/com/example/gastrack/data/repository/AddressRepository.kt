package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.AddressDto
import com.example.gastrack.data.remote.dto.CreateAddressRequest
import com.example.gastrack.data.remote.dto.CreateAddressResponse

class AddressRepository {
    private val api = RetrofitClient.instance

    suspend fun getAddresses(): ApiResult<List<AddressDto>> {
        return try {
            val response = api.getAddresses()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load addresses")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getPrimaryAddress(): ApiResult<AddressDto?> {
        return try {
            val response = api.getPrimaryAddress()
            if (response.isSuccessful) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load primary address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun createAddress(label: String, addressLine: String, addressType: String): ApiResult<CreateAddressResponse> {
        return try {
            val response = api.createAddress(CreateAddressRequest(label, addressLine, addressType))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to add address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun setPrimary(addressId: Int): ApiResult<Unit> {
        return try {
            val response = api.setPrimaryAddress(addressId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update primary address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteAddress(addressId: Int): ApiResult<Unit> {
        return try {
            val response = api.deleteAddress(addressId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to delete address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}