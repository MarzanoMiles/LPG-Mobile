package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.CustomerProfileDto
import com.example.gastrack.data.remote.dto.UpdateProfileRequest
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: CustomerProfileDto) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileSaveState {
    object Idle : ProfileSaveState()
    object Saving : ProfileSaveState()
    object Saved : ProfileSaveState()
    data class Error(val message: String) : ProfileSaveState()
}

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _saveState = MutableStateFlow<ProfileSaveState>(ProfileSaveState.Idle)
    val saveState: StateFlow<ProfileSaveState> = _saveState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            when (val result = repository.getProfile()) {
                is ApiResult.Success -> _uiState.value = ProfileUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    fun saveProfile(customerName: String, contactNo: String, address: String, customerType: String) {
        viewModelScope.launch {
            _saveState.value = ProfileSaveState.Saving
            val request = UpdateProfileRequest(customerName, contactNo, address, customerType)
            when (val result = repository.updateProfile(request)) {
                is ApiResult.Success -> {
                    _saveState.value = ProfileSaveState.Saved
                    loadProfile()
                }
                is ApiResult.Error -> _saveState.value = ProfileSaveState.Error(result.message)
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = ProfileSaveState.Idle
    }
}