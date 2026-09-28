package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.SaleDetailDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SaleDetailUiState {
    object Idle : SaleDetailUiState()
    object Loading : SaleDetailUiState()
    data class Success(val detail: SaleDetailDto) : SaleDetailUiState()
    data class Error(val message: String) : SaleDetailUiState()
}

class SaleDetailViewModel : ViewModel() {
    private val repository = SalesRepository()

    private val _uiState = MutableStateFlow<SaleDetailUiState>(SaleDetailUiState.Idle)
    val uiState: StateFlow<SaleDetailUiState> = _uiState

    fun loadDetail(saleId: Int) {
        viewModelScope.launch {
            _uiState.value = SaleDetailUiState.Loading
            when (val result = repository.getSaleDetail(saleId)) {
                is ApiResult.Success -> _uiState.value = SaleDetailUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = SaleDetailUiState.Error(result.message)
            }
        }
    }

    fun reset() {
        _uiState.value = SaleDetailUiState.Idle
    }
}