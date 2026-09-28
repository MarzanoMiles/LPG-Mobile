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
    suspend fun getProducts(
        @Query("status") status: String? = "Active",
        @Query("supplierId") supplierId: Int? = null
    ): Response<List<ProductDto>>

    @POST("products")
    suspend fun createProduct(@Body body: CreateProductRequest): Response<CreateProductResponse>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") productId: Int, @Body body: UpdateProductRequest): Response<Unit>

    @DELETE("products/{id}")
    suspend fun deactivateProduct(@Path("id") productId: Int): Response<Unit>

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("brands")
    suspend fun getBrands(): Response<List<BrandDto>>

    @POST("sales/checkout")
    suspend fun checkout(@Body body: CheckoutRequest): Response<CheckoutResponse>

    @GET("sales")
    suspend fun getSalesList(): Response<List<SalesListItemDto>>

    @GET("sales/stats")
    suspend fun getSalesStats(): Response<SalesStatsDto>

    @GET("sales/{id}")
    suspend fun getSaleDetail(@Path("id") saleId: Int): Response<SaleDetailDto>

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

    @GET("orders")
    suspend fun getOrders(): Response<List<OrderDto>>

    @GET("customers/me/profile")
    suspend fun getProfile(): Response<CustomerProfileDto>

    @PUT("customers/me/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<Unit>

    @GET("inventory")
    suspend fun getInventory(@Query("warehouseId") warehouseId: Int? = null): Response<List<InventoryDto>>

    @POST("inventory/transactions")
    suspend fun createStockTransaction(@Body body: StockTransactionRequest): Response<StockTransactionResponse>

    @GET("restock")
    suspend fun getRestockRecommendations(): Response<List<RestockRecommendationDto>>

    @POST("restock/generate")
    suspend fun generateRestockRecommendations(): Response<RestockGenerateResponse>

    @GET("suppliers")
    suspend fun getSuppliers(): Response<List<SupplierDto>>

    @POST("suppliers")
    suspend fun createSupplier(@Body body: CreateSupplierRequest): Response<CreateSupplierResponse>

    @PUT("suppliers/{id}")
    suspend fun updateSupplier(@Path("id") supplierId: Int, @Body body: UpdateSupplierRequest): Response<Unit>

    @DELETE("suppliers/{id}")
    suspend fun deleteSupplier(@Path("id") supplierId: Int): Response<Unit>

    @GET("purchase-orders")
    suspend fun getPurchaseOrders(): Response<List<PurchaseOrderDto>>

    @POST("purchase-orders/from-restock/{restockId}")
    suspend fun convertRestockToPO(@Path("restockId") restockId: Int): Response<ConvertRestockResponse>

    @PUT("purchase-orders/{id}/status")
    suspend fun updatePurchaseOrderStatus(@Path("id") purchaseOrderId: Int, @Body body: UpdatePurchaseOrderStatusRequest): Response<Unit>

    @GET("customers")
    suspend fun searchCustomers(@Query("search") search: String? = null): Response<List<CustomerDto>>

    @GET("customers/walk-in")
    suspend fun getWalkInCustomer(): Response<CustomerDto>

    @POST("customers")
    suspend fun createCustomer(@Body body: CreateCustomerRequest): Response<CreateCustomerResponse>

    @GET("deliveries")
    suspend fun getDeliveries(): Response<List<DeliveryDto>>

    @PUT("deliveries/{id}/status")
    suspend fun updateDeliveryStatus(@Path("id") deliveryId: Int, @Body body: UpdateDeliveryStatusRequest): Response<Unit>

    @GET("users")
    suspend fun getUsers(): Response<List<UserListItemDto>>

    @POST("users")
    suspend fun createUser(@Body body: CreateUserRequest): Response<CreateUserResponse>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body body: UpdateUserRequest): Response<Unit>

    @DELETE("users/{id}")
    suspend fun deactivateUser(@Path("id") userId: Int): Response<Unit>

    @GET("users/roles")
    suspend fun getRoles(): Response<List<RoleDto>>

    @GET("users/activity-log")
    suspend fun getActivityLog(): Response<List<ActivityLogDto>>

    @GET("warehouses")
    suspend fun getWarehouses(): Response<List<WarehouseDto>>
}