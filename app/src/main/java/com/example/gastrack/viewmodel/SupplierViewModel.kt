package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.CreateSupplierRequest
import com.example.gastrack.data.remote.dto.SupplierDto
import com.example.gastrack.data.remote.dto.UpdateSupplierRequest
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SupplierUiState {
    object Loading : SupplierUiState()
    data class Success(val suppliers: List<SupplierDto>) : SupplierUiState()
    data class Error(val message: String) : SupplierUiState()
}

class SupplierViewModel : ViewModel() {
    private val repository = SupplierRepository()

    private val _uiState = MutableStateFlow<SupplierUiState>(SupplierUiState.Loading)
    val uiState: StateFlow<SupplierUiState> = _uiState

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        viewModelScope.launch {
            _uiState.value = SupplierUiState.Loading
            when (val result = repository.getSuppliers()) {
                is ApiResult.Success -> _uiState.value = SupplierUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = SupplierUiState.Error(result.message)
            }
        }
    }

    fun addSupplier(name: String, contactPerson: String, email: String, address: String, contact: String, leadTimeDays: Int) {
        viewModelScope.launch {
            val request = CreateSupplierRequest(name, contactPerson, email, address, contact, leadTimeDays)
            when (val result = repository.createSupplier(request)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Supplier added"
                    loadSuppliers()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun updateSupplier(id: Int, name: String, contactPerson: String, email: String, address: String, contact: String, leadTimeDays: Int, status: String) {
        viewModelScope.launch {
            val request = UpdateSupplierRequest(name, contactPerson, email, address, contact, leadTimeDays, status)
            when (val result = repository.updateSupplier(id, request)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Supplier updated"
                    loadSuppliers()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun deactivateSupplier(id: Int) {
        viewModelScope.launch {
            when (val result = repository.deactivateSupplier(id)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Supplier removed"
                    loadSuppliers()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}