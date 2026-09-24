package com.example.gastrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.local.TokenManager
import com.example.gastrack.data.remote.dto.CustomerRegisterRequest
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = AuthRepository(tokenManager)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    var warehouseId: Int? = null
        private set

    fun loginEmployee(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.employeeLogin(email, password)) {
                is ApiResult.Success -> {
                    warehouseId = result.data.user.warehouseId
                    _uiState.value = AuthUiState.Success
                }
                is ApiResult.Error -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun loginCustomer(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.customerLogin(email, password)) {
                is ApiResult.Success -> _uiState.value = AuthUiState.Success
                is ApiResult.Error -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun registerCustomer(
        fullName: String, email: String, password: String,
        contactNo: String, address: String, customerType: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val request = CustomerRegisterRequest(fullName, email, password, contactNo, address, customerType)
            when (val result = repository.customerRegister(request)) {
                is ApiResult.Success -> _uiState.value = AuthUiState.Success
                is ApiResult.Error -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }
}