package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.PurchaseOrderDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.PurchaseOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PurchaseOrderUiState {
    object Loading : PurchaseOrderUiState()
    data class Success(val orders: List<PurchaseOrderDto>) : PurchaseOrderUiState()
    data class Error(val message: String) : PurchaseOrderUiState()
}

class PurchaseOrderViewModel : ViewModel() {
    private val repository = PurchaseOrderRepository()

    private val _uiState = MutableStateFlow<PurchaseOrderUiState>(PurchaseOrderUiState.Loading)
    val uiState: StateFlow<PurchaseOrderUiState> = _uiState

    init {
        loadPurchaseOrders()
    }

    fun loadPurchaseOrders() {
        viewModelScope.launch {
            _uiState.value = PurchaseOrderUiState.Loading
            when (val result = repository.getPurchaseOrders()) {
                is ApiResult.Success -> _uiState.value = PurchaseOrderUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = PurchaseOrderUiState.Error(result.message)
            }
        }
    }
}