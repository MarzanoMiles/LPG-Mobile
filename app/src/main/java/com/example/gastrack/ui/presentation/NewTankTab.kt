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
fun NewTankTabContent(
    cart: Map<Int, Int>,
    onQuantityChange: (Int, Int) -> Unit,
) {
    val newTankProducts = listOf(
        Product(4, "11kg Standard Tank", "Perfect for household cooking.", 1150, R.drawable.tank_11kg, 12, "Solane", isPopular = true),
        Product(5, "2.7kg Camping Tank", "Portable size for outdoor grills.", 350, R.drawable.tank_2_7kg, 5, "M-Gas"),
        Product(6, "50kg Commercial Tank", "For restaurants and kitchens.", 8500, R.drawable.tank_50kg, 3, "Phoenix"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        items(newTankProducts) { product ->
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
fun NewTankTabContentPreview() {
    NewTankTabContent(cart = emptyMap()) { _, _ -> }
}
