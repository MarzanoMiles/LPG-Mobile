package com.example.gastrack.data.repository

import com.example.gastrack.data.remote.RetrofitClient
import com.example.gastrack.data.remote.dto.*

class ComplianceRepository {
    private val api = RetrofitClient.instance

    suspend fun getReports(): ApiResult<List<ComplianceReportDto>> {
        return try {
            val response = api.getComplianceReports()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load compliance reports")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSummary(): ApiResult<ComplianceSummaryDto> {
        return try {
            val response = api.getComplianceSummary()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load summary")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun createReport(request: CreateComplianceReportRequest): ApiResult<CreateComplianceReportResponse> {
        return try {
            val response = api.createComplianceReport(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to schedule report")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun generateReport(reportId: Int): ApiResult<GenerateReportResponse> {
        return try {
            val response = api.generateComplianceReport(reportId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to generate report")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun submitReport(reportId: Int): ApiResult<Unit> {
        return try {
            val response = api.submitComplianceReport(reportId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to submit report")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteReport(reportId: Int): ApiResult<Unit> {
        return try {
            val response = api.deleteComplianceReport(reportId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to delete report")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}