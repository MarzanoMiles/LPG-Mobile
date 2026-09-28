package com.example.gastrack.data.remote.dto

data class CustomerProfileDto(
    val CustomerID: Int,
    val CustomerType: String,
    val CustomerName: String,
    val ContactNo: String,
    val Address: String,
    val Status: String,
    val Email: String
)

data class UpdateProfileRequest(
    val customerName: String,
    val contactNo: String,
    val address: String,
    val customerType: String
)