package com.example.gastrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.DataActivityLogDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.DataActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class DataLogUiState {
    object Loading : DataLogUiState()
    data class Success(val logs: List<DataActivityLogDto>) : DataLogUiState()
    data class Error(val message: String) : DataLogUiState()
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

class DataModuleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataActivityRepository()

    private val _logState = MutableStateFlow<DataLogUiState>(DataLogUiState.Loading)
    val logState: StateFlow<DataLogUiState> = _logState

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _logState.value = DataLogUiState.Loading
            when (val result = repository.getLogs()) {
                is ApiResult.Success -> _logState.value = DataLogUiState.Success(result.data)
                is ApiResult.Error -> _logState.value = DataLogUiState.Error(result.message)
            }
        }
    }

    fun exportData(dataType: String, range: String) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            when (val result = repository.exportData(dataType, range)) {
                is ApiResult.Success -> {
                    try {
                        val dir = getApplication<Application>().getExternalFilesDir(null)
                        val fileName = "${dataType}_${range}_${System.currentTimeMillis()}.csv"
                        val file = File(dir, fileName)
                        file.outputStream().use { out -> result.data.byteStream().copyTo(out) }
                        _exportState.value = ExportState.Success(file.absolutePath)
                        loadLogs()
                    } catch (e: Exception) {
                        _exportState.value = ExportState.Error(e.message ?: "Could not save file")
                    }
                }
                is ApiResult.Error -> _exportState.value = ExportState.Error(result.message)
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}