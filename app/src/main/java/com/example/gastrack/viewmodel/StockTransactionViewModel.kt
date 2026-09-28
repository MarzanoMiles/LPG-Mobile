package com.example.gastrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.StockTransactionRequest
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class StockTransactionUiState {
    object Idle : StockTransactionUiState()
    object Loading : StockTransactionUiState()
    data class Success(val newStock: Int) : StockTransactionUiState()
    data class Error(val message: String) : StockTransactionUiState()
}

class StockTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InventoryRepository()

    private val _uiState = MutableStateFlow<StockTransactionUiState>(StockTransactionUiState.Idle)
    val uiState: StateFlow<StockTransactionUiState> = _uiState

    fun submit(
        warehouseId: Int,
        productId: Int,
        transactionType: String,
        quantity: Int,
        reason: String?,
        referenceNo: String?,
        remarks: String?
    ) {
        viewModelScope.launch {
            _uiState.value = StockTransactionUiState.Loading
            val request = StockTransactionRequest(
                warehouseId = warehouseId,
                productId = productId,
                transactionType = transactionType,
                quantity = quantity,
                reason = reason,
                referenceNo = referenceNo,
                remarks = remarks
            )
            when (val result = repository.submitTransaction(request)) {
                is ApiResult.Success -> _uiState.value = StockTransactionUiState.Success(result.data.stockOnHand)
                is ApiResult.Error -> _uiState.value = StockTransactionUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = StockTransactionUiState.Idle
    }
}