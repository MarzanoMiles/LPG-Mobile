package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.CustomerDto
import com.example.gastrack.data.remote.dto.InventoryDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.util.resolveProductImageRes
import com.example.gastrack.viewmodel.*
import java.util.Locale

@Composable
fun POSEmployeeScreen(
    onBack: () -> Unit = {},
    posViewModel: POSViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Products") }
    var showPayPopup by remember { mutableStateOf(false) }
    var showOrderDetails by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerDto?>(null) }

    val inventoryState by posViewModel.inventoryState.collectAsState()
    val checkoutState by posViewModel.checkoutState.collectAsState()
    val context = LocalContext.current

    val totalAmount = posViewModel.cartTotal()

    LaunchedEffect(checkoutState) {
        when (val state = checkoutState) {
            is POSCheckoutState.Success -> {
                showPayPopup = false
                showOrderDetails = true
            }
            is POSCheckoutState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                posViewModel.resetCheckoutState()
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedTab == "Products") {
                POSBottomBar(
                    totalAmount = totalAmount,
                    onCheckoutClick = { if (totalAmount > 0) showPayPopup = true }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // --- Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StaffPortalButtonBlue, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(
                                text = "Point of Sale",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontStyle = FontStyle.Italic
                            )
                            Text(
                                text = "Admin",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(48.dp).clip(CircleShape),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        POSTabItem("Products", selectedTab == "Products") { selectedTab = "Products" }
                        POSTabItem("Customers", selectedTab == "Customers") { selectedTab = "Customers" }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // --- Tab Content ---
            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == "Products") {
                    when (val state = inventoryState) {
                        is POSInventoryUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = StaffPortalButtonBlue)
                            }
                        }
                        is POSInventoryUiState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Couldn't load products: ${state.message}", color = GasTrackRed)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { posViewModel.loadInventory() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                                    Text("Retry")
                                }
                            }
                        }
                        is POSInventoryUiState.Success -> {
                            POSProductsTab(
                                items = state.items,
                                cart = posViewModel.cart,
                                onUpdateQuantity = { id, qty -> posViewModel.setQuantity(id, qty) }
                            )
                        }
                    }
                } else {
                    POSCustomersTab(
                        posViewModel = posViewModel,
                        selectedCustomer = selectedCustomer,
                        onCustomerSelected = { selectedCustomer = it },
                        onAddNewClick = { showAddCustomerDialog = true }
                    )
                }
            }
        }
    }

    if (showPayPopup) {
        PayPopup(
            totalAmount = totalAmount,
            isLoading = checkoutState is POSCheckoutState.Loading,
            onDismiss = { showPayPopup = false },
            onConfirm = { paymentMethod, amountCollected ->
                posViewModel.checkout(selectedCustomer, paymentMethod, amountCollected)
            }
        )
    }

    if (showOrderDetails) {
        OrderDetailsPopup(
            posViewModel = posViewModel,
            customer = selectedCustomer,
            onDismiss = {
                showOrderDetails = false
                selectedCustomer = null
                posViewModel.clearCartAndResetCheckout()
            }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onAdd = { name, phone, address ->
                posViewModel.addCustomer(name, phone, address) {
                    showAddCustomerDialog = false
                }
            }
        )
    }
}

@Composable
fun POSTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        style = if (isSelected) LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline) else LocalTextStyle.current
    )
}

@Composable
fun POSProductsTab(
    items: List<InventoryDto>,
    cart: Map<Int, Int>,
    onUpdateQuantity: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Available Products", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
        }
        items(items, key = { it.InventoryID }) { item ->
            POSProductCard(
                item = item,
                quantity = cart[item.ProductID] ?: 0,
                onUpdateQuantity = { onUpdateQuantity(item.ProductID, it) }
            )
        }
    }
}

@Composable
fun POSProductCard(
    item: InventoryDto,
    quantity: Int,
    onUpdateQuantity: (Int) -> Unit
) {
    val price = item.UnitPrice?.toDoubleOrNull() ?: 0.0
    val inStock = item.StockOnHand > 0

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = resolveProductImageRes(item.ProductName)),
                contentDescription = null,
                modifier = Modifier.size(64.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)).padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.ProductName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = if (inStock) "Stock: ${item.StockOnHand} ${item.Unit}" else "Out of Stock",
                    color = if (inStock) Color.Gray else Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = String.format(Locale.US, "₱%,.2f", price),
                    color = StaffPortalButtonBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            if (inStock) {
                if (quantity > 0) {
                    POSQuantitySelector(
                        quantity = quantity,
                        onUpdateQuantity = { newQty -> onUpdateQuantity(minOf(newQty, item.StockOnHand)) }
                    )
                } else {
                    Button(
                        onClick = { onUpdateQuantity(1) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                OutlinedButton(
                    enabled = false,
                    onClick = {},
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun POSQuantitySelector(
    quantity: Int,
    onUpdateQuantity: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = { onUpdateQuantity(quantity - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StaffPortalBlue, modifier = Modifier.size(16.dp))
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        IconButton(
            onClick = { onUpdateQuantity(quantity + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = StaffPortalBlue, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun POSBottomBar(totalAmount: Double, onCheckoutClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(16.dp),
        color = Color.White
    ) {
        Button(
            onClick = onCheckoutClick,
            modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (totalAmount > 0) Color(0xFF0D1B6D) else Color.Gray),
            enabled = totalAmount > 0
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Checkout", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = String.format(Locale.US, "Pay ₱%,.2f", totalAmount), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun POSCustomersTab(
    posViewModel: POSViewModel,
    selectedCustomer: CustomerDto?,
    onCustomerSelected: (CustomerDto?) -> Unit,
    onAddNewClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val customerState by posViewModel.customerState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                posViewModel.searchCustomers(it.ifBlank { null })
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            placeholder = { Text("Search name or mobile...", fontSize = 14.sp) },
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            ),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onCustomerSelected(null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (selectedCustomer == null) StaffPortalBlue else Color.LightGray)
            ) {
                Text("Walk-in", fontWeight = FontWeight.Bold, color = if (selectedCustomer == null) StaffPortalBlue else Color.Black)
            }
            Button(
                onClick = onAddNewClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue.copy(alpha = 0.2f))
            ) {
                Text("Add New", fontWeight = FontWeight.Bold, color = StaffPortalBlue)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = customerState) {
            is POSCustomerUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalBlue)
                }
            }
            is POSCustomerUiState.Error -> {
                Text("Couldn't load customers: ${state.message}", color = GasTrackRed, fontSize = 13.sp)
            }
            is POSCustomerUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(state.customers, key = { it.CustomerID }) { customer ->
                        POSCustomerCard(
                            customer = customer,
                            isSelected = selectedCustomer?.CustomerID == customer.CustomerID,
                            onSelect = { onCustomerSelected(customer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun POSCustomerCard(customer: CustomerDto, isSelected: Boolean, onSelect: () -> Unit) {
    val initials = customer.CustomerName.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isSelected) StaffPortalBlue else Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFE8EAF6)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = initials, color = StaffPortalBlue, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customer.CustomerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = customer.ContactNo, color = Color.Gray, fontSize = 12.sp)
                }
                RadioButton(selected = isSelected, onClick = onSelect)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = customer.Address, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PayPopup(
    totalAmount: Double,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (paymentMethod: String, amountCollected: Double) -> Unit
) {
    var amountCollected by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    val collected = amountCollected.toDoubleOrNull() ?: 0.0
    val change = if (collected >= totalAmount) collected - totalAmount else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Payment", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                PaymentDetailRow("Total Amount:", String.format(Locale.US, "₱ %,.2f", totalAmount), isBold = true)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Amount Collected:", modifier = Modifier.weight(1f), fontSize = 14.sp)
                    OutlinedTextField(
                        value = amountCollected,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountCollected = it },
                        modifier = Modifier.width(120.dp),
                        placeholder = { Text("0.00", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                    )
                }
                PaymentDetailRow("Change Due:", String.format(Locale.US, "₱ %,.2f", change), color = if (collected < totalAmount) Color.Gray else Color.Red, isBold = true)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel") }
                    Button(
                        enabled = collected >= totalAmount && totalAmount > 0 && !isLoading,
                        onClick = { onConfirm(paymentMethod, collected) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun OrderDetailsPopup(
    posViewModel: POSViewModel,
    customer: CustomerDto?,
    onDismiss: () -> Unit
) {
    val receiptState by posViewModel.receiptState.collectAsState()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth(0.92f)) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (val state = receiptState) {
                    is ReceiptUiState.Loading, ReceiptUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = StaffPortalButtonBlue)
                        }
                    }
                    is ReceiptUiState.Error -> {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sale completed, but couldn't load receipt", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.message, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                            Text("Done")
                        }
                    }
                    is ReceiptUiState.Success -> {
                        val detail = state.detail

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sale Completed", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        DetailValueRow("Sale No:", detail.SaleNo)
                        DetailValueRow("Date:", detail.SaleDate.take(19).replace("T", " "))
                        DetailValueRow("Customer:", customer?.CustomerName ?: "Walk-in Customer")
                        detail.payments.firstOrNull()?.let {
                            DetailValueRow("Payment Method:", it.PaymentMethod)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Items", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray)) {
                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8EAF6)).padding(8.dp)) {
                                Text("Product", modifier = Modifier.weight(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Qty", modifier = Modifier.weight(0.15f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("Unit Price", modifier = Modifier.weight(0.225f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                Text("Subtotal", modifier = Modifier.weight(0.225f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            }
                            detail.items.forEach { line ->
                                Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Text(line.ProductName, modifier = Modifier.weight(0.4f), fontSize = 10.sp)
                                    Text(line.Quantity.toString(), modifier = Modifier.weight(0.15f), fontSize = 10.sp, textAlign = TextAlign.Center)
                                    Text(
                                        String.format(Locale.US, "₱%,.2f", line.UnitPrice.toDoubleOrNull() ?: 0.0),
                                        modifier = Modifier.weight(0.225f), fontSize = 10.sp, textAlign = TextAlign.End
                                    )
                                    Text(
                                        String.format(Locale.US, "₱%,.2f", line.Subtotal.toDoubleOrNull() ?: 0.0),
                                        modifier = Modifier.weight(0.225f), fontSize = 10.sp, textAlign = TextAlign.End
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        val discount = detail.SalesDiscount.toDoubleOrNull() ?: 0.0
                        if (discount > 0) {
                            DetailValueRow("Discount:", "-₱${String.format(Locale.US, "%,.2f", discount)}")
                        }
                        DetailValueRow("Total Amount:", "₱${String.format(Locale.US, "%,.2f", detail.TotalAmount.toDoubleOrNull() ?: 0.0)}", isBold = true)

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add New Customer", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel") }
                    Button(
                        enabled = name.isNotEmpty() && phone.isNotEmpty(),
                        onClick = { onAdd(name, phone, address) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text("Add Customer")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun POSEmployeeScreenPreview() {
    POSEmployeeScreen()
}