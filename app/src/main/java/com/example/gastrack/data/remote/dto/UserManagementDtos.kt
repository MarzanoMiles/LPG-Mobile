package com.example.gastrack.data.remote.dto

data class UserListItemDto(
    val UserID: Int,
    val FirstName: String,
    val LastName: String,
    val Email: String,
    val Status: String,
    // MySQL JSON columns come back as parsed objects, not strings, so `Any?` is the safe type here.
    val ModuleAccess: Any?,
    val RoleID: Int,
    val RoleName: String,
    val WarehouseID: Int?,
    val WarehouseName: String?
)

data class RoleDto(
    val RoleID: Int,
    val RoleName: String
)

data class WarehouseDto(
    val WarehouseID: Int,
    val WarehouseName: String,
    val Location: String?,
    val Status: String?
)

data class ActivityLogDto(
    val ActivityID: Int,
    val UserID: Int,
    val ActivityType: String,
    val Module: String,
    val RecordID: Int?,
    val Description: String?,
    val ActivityDate: String,
    val FirstName: String,
    val LastName: String,
    val RoleName: String
)

data class CreateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val roleId: Int,
    val warehouseId: Int? = null
)

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val roleId: Int,
    val warehouseId: Int? = null,
    val status: String? = null
)

data class CreateUserResponse(val userId: Int)