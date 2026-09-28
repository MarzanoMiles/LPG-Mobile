package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.DeliveryDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DeliveryUiState {
    object Loading : DeliveryUiState()
    data class Success(val deliveries: List<DeliveryDto>) : DeliveryUiState()
    data class Error(val message: String) : DeliveryUiState()
}

class DeliveryViewModel : ViewModel() {
    private val repository = DeliveryRepository()

    private val _uiState = MutableStateFlow<DeliveryUiState>(DeliveryUiState.Loading)
    val uiState: StateFlow<DeliveryUiState> = _uiState

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    init {
        loadDeliveries()
    }

    fun loadDeliveries() {
        viewModelScope.launch {
            _uiState.value = DeliveryUiState.Loading
            when (val result = repository.getDeliveries()) {
                is ApiResult.Success -> _uiState.value = DeliveryUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = DeliveryUiState.Error(result.message)
            }
        }
    }

    fun updateStatus(deliveryId: Int, newStatus: String) {
        viewModelScope.launch {
            when (val result = repository.updateStatus(deliveryId, newStatus)) {
                is ApiResult.Success -> loadDeliveries()
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}