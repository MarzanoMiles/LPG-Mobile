package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.example.gastrack.R
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.StaffPortalButtonBlue
import java.util.Locale

@Composable
fun POSEmployeeScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Products") }
    var showPayPopup by remember { mutableStateOf(false) }
    var showOrderDetails by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    // Cart State: Map of Product ID to Quantity
    val cart = remember { mutableStateMapOf<Int, Int>() }
    var selectedCustomer by remember { mutableStateOf<POSCustomerData?>(null) }

    // Customers State
    val customers = remember { mutableStateListOf(
        POSCustomerData(1, "Juan Dela Cruz", "0917-123-4567", "45 Mabini St, Brgy. San Jose, Navotas City", "LAST ORDER: 2 DAYS AGO", "JD"),
        POSCustomerData(2, "Maria Clara", "0918-987-6543", "12 Rizal Ave, Brgy. Concepcion, Malabon City", "LAST ORDER: 1 WEEK AGO", "MC"),
        POSCustomerData(3, "Andres Bonifacio", "0917-123-4567", "8 Bonifacio St, Monumento, Caloocan City", "LAST ORDER: 1 MONTH AGO", "AB")
    ) }

    val products = listOf(
        POSProductData(1, "11kg Standard", "Refill • Stock: 15", 950.0, R.drawable.tank_11kg, true),
        POSProductData(2, "2.7kg Camping Cylinder", "Out of Stock", 280.0, R.drawable.tank_2_7kg, false),
        POSProductData(3, "50kg Commercial Refill", "Out of Stock", 4500.0, R.drawable.tank_50kg, false)
    )

    val totalAmount = products.sumOf { product ->
        (cart[product.id] ?: 0) * product.price
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

                    // --- Tabs ---
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
                    POSProductsTab(
                        products = products,
                        cart = cart,
                        onUpdateQuantity = { id, qty ->
                            if (qty <= 0) cart.remove(id) else cart[id] = qty
                        }
                    )
                } else {
                    POSCustomersTab(
                        customers = customers,
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
            onDismiss = { showPayPopup = false },
            onConfirm = {
                showPayPopup = false
                showOrderDetails = true
            }
        )
    }

    if (showOrderDetails) {
        OrderDetailsPopup(
            cart = cart,
            products = products,
            customer = selectedCustomer,
            onDismiss = {
                showOrderDetails = false
                cart.clear()
            }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onAdd = { name, phone, address ->
                val newId = (customers.maxOfOrNull { it.id } ?: 0) + 1
                val initials = name.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()
                customers.add(POSCustomerData(newId, name, phone, address, "NEWLY ADDED", initials))
                showAddCustomerDialog = false
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
    products: List<POSProductData>,
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
        items(products) { product ->
            POSProductCard(
                product = product,
                quantity = cart[product.id] ?: 0,
                onUpdateQuantity = { onUpdateQuantity(product.id, it) }
            )
        }
    }
}

data class POSProductData(val id: Int, val name: String, val status: String, val price: Double, val imageRes: Int, val inStock: Boolean)

@Composable
fun POSProductCard(
    product: POSProductData,
    quantity: Int,
    onUpdateQuantity: (Int) -> Unit
) {
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
            Image(
                painter = painterResource(id = product.imageRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)).padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = product.status,
                    color = if (product.inStock) Color.Gray else Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = String.format(Locale.US, "₱%,.2f", product.price),
                    color = StaffPortalButtonBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            if (product.inStock) {
                if (quantity > 0) {
                    POSQuantitySelector(
                        quantity = quantity,
                        onUpdateQuantity = onUpdateQuantity
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
    customers: List<POSCustomerData>,
    selectedCustomer: POSCustomerData?,
    onCustomerSelected: (POSCustomerData?) -> Unit,
    onAddNewClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filteredCustomers) { customer ->
                POSCustomerCard(
                    customer = customer,
                    isSelected = selectedCustomer?.id == customer.id,
                    onSelect = { onCustomerSelected(customer) }
                )
            }
        }
    }
}

data class POSCustomerData(val id: Int, val name: String, val phone: String, val address: String, val lastOrder: String, val initials: String)

@Composable
fun POSCustomerCard(customer: POSCustomerData, isSelected: Boolean, onSelect: () -> Unit) {
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
                        Text(text = customer.initials, color = StaffPortalBlue, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = customer.phone, color = Color.Gray, fontSize = 12.sp)
                }
                RadioButton(selected = isSelected, onClick = onSelect)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = customer.address, fontSize = 12.sp, color = Color.Gray)
            Text(text = customer.lastOrder, fontSize = 9.sp, color = if (isSelected) StaffPortalBlue else Color.LightGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PayPopup(totalAmount: Double, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var amountCollected by remember { mutableStateOf("") }
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
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var saveReceipt by remember { mutableStateOf(true) }
                    Checkbox(checked = saveReceipt, onCheckedChange = { saveReceipt = it })
                    Text("Save Receipt", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel") }
                    Button(
                        enabled = collected >= totalAmount && totalAmount > 0,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text("Okay")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentDetailRow(label: String, value: String, color: Color = Color.Black, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 16.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 16.sp, color = color, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun OrderDetailsPopup(
    cart: Map<Int, Int>,
    products: List<POSProductData>,
    customer: POSCustomerData?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Order Details", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.height(16.dp))

                DetailValueRow("Order ID:", "O-001")
                DetailValueRow("Order Type:", if (customer == null) "Walk-in" else "Delivery")
                DetailValueRow("Date:", "01/01/2026 2:14:05 PM")
                DetailValueRow("Order Status:", "Completed")

                Spacer(modifier = Modifier.height(16.dp))

                // Table
                Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray)) {
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8EAF6)).padding(8.dp)) {
                        Text("Product Name", modifier = Modifier.weight(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Qty", modifier = Modifier.weight(0.1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Unit Price", modifier = Modifier.weight(0.25f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text("Subtotal", modifier = Modifier.weight(0.25f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }
                    var subtotal = 0.0
                    var itemCount = 0
                    products.filter { cart.containsKey(it.id) }.forEach { product ->
                        val qty = cart[product.id] ?: 0
                        val lineTotal = qty * product.price
                        subtotal += lineTotal
                        itemCount += qty
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(product.name, modifier = Modifier.weight(0.4f), fontSize = 10.sp)
                            Text(qty.toString(), modifier = Modifier.weight(0.1f), fontSize = 10.sp, textAlign = TextAlign.Center)
                            Text(String.format(Locale.US, "₱%,.2f", product.price), modifier = Modifier.weight(0.25f), fontSize = 10.sp, textAlign = TextAlign.End)
                            Text(String.format(Locale.US, "₱%,.2f", lineTotal), modifier = Modifier.weight(0.25f), fontSize = 10.sp, textAlign = TextAlign.End)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                DetailValueRow("Total Items:", cart.values.sum().toString())
                DetailValueRow("Subtotal:", String.format(Locale.US, "₱ %,.2f", cart.entries.sumOf { entry -> (products.find { it.id == entry.key }?.price ?: 0.0) * entry.value }))
                DetailValueRow("Delivery:", "₱ 0.00")
                DetailValueRow("Total Amount:", String.format(Locale.US, "₱ %,.2f", cart.entries.sumOf { entry -> (products.find { it.id == entry.key }?.price ?: 0.0) * entry.value }), isBold = true)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Customer Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Name: ${customer?.name ?: "Walk-in"}", fontSize = 12.sp)
                if (customer != null) {
                    Text("Phone: ${customer.phone}", fontSize = 12.sp)
                    Text("Address: ${customer.address}", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                DetailValueRow("Payment ID:", "PM-001")
                DetailValueRow("Payment Method:", "Cash")

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                ) {
                    Text("Close & Print Receipt")
                }
            }
        }
    }
}

@Composable
fun DetailValueRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
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
