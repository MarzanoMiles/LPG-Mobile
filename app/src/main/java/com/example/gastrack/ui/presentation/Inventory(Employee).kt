package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.InventoryDto
import com.example.gastrack.data.remote.dto.ProductDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.*

enum class InventoryScreenState {
    OVERVIEW, STOCK_IN, STOCK_OUT
}

@Composable
fun InventoryEmployeeScreen(
    onBack: () -> Unit = {},
    onNavigateToAdjustment: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    var currentState by remember { mutableStateOf(InventoryScreenState.OVERVIEW) }

    Crossfade(targetState = currentState, label = "InventoryTransition") { state ->
        when (state) {
            InventoryScreenState.OVERVIEW -> {
                InventoryOverviewScreen(
                    inventoryViewModel = inventoryViewModel,
                    onStockInClick = { currentState = InventoryScreenState.STOCK_IN },
                    onStockOutClick = { currentState = InventoryScreenState.STOCK_OUT },
                    onAdjustmentClick = onNavigateToAdjustment,
                    onBack = onBack
                )
            }
            InventoryScreenState.STOCK_IN -> {
                StockInOutScreen(
                    isStockIn = true,
                    warehouseId = inventoryViewModel.warehouseId,
                    onToggle = { currentState = InventoryScreenState.STOCK_OUT },
                    onBack = { currentState = InventoryScreenState.OVERVIEW },
                    onCompleted = {
                        inventoryViewModel.loadInventory()
                        currentState = InventoryScreenState.OVERVIEW
                    }
                )
            }
            InventoryScreenState.STOCK_OUT -> {
                StockInOutScreen(
                    isStockIn = false,
                    warehouseId = inventoryViewModel.warehouseId,
                    onToggle = { currentState = InventoryScreenState.STOCK_IN },
                    onBack = { currentState = InventoryScreenState.OVERVIEW },
                    onCompleted = {
                        inventoryViewModel.loadInventory()
                        currentState = InventoryScreenState.OVERVIEW
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryOverviewScreen(
    inventoryViewModel: InventoryViewModel,
    onStockInClick: () -> Unit,
    onStockOutClick: () -> Unit,
    onAdjustmentClick: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by inventoryViewModel.uiState.collectAsState()
    val restockState by inventoryViewModel.restockState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(restockState) {
        when (val state = restockState) {
            is RestockGenState.Done -> {
                val message = if (state.count > 0) {
                    "${state.count} new restock recommendation(s) generated"
                } else {
                    "No products currently need restocking"
                }
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                inventoryViewModel.resetRestockState()
            }
            is RestockGenState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                inventoryViewModel.resetRestockState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(bottomEnd = 48.dp))
                .background(StaffPortalButtonBlue, RoundedCornerShape(bottomEnd = 48.dp))
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = "Inventory",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = "Admin",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        when (val state = uiState) {
            is InventoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is InventoryUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load inventory", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { inventoryViewModel.loadInventory() },
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is InventoryUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InventoryActionButton(
                                text = "Stock In",
                                icon = Icons.Default.ArrowDownward,
                                onClick = onStockInClick,
                                modifier = Modifier.weight(1f)
                            )
                            InventoryActionButton(
                                text = "Stock Out",
                                icon = Icons.Default.ArrowUpward,
                                onClick = onStockOutClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        InventoryActionButton(
                            text = "Stock Adjustment",
                            icon = Icons.Default.Tune,
                            onClick = onAdjustmentClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            text = "Current Stock",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                        )
                    }

                    if (state.items.isEmpty()) {
                        item {
                            Text(
                                text = "No inventory records for this warehouse yet.",
                                color = TextGray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        items(state.items) { item ->
                            StockItemCard(item)
                        }
                    }

                    item {
                        Button(
                            onClick = { inventoryViewModel.generateAiRecommendations() },
                            enabled = restockState !is RestockGenState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .height(56.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (restockState is RestockGenState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ai Recommendation",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontStyle = FontStyle.Italic
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

// items(...) extension for LazyListScope over a plain List<InventoryDto>
private fun androidx.compose.foundation.lazy.LazyListScope.items(items: List<InventoryDto>, content: @Composable (InventoryDto) -> Unit) {
    items(count = items.size) { index -> content(items[index]) }
}

@Composable
fun InventoryActionButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = StaffPortalButtonBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Composable
fun StockItemCard(item: InventoryDto) {
    val isLowStock = item.StockOnHand <= item.ReorderLevel

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) Color(0xFFFFEBEE) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = item.ProductName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLowStock) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (isLowStock) "Low Stock Alert" else "Normal",
                        fontSize = 14.sp,
                        color = if (isLowStock) Color.Red else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = "${item.StockOnHand} ${item.Unit}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isLowStock) Color.Red else Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInOutScreen(
    isStockIn: Boolean,
    warehouseId: Int?,
    onToggle: () -> Unit,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    productViewModel: ProductViewModel = viewModel(),
    stockTransactionViewModel: StockTransactionViewModel = viewModel()
) {
    var quantity by remember { mutableIntStateOf(1) }
    var referenceOrAssignee by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductDto?>(null) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val productState by productViewModel.uiState.collectAsState()
    val transactionState by stockTransactionViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(transactionState) {
        when (val state = transactionState) {
            is StockTransactionUiState.Success -> {
                android.widget.Toast.makeText(
                    context,
                    "Recorded. New stock level: ${state.newStock}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                stockTransactionViewModel.resetState()
                onCompleted()
            }
            is StockTransactionUiState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                stockTransactionViewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Text(
                    text = "Inventory",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Toggle ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(2.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .background(
                                if (isStockIn) StaffPortalButtonBlue else Color.Transparent,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { if (!isStockIn) onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stock In",
                            color = if (isStockIn) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .background(
                                if (!isStockIn) StaffPortalButtonBlue else Color.Transparent,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { if (isStockIn) onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stock Out",
                            color = if (!isStockIn) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Product Selection (real, from product catalog) ---
            Text(
                text = if (isStockIn) "Select Product" else "Select Product to Dispatch",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (val state = productState) {
                is ProductUiState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is ProductUiState.Error -> {
                    Text("Couldn't load products: ${state.message}", color = GasTrackRed, fontSize = 13.sp)
                }
                is ProductUiState.Success -> {
                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded,
                        onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.ProductName ?: "Select a product",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            state.products.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text(product.ProductName) },
                                    onClick = {
                                        selectedProduct = product
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Quantity Selector ---
            Text(
                text = "Quantity",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StaffPortalBlue)
                }
                Text(text = quantity.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextDark)
                IconButton(
                    onClick = { quantity++ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = StaffPortalBlue)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Reference / Assignee ---
            if (isStockIn) {
                Text(
                    text = "Reference / DR Number",
                    modifier = Modifier.align(Alignment.Start),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = referenceOrAssignee,
                    onValueChange = { referenceOrAssignee = it },
                    placeholder = { Text("Enter DR Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Text(
                    text = "Dispatch To / Assignee",
                    modifier = Modifier.align(Alignment.Start),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = referenceOrAssignee,
                    onValueChange = { referenceOrAssignee = it },
                    placeholder = { Text("e.g. Delivery Truck #3") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Warning Info ---
            if (!isStockIn) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF6C00))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ensure empty cylinders are received before releasing new commercial stock to customers.",
                            fontSize = 12.sp,
                            color = Color(0xFFEF6C00),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // --- Confirm Button ---
            Button(
                onClick = {
                    val product = selectedProduct
                    val whId = warehouseId
                    if (product == null) {
                        android.widget.Toast.makeText(context, "Select a product first", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (whId == null) {
                        android.widget.Toast.makeText(context, "No warehouse assigned to your account", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    stockTransactionViewModel.submit(
                        warehouseId = whId,
                        productId = product.ProductID,
                        transactionType = if (isStockIn) "Stock In" else "Stock Out",
                        quantity = quantity,
                        reason = if (isStockIn) "Supplier Delivery" else "Dispatch",
                        referenceNo = referenceOrAssignee.ifBlank { null },
                        remarks = null
                    )
                },
                enabled = transactionState !is StockTransactionUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStockIn) Color(0xFF2E7D32) else Color(0xFFC62828)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (transactionState is StockTransactionUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isStockIn) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isStockIn) "Confirm Stock In" else "Confirm Stock Out",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryEmployeeScreenPreview() {
    InventoryEmployeeScreen()
}