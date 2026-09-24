package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.ProductDto
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<ProductDto>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.getProducts()) {
                is ApiResult.Success -> _uiState.value = ProductUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = ProductUiState.Error(result.message)
            }
        }
    }
}