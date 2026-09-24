package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.R
import com.example.gastrack.data.remote.dto.ProductDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.util.resolveProductImageRes
import com.example.gastrack.viewmodel.CartViewModel
import com.example.gastrack.viewmodel.ProductUiState
import com.example.gastrack.viewmodel.ProductViewModel
import java.util.Locale
import kotlin.math.roundToInt

// Maps a backend ProductDto onto the existing UI-facing Product model used by ProductCard.
private fun ProductDto.toUiProduct(): Product {
    val priceInt = this.UnitPrice.toDoubleOrNull()?.roundToInt() ?: 0
    return Product(
        id = ProductID,
        name = ProductName,
        description = "$Category • $Unit",
        price = priceInt,
        imageRes = resolveProductImageRes(ProductName),
        stock = 99, // live stock lives in /inventory; checked authoritatively server-side at checkout
        brand = Brand,
        isPopular = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTabScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToMenu: () -> Unit,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedBrand by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }

    val uiState by productViewModel.uiState.collectAsState()

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
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StaffPortalBlue)
                    }
                }
                is ProductUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Couldn't load products",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.message,
                            fontSize = 13.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { productViewModel.loadProducts() },
                            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is ProductUiState.Success -> {
                    LaunchedEffect(state.products) {
                        cartViewModel.setProducts(state.products)
                    }

                    val allProducts = state.products.map { it.toUiProduct() }
                    val categories = state.products.map { it.Category }.distinct()
                    val brands = listOf("All") + state.products.map { it.Brand }.distinct()
                    val activeCategory = selectedCategory ?: categories.firstOrNull()

                    val filteredProducts = allProducts.filter { product ->
                        val sourceDto = state.products.find { it.ProductID == product.id }
                        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
                        val matchesCategory = activeCategory == null || sourceDto?.Category == activeCategory
                        val matchesBrand = selectedBrand == "All" || product.brand == selectedBrand
                        matchesSearch && matchesCategory && matchesBrand
                    }

                    val totalItems = cartViewModel.totalItems()
                    val totalPrice = cartViewModel.subtotal().roundToInt()
                    val formattedTotal = String.format(Locale.US, "₱%,d.00", totalPrice)

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

                        // --- Category Tabs (dynamic, from DB) ---
                        if (categories.isNotEmpty()) {
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
                                    val isSelected = activeCategory == category
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
                        }

                        // --- Brand Filter (dynamic, from DB) ---
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
                        if (filteredProducts.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No products match your filters", color = TextGray, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 100.dp),
                            ) {
                                items(filteredProducts) { product ->
                                    ProductCard(
                                        product = product,
                                        quantity = cartViewModel.quantities[product.id] ?: 0,
                                        onQuantityChange = { qty -> cartViewModel.setQuantity(product.id, qty) },
                                        onBuyNow = {
                                            cartViewModel.setQuantity(product.id, 1)
                                            onNavigateToCheckout()
                                        },
                                    )
                                }
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
    }
}

@Preview(showBackground = true)
@Composable
fun OrderTabScreenPreview() {
    OrderTabScreen(onNavigateToHome = {}, onNavigateToCheckout = {}, onNavigateToMenu = {})
}