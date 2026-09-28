package com.example.gastrack.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.local.TokenManager
import com.example.gastrack.data.remote.dto.*
import com.example.gastrack.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class POSInventoryUiState {
    object Loading : POSInventoryUiState()
    data class Success(val items: List<InventoryDto>) : POSInventoryUiState()
    data class Error(val message: String) : POSInventoryUiState()
}

sealed class POSCustomerUiState {
    object Loading : POSCustomerUiState()
    data class Success(val customers: List<CustomerDto>) : POSCustomerUiState()
    data class Error(val message: String) : POSCustomerUiState()
}

sealed class POSCheckoutState {
    object Idle : POSCheckoutState()
    object Loading : POSCheckoutState()
    data class Success(val response: CheckoutResponse) : POSCheckoutState()
    data class Error(val message: String) : POSCheckoutState()
}

sealed class ReceiptUiState {
    object Idle : ReceiptUiState()
    object Loading : ReceiptUiState()
    data class Success(val detail: SaleDetailDto) : ReceiptUiState()
    data class Error(val message: String) : ReceiptUiState()
}

class POSViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val inventoryRepository = InventoryRepository()
    private val customerRepository = CustomerRepository()
    private val salesRepository = SalesRepository()

    private val _inventoryState = MutableStateFlow<POSInventoryUiState>(POSInventoryUiState.Loading)
    val inventoryState: StateFlow<POSInventoryUiState> = _inventoryState

    private val _customerState = MutableStateFlow<POSCustomerUiState>(POSCustomerUiState.Loading)
    val customerState: StateFlow<POSCustomerUiState> = _customerState

    private val _checkoutState = MutableStateFlow<POSCheckoutState>(POSCheckoutState.Idle)
    val checkoutState: StateFlow<POSCheckoutState> = _checkoutState

    private val _receiptState = MutableStateFlow<ReceiptUiState>(ReceiptUiState.Idle)
    val receiptState: StateFlow<ReceiptUiState> = _receiptState

    // productId -> quantity
    val cart = mutableStateMapOf<Int, Int>()

    var warehouseId: Int? = null
        private set

    private var walkInCustomerId: Int? = null

    init {
        loadInventory()
        searchCustomers(null)
    }

    fun loadInventory() {
        viewModelScope.launch {
            _inventoryState.value = POSInventoryUiState.Loading
            warehouseId = tokenManager.getWarehouseId()
            when (val result = inventoryRepository.getInventory(warehouseId)) {
                is ApiResult.Success -> _inventoryState.value = POSInventoryUiState.Success(result.data)
                is ApiResult.Error -> _inventoryState.value = POSInventoryUiState.Error(result.message)
            }
        }
    }

    fun searchCustomers(query: String?) {
        viewModelScope.launch {
            _customerState.value = POSCustomerUiState.Loading
            when (val result = customerRepository.searchCustomers(query)) {
                is ApiResult.Success -> _customerState.value = POSCustomerUiState.Success(result.data)
                is ApiResult.Error -> _customerState.value = POSCustomerUiState.Error(result.message)
            }
        }
    }

    fun addCustomer(name: String, phone: String, address: String, onDone: () -> Unit) {
        viewModelScope.launch {
            when (customerRepository.createCustomer(name, phone, address)) {
                is ApiResult.Success -> {
                    searchCustomers(null)
                    onDone()
                }
                is ApiResult.Error -> onDone()
            }
        }
    }

    fun setQuantity(productId: Int, qty: Int) {
        if (qty <= 0) cart.remove(productId) else cart[productId] = qty
    }

    fun cartTotal(): Double {
        val items = (inventoryState.value as? POSInventoryUiState.Success)?.items ?: return 0.0
        return cart.entries.sumOf { (id, qty) ->
            (items.find { it.ProductID == id }?.UnitPrice?.toDoubleOrNull() ?: 0.0) * qty
        }
    }

    fun checkout(selectedCustomer: CustomerDto?, paymentMethod: String, amountPaid: Double) {
        viewModelScope.launch {
            _checkoutState.value = POSCheckoutState.Loading

            val whId = warehouseId
            if (whId == null) {
                _checkoutState.value = POSCheckoutState.Error("No warehouse assigned to your account")
                return@launch
            }
            if (cart.isEmpty()) {
                _checkoutState.value = POSCheckoutState.Error("Cart is empty")
                return@launch
            }

            val customerId = selectedCustomer?.CustomerID ?: run {
                if (walkInCustomerId == null) {
                    when (val result = customerRepository.getWalkInCustomer()) {
                        is ApiResult.Success -> walkInCustomerId = result.data.CustomerID
                        is ApiResult.Error -> {
                            _checkoutState.value = POSCheckoutState.Error(result.message)
                            return@launch
                        }
                    }
                }
                walkInCustomerId!!
            }

            val request = CheckoutRequest(
                customerId = customerId,
                warehouseId = whId,
                orderType = "Walk-in",
                items = cart.map { (id, qty) -> CheckoutItem(id, qty) },
                paymentMethod = paymentMethod,
                amountPaid = amountPaid
            )

            when (val result = salesRepository.checkout(request)) {
                is ApiResult.Success -> {
                    _checkoutState.value = POSCheckoutState.Success(result.data)
                    loadReceipt(result.data.saleId)
                }
                is ApiResult.Error -> _checkoutState.value = POSCheckoutState.Error(result.message)
            }
        }
    }

    fun loadReceipt(saleId: Int) {
        viewModelScope.launch {
            _receiptState.value = ReceiptUiState.Loading
            when (val result = salesRepository.getSaleDetail(saleId)) {
                is ApiResult.Success -> _receiptState.value = ReceiptUiState.Success(result.data)
                is ApiResult.Error -> _receiptState.value = ReceiptUiState.Error(result.message)
            }
        }
    }

    fun clearCartAndResetCheckout() {
        cart.clear()
        _checkoutState.value = POSCheckoutState.Idle
        _receiptState.value = ReceiptUiState.Idle
        loadInventory()
    }

    fun resetCheckoutState() {
        _checkoutState.value = POSCheckoutState.Idle
    }
}