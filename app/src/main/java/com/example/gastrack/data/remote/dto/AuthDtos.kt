package com.example.gastrack.data.remote.dto

data class EmployeeLoginRequest(val email: String, val password: String)
data class EmployeeLoginResponse(val token: String, val user: EmployeeUser)
data class EmployeeUser(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val companyId: Int,
    val warehouseId: Int?,
    val moduleAccess: Any?
)

data class CustomerLoginRequest(val email: String, val password: String)
data class CustomerLoginResponse(val token: String, val customer: CustomerUser)
data class CustomerUser(
    val customerId: Int,
    val fullName: String,
    val contactNo: String,
    val address: String,
    val customerType: String
)

data class CustomerRegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val contactNo: String,
    val address: String,
    val customerType: String
)
data class CustomerRegisterResponse(val token: String, val customerId: Int)

data class EmployeeRegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val roleId: Int? = null,
    val warehouseId: Int? = null,
    val companyId: Int? = null
)
data class EmployeeRegisterResponse(val token: String, val userId: Int)