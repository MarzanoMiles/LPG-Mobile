package com.example.gastrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastrack.data.remote.dto.*
import com.example.gastrack.data.repository.ApiResult
import com.example.gastrack.data.repository.ProductManagementRepository
import com.example.gastrack.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProductMgmtUiState {
    object Loading : ProductMgmtUiState()
    data class Success(val products: List<ProductDto>) : ProductMgmtUiState()
    data class Error(val message: String) : ProductMgmtUiState()
}

class ProductManagementViewModel : ViewModel() {
    private val repository = ProductManagementRepository()
    private val supplierRepository = SupplierRepository()

    private val _uiState = MutableStateFlow<ProductMgmtUiState>(ProductMgmtUiState.Loading)
    val uiState: StateFlow<ProductMgmtUiState> = _uiState

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories

    private val _brands = MutableStateFlow<List<BrandDto>>(emptyList())
    val brands: StateFlow<List<BrandDto>> = _brands

    private val _suppliers = MutableStateFlow<List<SupplierDto>>(emptyList())
    val suppliers: StateFlow<List<SupplierDto>> = _suppliers

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    init {
        loadProducts()
        loadLookups()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductMgmtUiState.Loading
            when (val result = repository.getAllProducts()) {
                is ApiResult.Success -> _uiState.value = ProductMgmtUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = ProductMgmtUiState.Error(result.message)
            }
        }
    }

    fun loadLookups() {
        viewModelScope.launch {
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> _categories.value = result.data
                is ApiResult.Error -> { /* dropdown stays empty; non-fatal */ }
            }
            when (val result = repository.getBrands()) {
                is ApiResult.Success -> _brands.value = result.data
                is ApiResult.Error -> { /* dropdown stays empty; non-fatal */ }
            }
            when (val result = supplierRepository.getSuppliers()) {
                is ApiResult.Success -> _suppliers.value = result.data
                is ApiResult.Error -> { /* dropdown stays empty; non-fatal */ }
            }
        }
    }

    fun addProduct(
        name: String,
        categoryId: Int,
        brandId: Int,
        supplierId: Int,
        unit: String,
        unitPrice: Double,
        costPrice: Double,
        reorderLevel: Int
    ) {
        viewModelScope.launch {
            val request = CreateProductRequest(name, categoryId, brandId, supplierId, unit, unitPrice, costPrice, reorderLevel)
            when (val result = repository.createProduct(request)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Product added"
                    loadProducts()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun updateProduct(
        id: Int,
        name: String,
        categoryId: Int,
        brandId: Int,
        supplierId: Int,
        unit: String,
        unitPrice: Double,
        costPrice: Double,
        reorderLevel: Int,
        status: String
    ) {
        viewModelScope.launch {
            val request = UpdateProductRequest(name, categoryId, brandId, supplierId, unit, unitPrice, costPrice, reorderLevel, status)
            when (val result = repository.updateProduct(id, request)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Product updated"
                    loadProducts()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun deactivateProduct(id: Int) {
        viewModelScope.launch {
            when (val result = repository.deactivateProduct(id)) {
                is ApiResult.Success -> {
                    _actionMessage.value = "Product deactivated"
                    loadProducts()
                }
                is ApiResult.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun reactivateProduct(product: ProductDto) {
        updateProduct(
            id = product.ProductID,
            name = product.ProductName,
            categoryId = product.CategoryID,
            brandId = product.BrandID,
            supplierId = product.SupplierID,
            unit = product.Unit,
            unitPrice = product.UnitPrice.toDoubleOrNull() ?: 0.0,
            costPrice = product.CostPrice.toDoubleOrNull() ?: 0.0,
            reorderLevel = product.ReorderLevel,
            status = "Active"
        )
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}