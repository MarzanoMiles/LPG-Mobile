package com.example.gastrack.data.remote.dto

data class ComplianceReportDto(
    val id: Int,
    val name: String,
    val type: String,
    val period: String,
    val periodStart: String,
    val periodEnd: String,
    val dueDate: String,
    val status: String, // "Upcoming" | "Due Soon" | "Overdue" | "Submitted"
    val submittedAt: String?,
    val fileName: String?
)

data class ComplianceSummaryDto(
    val dueSoon: Int,
    val overdue: Int,
    val submittedThisMonth: Int,
    val total: Int
)

data class CreateComplianceReportRequest(
    val reportName: String,
    val reportType: String,
    val periodLabel: String,
    val periodStart: String,
    val periodEnd: String,
    val dueDate: String
)

data class CreateComplianceReportResponse(val id: Int)

data class GenerateReportResponse(val fileName: String, val csv: String)