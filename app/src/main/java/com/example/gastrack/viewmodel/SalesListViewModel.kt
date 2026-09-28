package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.SalesListItemDto
import com.example.gastrack.data.remote.dto.SalesStatsDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.SalesListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SalesListUiState {
    object Loading : SalesListUiState()
    data class Success(val sales: List<SalesListItemDto>) : SalesListUiState()
    data class Error(val message: String) : SalesListUiState()
}

sealed class SalesStatsUiState {
    object Loading : SalesStatsUiState()
    data class Success(val stats: SalesStatsDto) : SalesStatsUiState()
    data class Error(val message: String) : SalesStatsUiState()
}

class SalesListViewModel : ViewModel() {
    private val repository = SalesListRepository()

    private val _listState = MutableStateFlow<SalesListUiState>(SalesListUiState.Loading)
    val listState: StateFlow<SalesListUiState> = _listState

    private val _statsState = MutableStateFlow<SalesStatsUiState>(SalesStatsUiState.Loading)
    val statsState: StateFlow<SalesStatsUiState> = _statsState

    init {
        loadStats()
        loadSalesList()
    }

    fun loadStats() {
        viewModelScope.launch {
            _statsState.value = SalesStatsUiState.Loading
            when (val result = repository.getStats()) {
                is ApiResult.Success -> _statsState.value = SalesStatsUiState.Success(result.data)
                is ApiResult.Error -> _statsState.value = SalesStatsUiState.Error(result.message)
            }
        }
    }

    fun loadSalesList() {
        viewModelScope.launch {
            _listState.value = SalesListUiState.Loading
            when (val result = repository.getSalesList()) {
                is ApiResult.Success -> _listState.value = SalesListUiState.Success(result.data)
                is ApiResult.Error -> _listState.value = SalesListUiState.Error(result.message)
            }
        }
    }
}