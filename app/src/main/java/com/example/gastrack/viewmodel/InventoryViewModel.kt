package com.example.gastrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.local.TokenManager
import com.example.gastrack.data.remote.dto.InventoryDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.InventoryRepository
import com.example.gastrack.data.repository.RestockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class InventoryUiState {
    object Loading : InventoryUiState()
    data class Success(val items: List<InventoryDto>) : InventoryUiState()
    data class Error(val message: String) : InventoryUiState()
}

sealed class RestockGenState {
    object Idle : RestockGenState()
    object Loading : RestockGenState()
    data class Done(val count: Int) : RestockGenState()
    data class Error(val message: String) : RestockGenState()
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = InventoryRepository()
    private val restockRepository = RestockRepository()

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiState: StateFlow<InventoryUiState> = _uiState

    private val _restockState = MutableStateFlow<RestockGenState>(RestockGenState.Idle)
    val restockState: StateFlow<RestockGenState> = _restockState

    var warehouseId: Int? = null
        private set

    init {
        loadInventory()
    }

    fun loadInventory() {
        viewModelScope.launch {
            _uiState.value = InventoryUiState.Loading
            warehouseId = tokenManager.getWarehouseId()
            when (val result = repository.getInventory(warehouseId)) {
                is ApiResult.Success -> _uiState.value = InventoryUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = InventoryUiState.Error(result.message)
            }
        }
    }

    fun generateAiRecommendations() {
        viewModelScope.launch {
            _restockState.value = RestockGenState.Loading
            when (val result = restockRepository.generateRecommendations()) {
                is ApiResult.Success -> _restockState.value = RestockGenState.Done(result.data.createdCount)
                is ApiResult.Error -> _restockState.value = RestockGenState.Error(result.message)
            }
        }
    }

    fun resetRestockState() {
        _restockState.value = RestockGenState.Idle
    }
}