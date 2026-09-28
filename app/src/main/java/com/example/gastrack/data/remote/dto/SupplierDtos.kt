package com.example.gastrack.data.remote.dto

data class SupplierDto(
    val SupplierID: Int,
    val SupplierName: String,
    val ContactPerson: String?,
    val Email: String?,
    val Address: String?,
    val Contact: String?,
    val LeadTimeDays: Int,
    val Status: String
)

data class CreateSupplierRequest(
    val supplierName: String,
    val contactPerson: String?,
    val email: String?,
    val address: String?,
    val contact: String?,
    val leadTimeDays: Int
)

data class UpdateSupplierRequest(
    val supplierName: String,
    val contactPerson: String?,
    val email: String?,
    val address: String?,
    val contact: String?,
    val leadTimeDays: Int,
    val status: String
)

data class CreateSupplierResponse(val supplierId: Int)