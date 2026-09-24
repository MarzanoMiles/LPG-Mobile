package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.R
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.TextDark
import com.example.gastrack.ui.theme.TextGray
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTabScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToMenu: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Refill") }
    var selectedBrand by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }

    // Cart State
    val cart = remember { mutableStateMapOf<Int, Int>() }

    val allProducts = listOf(
        Product(1, "11kg Standard Refill", "Refill only", 950, R.drawable.tank_11kg, 24, "Petron", isPopular = true),
        Product(2, "2.7kg Camping Refill", "Portable size", 280, R.drawable.tank_2_7kg, 15, "Petron"),
        Product(3, "50kg Commercial Refill", "Bulk usage", 4500, R.drawable.tank_50kg, 8, "Solane"),
        Product(4, "11kg Standard Tank", "New cylinder", 1150, R.drawable.tank_11kg, 12, "Solane", isPopular = true),
        Product(5, "2.7kg Camping Tank", "Small cylinder", 350, R.drawable.tank_2_7kg, 5, "M-Gas"),
        Product(6, "50kg Commercial Tank", "Heavy duty", 8500, R.drawable.tank_50kg, 3, "Phoenix"),
    )

    val categories = listOf("Refill", "New Tank")
    val brands = listOf("All", "Petron", "Solane", "M-Gas", "Phoenix")

    val filteredProducts = allProducts.filter { product ->
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = if (selectedCategory == "Refill") product.name.contains("Refill") else !product.name.contains("Refill")
        val matchesBrand = if (selectedBrand == "All") true else product.brand == selectedBrand
        matchesSearch && matchesCategory && matchesBrand
    }

    val totalItems = cart.values.sum()
    val totalPrice = cart.entries.sumOf { entry ->
        (allProducts.find { it.id == entry.key }?.price ?: 0) * entry.value
    }
    val formattedTotal = String.format(Locale.US, "₱%,d.00", totalPrice)

    Scaffold(
        containerColor = Color(0xFFFBFBFE),
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "order",
                onHomeClick = onNavigateToHome,
                onMenuClick = onNavigateToMenu,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // --- Top Bar ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Marketplace",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark,
                        )
                        Text(
                            text = "Find the best deals for your LPG",
                            fontSize = 13.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.favicon),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Search Bar ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search gas, cylinders, parts...", color = Color.LightGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StaffPortalBlue) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = GasTrackBlue,
                    ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- Category Tabs ---
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGray,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            modifier = Modifier
                                .clickable { selectedCategory = category }
                                .height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) StaffPortalBlue else Color.White,
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE9ECEF)),
                            shadowElevation = if (isSelected) 4.dp else 0.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.White else TextGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Brand Filter ---
                Text(
                    text = "FILTER BY BRAND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGray,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedBrand,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE9ECEF),
                            focusedBorderColor = GasTrackBlue
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand) },
                                onClick = {
                                    selectedBrand = brand
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Product List ---
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            quantity = cart[product.id] ?: 0,
                            onQuantityChange = { qty ->
                                if (qty <= 0) cart.remove(product.id) else cart[product.id] = qty
                            },
                            onBuyNow = {
                                cart[product.id] = 1
                                onNavigateToCheckout()
                            },
                        )
                    }
                }
            }

            // --- Floating View Cart Bar ---
            if (totalItems > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                ) {
                    Button(
                        onClick = onNavigateToCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        text = "$totalItems ITEM${if (totalItems > 1) "S" else ""}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "View My Cart",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            Text(
                                text = formattedTotal,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderTabScreenPreview() {
    OrderTabScreen(onNavigateToHome = {}, onNavigateToCheckout = {}, onNavigateToMenu = {})
}
