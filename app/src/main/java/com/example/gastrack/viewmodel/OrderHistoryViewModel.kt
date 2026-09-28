package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.OrderDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OrderHistoryUiState {
    object Loading : OrderHistoryUiState()
    data class Success(val orders: List<OrderDto>) : OrderHistoryUiState()
    data class Error(val message: String) : OrderHistoryUiState()
}

class OrderHistoryViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState: StateFlow<OrderHistoryUiState> = _uiState

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrderHistoryUiState.Loading
            when (val result = repository.getOrders()) {
                is ApiResult.Success -> _uiState.value = OrderHistoryUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = OrderHistoryUiState.Error(result.message)
            }
        }
    }
}