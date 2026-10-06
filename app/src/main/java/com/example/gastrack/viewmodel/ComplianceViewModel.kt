package com.example.gastrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.ComplianceReportDto
import com.example.gastrack.data.remote.dto.ComplianceSummaryDto
import com.example.gastrack.data.remote.dto.CreateComplianceReportRequest
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.ComplianceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class ComplianceUiState {
    object Loading : ComplianceUiState()
    data class Success(val reports: List<ComplianceReportDto>) : ComplianceUiState()
    data class Error(val message: String) : ComplianceUiState()
}

sealed class ComplianceSummaryUiState {
    object Loading : ComplianceSummaryUiState()
    data class Success(val summary: ComplianceSummaryDto) : ComplianceSummaryUiState()
    data class Error(val message: String) : ComplianceSummaryUiState()
}

class ComplianceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ComplianceRepository()

    private val _uiState = MutableStateFlow<ComplianceUiState>(ComplianceUiState.Loading)
    val uiState: StateFlow<ComplianceUiState> = _uiState

    private val _summaryState = MutableStateFlow<ComplianceSummaryUiState>(ComplianceSummaryUiState.Loading)
    val summaryState: StateFlow<ComplianceSummaryUiState> = _summaryState

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    // Tracks which report is mid-generate/submit so its card can show a spinner.
    private val _busyReportId = MutableStateFlow<Int?>(null)
    val busyReportId: StateFlow<Int?> = _busyReportId

    init {
        loadReports()
        loadSummary()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = ComplianceUiState.Loading
            when (val result = repository.getReports()) {
                is ApiResult.Success -> _uiState.value = ComplianceUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = ComplianceUiState.Error(result.message)
            }
        }
    }

    fun loadSummary() {
        viewModelScope.launch {
            when (val result = repository.getSummary()) {
                is ApiResult.Success -> _summaryState.value = ComplianceSummaryUiState.Success(result.data)
                is ApiResult.Error -> _summaryState.value = ComplianceSummaryUiState.Error(result.message)
            }
        }
    }

    fun createReport(
        reportName: String,
        reportType: String,
        periodLabel: String,
        periodStart: String,
        periodEnd: String,
        dueDate: String
    ) {
        viewModelScope.launch {
            val request = CreateComplianceReportRequest(reportName, reportType, periodLabel, periodStart, periodEnd, dueDate)
            when (val result = repository.createReport(request)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Report scheduled"
                    loadReports()
                    loadSummary()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun generateReport(reportId: Int) {
        viewModelScope.launch {
            _busyReportId.value = reportId
            when (val result = repository.generateReport(reportId)) {
                is ApiResult.Success -> {
                    try {
                        val dir = getApplication<Application>().getExternalFilesDir(null)
                        val file = File(dir, result.data.fileName)
                        file.writeText(result.data.csv)
                        _actionMessage.value = "Saved to ${file.absolutePath}"
                    } catch (e: Exception) {
                        _actionMessage.value = "Generated, but couldn't save file: ${e.message}"
                    }
                    loadReports()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
            _busyReportId.value = null
        }
    }

    fun submitReport(reportId: Int) {
        viewModelScope.launch {
            _busyReportId.value = reportId
            when (val result = repository.submitReport(reportId)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Report marked as submitted"
                    loadReports()
                    loadSummary()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
            _busyReportId.value = null
        }
    }

    fun deleteReport(reportId: Int) {
        viewModelScope.launch {
            when (val result = repository.deleteReport(reportId)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Report deleted"
                    loadReports()
                    loadSummary()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}