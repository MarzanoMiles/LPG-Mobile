package com.example.gastrack.data.remote.dto

data class AddressDto(
    val AddressID: Int,
    val CustomerID: Int,
    val Label: String,
    val AddressLine: String,
    val AddressType: String,
    val IsPrimary: Int, // MySQL tinyint(1): 0 or 1
    val CreatedAt: String
)

data class CreateAddressRequest(
    val label: String,
    val addressLine: String,
    val addressType: String
)

data class CreateAddressResponse(val addressId: Int, val isPrimary: Boolean)