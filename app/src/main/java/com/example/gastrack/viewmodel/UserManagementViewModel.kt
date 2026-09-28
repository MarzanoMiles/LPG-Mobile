package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.*
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UserListUiState {
    object Loading : UserListUiState()
    data class Success(val users: List<UserListItemDto>) : UserListUiState()
    data class Error(val message: String) : UserListUiState()
}

sealed class ActivityLogUiState {
    object Loading : ActivityLogUiState()
    data class Success(val logs: List<ActivityLogDto>) : ActivityLogUiState()
    data class Error(val message: String) : ActivityLogUiState()
}

class UserManagementViewModel : ViewModel() {
    private val repository = UserManagementRepository()

    private val _userListState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val userListState: StateFlow<UserListUiState> = _userListState

    private val _activityLogState = MutableStateFlow<ActivityLogUiState>(ActivityLogUiState.Loading)
    val activityLogState: StateFlow<ActivityLogUiState> = _activityLogState

    private val _roles = MutableStateFlow<List<RoleDto>>(emptyList())
    val roles: StateFlow<List<RoleDto>> = _roles

    private val _warehouses = MutableStateFlow<List<WarehouseDto>>(emptyList())
    val warehouses: StateFlow<List<WarehouseDto>> = _warehouses

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    init {
        loadUsers()
        loadRoles()
        loadWarehouses()
        loadActivityLog()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _userListState.value = UserListUiState.Loading
            when (val result = repository.getUsers()) {
                is ApiResult.Success -> _userListState.value = UserListUiState.Success(result.data)
                is ApiResult.Error -> _userListState.value = UserListUiState.Error(result.message)
            }
        }
    }

    fun loadRoles() {
        viewModelScope.launch {
            when (val result = repository.getRoles()) {
                is ApiResult.Success -> _roles.value = result.data
                is ApiResult.Error -> { /* role dropdown stays empty; non-fatal */ }
            }
        }
    }

    fun loadWarehouses() {
        viewModelScope.launch {
            when (val result = repository.getWarehouses()) {
                is ApiResult.Success -> _warehouses.value = result.data
                is ApiResult.Error -> { /* warehouse dropdown stays empty; non-fatal */ }
            }
        }
    }

    fun loadActivityLog() {
        viewModelScope.launch {
            _activityLogState.value = ActivityLogUiState.Loading
            when (val result = repository.getActivityLog()) {
                is ApiResult.Success -> _activityLogState.value = ActivityLogUiState.Success(result.data)
                is ApiResult.Error -> _activityLogState.value = ActivityLogUiState.Error(result.message)
            }
        }
    }

    fun addUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        roleId: Int,
        warehouseId: Int?
    ) {
        viewModelScope.launch {
            val request = CreateUserRequest(firstName, lastName, email, password, roleId, warehouseId)
            when (val result = repository.createUser(request)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "User created"
                    loadUsers()
                    loadActivityLog()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun updateUser(
        userId: Int,
        firstName: String,
        lastName: String,
        roleId: Int,
        warehouseId: Int?,
        status: String?
    ) {
        viewModelScope.launch {
            val request = UpdateUserRequest(firstName, lastName, roleId, warehouseId, status)
            when (val result = repository.updateUser(userId, request)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "User updated"
                    loadUsers()
                    loadActivityLog()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun deactivateUser(userId: Int) {
        viewModelScope.launch {
            when (val result = repository.deactivateUser(userId)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "User deactivated"
                    loadUsers()
                    loadActivityLog()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}