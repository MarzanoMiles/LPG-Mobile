package com.example.gastrack.data.remote.dto

data class DataActivityLogDto(
    val logId: Int,
    val userId: Int,
    val activityType: String?, // "Export" | "Generate Report"
    val dataType: String?,
    val fileName: String?,
    val fileFormat: String?,
    val dateFrom: String?,
    val dateTo: String?,
    val status: String?, // "Successful" | "Failed"
    val activityDate: String,
    val firstName: String,
    val lastName: String
)