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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.gastrack.ui.theme.*

enum class InventoryScreenState {
    OVERVIEW, STOCK_IN, STOCK_OUT
}

@Composable
fun InventoryEmployeeScreen(
    onBack: () -> Unit = {},
    onNavigateToAdjustment: () -> Unit = {}
) {
    var currentState by remember { mutableStateOf(_root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.OVERVIEW) }

    Crossfade(targetState = currentState, label = "InventoryTransition") { state ->
        when (state) {
            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.OVERVIEW -> {
                _root_ide_package_.com.example.gastrack.ui.presentation.InventoryOverviewScreen(
                    onStockInClick = {
                        currentState =
                            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.STOCK_IN
                    },
                    onStockOutClick = {
                        currentState =
                            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.STOCK_OUT
                    },
                    onAdjustmentClick = onNavigateToAdjustment,
                    onBack = onBack
                )
            }
            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.STOCK_IN -> {
                _root_ide_package_.com.example.gastrack.ui.presentation.StockInOutScreen(
                    isStockIn = true,
                    onToggle = {
                        currentState =
                            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.STOCK_OUT
                    },
                    onBack = {
                        currentState =
                            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.OVERVIEW
                    }
                )
            }
            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.STOCK_OUT -> {
                _root_ide_package_.com.example.gastrack.ui.presentation.StockInOutScreen(
                    isStockIn = false,
                    onToggle = {
                        currentState =
                            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.STOCK_IN
                    },
                    onBack = {
                        currentState =
                            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryScreenState.OVERVIEW
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryOverviewScreen(
    onStockInClick: () -> Unit,
    onStockOutClick: () -> Unit,
    onAdjustmentClick: () -> Unit,
    onBack: () -> Unit
) {
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
                    _root_ide_package_.com.example.gastrack.ui.presentation.InventoryActionButton(
                        text = "Stock In",
                        icon = Icons.Default.ArrowDownward,
                        onClick = onStockInClick,
                        modifier = Modifier.weight(1f)
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.InventoryActionButton(
                        text = "Stock Out",
                        icon = Icons.Default.ArrowUpward,
                        onClick = onStockOutClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                _root_ide_package_.com.example.gastrack.ui.presentation.InventoryActionButton(
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

            item {
                _root_ide_package_.com.example.gastrack.ui.presentation.StockItemCard(
                    name = "11kg Standard",
                    status = "Low Stock Alert",
                    quantity = "15",
                    isLowStock = true
                )
            }
            item {
                _root_ide_package_.com.example.gastrack.ui.presentation.StockItemCard(
                    name = "50kg Commercial",
                    status = "Normal",
                    quantity = "42",
                    isLowStock = false
                )
            }
            item {
                _root_ide_package_.com.example.gastrack.ui.presentation.StockItemCard(
                    name = "Regulators",
                    status = "Normal",
                    quantity = "123",
                    isLowStock = false
                )
            }

            item {
                Button(
                    onClick = { /* Handle AI */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
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
fun StockItemCard(name: String, status: String, quantity: String, isLowStock: Boolean) {
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
                Text(text = name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
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
                        text = status,
                        fontSize = 14.sp,
                        color = if (isLowStock) Color.Red else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = quantity,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isLowStock) Color.Red else Color.Black
            )
        }
    }
}

@Composable
fun StockInOutScreen(
    isStockIn: Boolean,
    onToggle: () -> Unit,
    onBack: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(15) }
    var reference by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var showDetailDialog by remember { mutableStateOf(false) }

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

            // --- Product Selection ---
            Text(
                text = if (isStockIn) "Select Product" else "Select Product to Dispatch",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = "11kg Standard",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

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
                    onClick = { if (quantity > 0) quantity-- },
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
                    value = reference,
                    onValueChange = { reference = it },
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
                    value = "Delivery Truck #3",
                    onValueChange = { assignee = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Driver: Juan Dela Cruz") }
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
                onClick = { showDetailDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStockIn) Color(0xFF2E7D32) else Color(0xFFC62828)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDetailDialog) {
        if (isStockIn) {
            _root_ide_package_.com.example.gastrack.ui.presentation.StockInDetailDialog(onDismiss = {
                showDetailDialog = false
            })
        } else {
            _root_ide_package_.com.example.gastrack.ui.presentation.StockOutDetailDialog(onDismiss = {
                showDetailDialog = false
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInDetailDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f),
        content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Add Stock In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Stock In ID",
                        "SI-001"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Supplier",
                        "Supplier Name"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Invoice No",
                        "INV-001"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Received by",
                        "User Name"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Date",
                        "01/01/2026 2:14:05 PM"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Status",
                        "Pending"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Items",
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic
                        )
                        Button(onClick = {}, shape = RoundedCornerShape(4.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), modifier = Modifier.height(24.dp)) {
                            Text("Add Item", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    _root_ide_package_.com.example.gastrack.ui.presentation.TableBox(isStockIn = true)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Items: 50", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Estimated Cost: ₱ 45,675.00", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Save Draft") }
                        Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) { Text("Received") }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOutDetailDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f),
        content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Add Stock Out",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Stock Out ID",
                        "SO-001"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Stock Out Type",
                        "Supplier Return"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Reference ID",
                        "Reference ID"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Reason",
                        "Damaged"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Processed by",
                        "User Name"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Date",
                        "01/01/2026 2:14:05 PM"
                    )
                    _root_ide_package_.com.example.gastrack.ui.presentation.DetailField(
                        "Status",
                        "Pending"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Items",
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic
                        )
                        Button(onClick = {}, shape = RoundedCornerShape(4.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), modifier = Modifier.height(24.dp)) {
                            Text("Add Item", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    _root_ide_package_.com.example.gastrack.ui.presentation.TableBox(isStockIn = false)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Stock Out Items: 50", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Stock Deduction: 50", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Note: Items will be returned to supplier warehouse", fontSize = 10.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Save Draft") }
                        Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) { Text("Approved") }
                    }
                }
            }
        }
    )
}

@Composable
fun DetailField(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(0.4f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(":", modifier = Modifier.width(16.dp), fontSize = 12.sp)
        Box(modifier = Modifier.weight(0.6f).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(value, fontSize = 12.sp)
        }
    }
}

@Composable
fun TableBox(isStockIn: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().background(StaffPortalButtonBlue.copy(alpha = 0.1f)).padding(4.dp)) {
            Text("#", modifier = Modifier.weight(0.1f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Product ID", modifier = Modifier.weight(0.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Product Name", modifier = Modifier.weight(0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Qty", modifier = Modifier.weight(0.1f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            if (isStockIn) {
                Text("Cost Price", modifier = Modifier.weight(0.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Expiry Date", modifier = Modifier.weight(0.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("Condition", modifier = Modifier.weight(0.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Action", modifier = Modifier.weight(0.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        // Mock Row
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("1", modifier = Modifier.weight(0.1f), fontSize = 9.sp)
            Text("P-001", modifier = Modifier.weight(0.2f), fontSize = 9.sp)
            Text("Gasul LPG 2.7kg", modifier = Modifier.weight(0.3f), fontSize = 9.sp)
            Text("20", modifier = Modifier.weight(0.1f), fontSize = 9.sp)
            if (isStockIn) {
                Text("₱ 4,850.00", modifier = Modifier.weight(0.2f), fontSize = 9.sp)
                Text("01/01/2027", modifier = Modifier.weight(0.2f), fontSize = 9.sp)
            } else {
                Text("Damaged", modifier = Modifier.weight(0.2f), fontSize = 9.sp)
                Text("Send Back", modifier = Modifier.weight(0.2f), fontSize = 9.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryEmployeeScreenPreview() {
    _root_ide_package_.com.example.gastrack.ui.presentation.InventoryEmployeeScreen()
}
