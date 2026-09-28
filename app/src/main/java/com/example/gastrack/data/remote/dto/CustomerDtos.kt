package com.example.gastrack.data.remote.dto

data class CustomerDto(
    val CustomerID: Int,
    val CustomerType: String,
    val CustomerName: String,
    val ContactNo: String,
    val Address: String,
    val Status: String
)

data class CreateCustomerRequest(
    val customerType: String,
    val customerName: String,
    val contactNo: String,
    val address: String
)

data class CreateCustomerResponse(val customerId: Int)