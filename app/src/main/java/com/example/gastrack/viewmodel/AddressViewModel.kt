package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.AddressDto
import com.example.gastrack.data.repository.AddressRepository
import com.example.gastrack.data.repository.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddressUiState {
    object Loading : AddressUiState()
    data class Success(val addresses: List<AddressDto>) : AddressUiState()
    data class Error(val message: String) : AddressUiState()
}

class AddressViewModel : ViewModel() {
    private val repository = AddressRepository()

    private val _uiState = MutableStateFlow<AddressUiState>(AddressUiState.Loading)
    val uiState: StateFlow<AddressUiState> = _uiState

    private val _primaryAddress = MutableStateFlow<AddressDto?>(null)
    val primaryAddress: StateFlow<AddressDto?> = _primaryAddress

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            when (val result = repository.getAddresses()) {
                is ApiResult.Success -> {
                    _uiState.value = AddressUiState.Success(result.data)
                    _primaryAddress.value = result.data.find { it.IsPrimary == 1 } ?: result.data.firstOrNull()
                }
                is ApiResult.Error -> _uiState.value = AddressUiState.Error(result.message)
            }
        }
    }

    fun loadPrimaryAddress() {
        viewModelScope.launch {
            when (val result = repository.getPrimaryAddress()) {
                is ApiResult.Success -> _primaryAddress.value = result.data
                is ApiResult.Error -> { /* silently keep old value; checkout screen has its own fallback text */ }
            }
        }
    }

    fun addAddress(label: String, addressLine: String, addressType: String) {
        viewModelScope.launch {
            when (val result = repository.createAddress(label, addressLine, addressType)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Address added"
                    loadAddresses()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun setPrimary(addressId: Int) {
        viewModelScope.launch {
            when (val result = repository.setPrimary(addressId)) {
                is ApiResult.Success -> loadAddresses()
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun deleteAddress(addressId: Int) {
        viewModelScope.launch {
            when (val result = repository.deleteAddress(addressId)) {
                is ApiResult.Success -> loadAddresses()
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}