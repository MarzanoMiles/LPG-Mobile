package com.example.gastrack.data.remote

import com.example.gastrack.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/employee/login")
    suspend fun employeeLogin(@Body body: EmployeeLoginRequest): Response<EmployeeLoginResponse>

    @POST("auth/employee/register")
    suspend fun employeeRegister(@Body body: EmployeeRegisterRequest): Response<EmployeeRegisterResponse>

    @POST("auth/customer/login")
    suspend fun customerLogin(@Body body: CustomerLoginRequest): Response<CustomerLoginResponse>

    @POST("auth/customer/register")
    suspend fun customerRegister(@Body body: CustomerRegisterRequest): Response<CustomerRegisterResponse>

    @GET("products")
    suspend fun getProducts(@Query("status") status: String? = "Active"): Response<List<ProductDto>>

    @POST("sales/checkout")
    suspend fun checkout(@Body body: CheckoutRequest): Response<CheckoutResponse>

    @GET("dashboard/employee")
    suspend fun getEmployeeDashboard(): Response<EmployeeDashboardResponse>

    @GET("dashboard/customer")
    suspend fun getCustomerDashboard(): Response<CustomerDashboardResponse>

    @GET("addresses")
    suspend fun getAddresses(): Response<List<AddressDto>>

    @GET("addresses/primary")
    suspend fun getPrimaryAddress(): Response<AddressDto?>

    @POST("addresses")
    suspend fun createAddress(@Body body: CreateAddressRequest): Response<CreateAddressResponse>

    @PUT("addresses/{id}/set-primary")
    suspend fun setPrimaryAddress(@Path("id") addressId: Int): Response<Unit>

    @DELETE("addresses/{id}")
    suspend fun deleteAddress(@Path("id") addressId: Int): Response<Unit>
}