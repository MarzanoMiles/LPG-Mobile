package com.example.gastrack.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.gastrack.data.remote.dto.ProductDto
import com.example.gastrack.util.resolveProductImageRes

data class CartLineItem(
    val productId: Int,
    val name: String,
    val unitPrice: Double,
    val quantity: Int,
    val imageRes: Int
)

class CartViewModel : ViewModel() {
    // Snapshot state map: Compose observes reads/writes automatically, no StateFlow needed.
    val quantities = mutableStateMapOf<Int, Int>()

    private var products: List<ProductDto> = emptyList()

    fun setProducts(list: List<ProductDto>) {
        products = list
    }

    fun setQuantity(productId: Int, qty: Int) {
        if (qty <= 0) quantities.remove(productId) else quantities[productId] = qty
    }

    fun removeItem(productId: Int) {
        quantities.remove(productId)
    }

    fun getLineItems(): List<CartLineItem> {
        return quantities.mapNotNull { (id, qty) ->
            val product = products.find { it.ProductID == id } ?: return@mapNotNull null
            CartLineItem(
                productId = id,
                name = product.ProductName,
                unitPrice = product.UnitPrice.toDoubleOrNull() ?: 0.0,
                quantity = qty,
                imageRes = resolveProductImageRes(product.ProductName)
            )
        }
    }

    fun totalItems(): Int = quantities.values.sum()

    fun subtotal(): Double = quantities.entries.sumOf { (id, qty) ->
        (products.find { it.ProductID == id }?.UnitPrice?.toDoubleOrNull() ?: 0.0) * qty
    }

    fun clear() {
        quantities.clear()
    }
}