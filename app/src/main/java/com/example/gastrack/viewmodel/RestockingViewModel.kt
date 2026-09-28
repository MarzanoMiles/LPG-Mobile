package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.PurchaseOrderDto
import com.example.gastrack.data.remote.dto.RestockRecommendationDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.PurchaseOrderRepository
import com.example.gastrack.data.repository.RestockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PendingPOUiState {
    object Loading : PendingPOUiState()
    data class Success(val orders: List<PurchaseOrderDto>) : PendingPOUiState()
    data class Error(val message: String) : PendingPOUiState()
}

sealed class RecommendationUiState {
    object Loading : RecommendationUiState()
    data class Success(val recommendations: List<RestockRecommendationDto>) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}

class RestockingViewModel : ViewModel() {
    private val purchaseOrderRepository = PurchaseOrderRepository()
    private val restockRepository = RestockRepository()

    private val _pendingPOState = MutableStateFlow<PendingPOUiState>(PendingPOUiState.Loading)
    val pendingPOState: StateFlow<PendingPOUiState> = _pendingPOState

    private val _recommendationState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val recommendationState: StateFlow<RecommendationUiState> = _recommendationState

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    init {
        loadPendingPurchaseOrders()
        loadRecommendations()
    }

    fun loadPendingPurchaseOrders() {
        viewModelScope.launch {
            _pendingPOState.value = PendingPOUiState.Loading
            when (val result = purchaseOrderRepository.getPurchaseOrders()) {
                is ApiResult.Success -> {
                    val pending = result.data.filter { it.Status == "Pending" }
                    _pendingPOState.value = PendingPOUiState.Success(pending)
                }
                is ApiResult.Error -> _pendingPOState.value = PendingPOUiState.Error(result.message)
            }
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _recommendationState.value = RecommendationUiState.Loading
            when (val result = restockRepository.getRecommendations()) {
                is ApiResult.Success -> _recommendationState.value = RecommendationUiState.Success(result.data)
                is ApiResult.Error -> _recommendationState.value = RecommendationUiState.Error(result.message)
            }
        }
    }

    fun approvePurchaseOrder(purchaseOrderId: Int) {
        viewModelScope.launch {
            when (val result = purchaseOrderRepository.updateStatus(purchaseOrderId, "Approved")) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Purchase order approved — stock updated"
                    loadPendingPurchaseOrders()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun cancelPurchaseOrder(purchaseOrderId: Int) {
        viewModelScope.launch {
            when (val result = purchaseOrderRepository.updateStatus(purchaseOrderId, "Cancelled")) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Purchase order cancelled"
                    loadPendingPurchaseOrders()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun approveRecommendation(restockId: Int) {
        viewModelScope.launch {
            when (val result = purchaseOrderRepository.convertRestockToPO(restockId)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Generated PO ${result.data.poNo}"
                    loadRecommendations()
                    loadPendingPurchaseOrders()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun generateNewRecommendations() {
        viewModelScope.launch {
            when (val result = restockRepository.generateRecommendations()) {
                is ApiResult.Success -> {
                    val message = if (result.data.createdCount > 0) {
                        "${result.data.createdCount} new recommendation(s) generated"
                    } else {
                        "No products currently need restocking"
                    }
                    _actionMessage.value = message
                    loadRecommendations()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}