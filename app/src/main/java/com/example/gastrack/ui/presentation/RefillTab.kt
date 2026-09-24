package com.example.gastrack.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gastrack.R

@Composable
fun RefillTabContent(
    cart: Map<Int, Int>,
    onQuantityChange: (Int, Int) -> Unit,
) {
    val refillProducts = listOf(
        Product(1, "11kg Standard Refill", "Perfect for household cooking.", 950, R.drawable.tank_11kg, 24, "Petron", isPopular = true),
        Product(2, "2.7kg Camping Refill", "Portable size for outdoor grills.", 280, R.drawable.tank_2_7kg, 15, "Petron"),
        Product(3, "50kg Commercial Refill", "For restaurants and kitchens.", 4500, R.drawable.tank_50kg, 8, "Solane"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        items(refillProducts) { product ->
            ProductCard(
                product = product,
                quantity = cart[product.id] ?: 0,
                onQuantityChange = { onQuantityChange(product.id, it) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RefillTabContentPreview() {
    RefillTabContent(cart = emptyMap()) { _, _ -> }
}
