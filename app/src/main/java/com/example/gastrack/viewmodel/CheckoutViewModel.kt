package com.example.gastrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.local.TokenManager
import com.example.gastrack.data.remote.dto.CheckoutItem
import com.example.gastrack.data.remote.dto.CheckoutRequest
import com.example.gastrack.data.remote.dto.CheckoutResponse
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object Loading : CheckoutUiState()
    data class Success(val response: CheckoutResponse) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = SalesRepository()

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState: StateFlow<CheckoutUiState> = _uiState

    fun placeOrder(
        items: Map<Int, Int>,
        orderType: String,
        paymentMethod: String,
        amountPaid: Double,
        deliveryAddress: String? = null,
        deliveryCharge: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Loading

            val customerId = tokenManager.getUserId()
            val warehouseId = tokenManager.getWarehouseId() ?: 1 // your DB currently has one warehouse (Pasig Warehouse, ID 1)

            if (customerId == null) {
                _uiState.value = CheckoutUiState.Error("You must be logged in to place an order.")
                return@launch
            }
            if (items.isEmpty()) {
                _uiState.value = CheckoutUiState.Error("Your cart is empty.")
                return@launch
            }

            val request = CheckoutRequest(
                customerId = customerId,
                warehouseId = warehouseId,
                orderType = orderType,
                items = items.map { (id, qty) -> CheckoutItem(id, qty) },
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                deliveryAddress = deliveryAddress,
                deliveryCharge = deliveryCharge
            )

            when (val result = repository.checkout(request)) {
                is ApiResult.Success -> _uiState.value = CheckoutUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = CheckoutUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckoutUiState.Idle
    }
}