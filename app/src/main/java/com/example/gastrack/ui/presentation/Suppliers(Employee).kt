package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.ProductDto
import com.example.gastrack.data.remote.dto.PurchaseOrderDto
import com.example.gastrack.data.remote.dto.SupplierDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.*
import java.util.Locale

@Composable
fun SuppliersEmployeeScreen(
    onBack: () -> Unit = {},
    supplierViewModel: SupplierViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Dashboard") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var supplierBeingEdited by remember { mutableStateOf<SupplierDto?>(null) }

    val actionMessage by supplierViewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            supplierViewModel.clearActionMessage()
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
                .background(StaffPortalButtonBlue)
                .padding(top = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(
                            text = "Suppliers",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            color = Color.White,
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SupplierTabItem("Dashboard", selectedTab == "Dashboard") { selectedTab = "Dashboard" }
                    SupplierTabItem("Products", selectedTab == "Products") { selectedTab = "Products" }
                    SupplierTabItem("Orders", selectedTab == "Orders") { selectedTab = "Orders" }
                    SupplierTabItem("History", selectedTab == "History") { selectedTab = "History" }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = selectedTab, label = "SupplierTabTransition") { tab ->
                when (tab) {
                    "Dashboard" -> SuppliersDashboardTab(
                        supplierViewModel = supplierViewModel,
                        onEditClick = { supplier ->
                            supplierBeingEdited = supplier
                            showEditDialog = true
                        },
                        onAddClick = { showAddDialog = true },
                        onDeleteClick = { supplier -> supplierViewModel.deactivateSupplier(supplier.SupplierID) }
                    )
                    "Products" -> SupplierProductsTab()
                    "Orders" -> SupplierPOTab()
                    "History" -> SupplierDeliveryHistoryTab()
                }
            }
        }
    }

    if (showEditDialog && supplierBeingEdited != null) {
        val supplier = supplierBeingEdited!!
        SupplierEditDialog(
            isEdit = true,
            existing = supplier,
            onDismiss = {
                showEditDialog = false
                supplierBeingEdited = null
            },
            onSave = { name, contactPerson, email, address, contact, leadTime ->
                supplierViewModel.updateSupplier(
                    id = supplier.SupplierID,
                    name = name,
                    contactPerson = contactPerson,
                    email = email,
                    address = address,
                    contact = contact,
                    leadTimeDays = leadTime,
                    status = supplier.Status
                )
                showEditDialog = false
                supplierBeingEdited = null
            }
        )
    }

    if (showAddDialog) {
        SupplierEditDialog(
            isEdit = false,
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, contactPerson, email, address, contact, leadTime ->
                supplierViewModel.addSupplier(name, contactPerson, email, address, contact, leadTime)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SupplierTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        )
        if (isSelected) {
            Box(modifier = Modifier.width(20.dp).height(2.dp).background(Color.White).padding(top = 2.dp))
        }
    }
}

@Composable
fun SuppliersDashboardTab(
    supplierViewModel: SupplierViewModel,
    onEditClick: (SupplierDto) -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (SupplierDto) -> Unit
) {
    val uiState by supplierViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is SupplierUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is SupplierUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load suppliers", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { supplierViewModel.loadSuppliers() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                        Text("Retry")
                    }
                }
            }
            is SupplierUiState.Success -> {
                if (state.suppliers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No suppliers yet. Tap Add Supplier to create one.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.suppliers, key = { it.SupplierID }) { supplier ->
                            SupplierCard(
                                supplier = supplier,
                                onEditClick = { onEditClick(supplier) },
                                onDeleteClick = { onDeleteClick(supplier) }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Add Supplier", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SupplierCard(supplier: SupplierDto, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "SUP-${supplier.SupplierID}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = supplier.SupplierName, fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${supplier.LeadTimeDays} day lead time", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        color = if (supplier.Status == "Active") Color(0xFF4CAF50) else Color(0xFFE57373),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = supplier.Status,
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Contact Person", fontSize = 10.sp, color = Color.Gray)
                        Text(supplier.ContactPerson ?: "—", fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Phone", fontSize = 10.sp, color = Color.Gray)
                        Text(supplier.Contact ?: "—", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SupplierActionItem(Icons.Default.Edit, "Edit", Color(0xFF4CAF50), onClick = onEditClick)
                SupplierActionItem(Icons.Default.Delete, "Remove", Color.Red, onClick = onDeleteClick)
            }
        }
    }
}

@Composable
fun SupplierActionItem(icon: ImageVector, text: String, color: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.clickable { onClick() }.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// Global product catalog, filterable by supplier in the backend if needed later.
@Composable
fun SupplierProductsTab(productViewModel: ProductViewModel = viewModel()) {
    val uiState by productViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ProductUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StaffPortalButtonBlue)
            }
        }
        is ProductUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Couldn't load products: ${state.message}", color = GasTrackRed)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { productViewModel.loadProducts() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                    Text("Retry")
                }
            }
        }
        is ProductUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.products, key = { it.ProductID }) { product ->
                    SupplierProductCard(product)
                }
            }
        }
    }
}

@Composable
fun SupplierProductCard(product: ProductDto) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "P-${product.ProductID}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = product.ProductName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Supplied by ${product.SupplierName}", fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${product.SupplierLeadTimeDays ?: 0} Days", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Surface(color = if (product.Status == "Active") Color(0xFF4CAF50) else Color.Gray, shape = RoundedCornerShape(8.dp)) {
                        Text(text = product.Status, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cost Price", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            String.format(Locale.US, "₱ %,.2f", product.CostPrice.toDoubleOrNull() ?: 0.0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Unit", fontSize = 10.sp, color = Color.Gray)
                        Text(product.Unit, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Purchase orders across all suppliers, from purchaseorder table.
@Composable
fun SupplierPOTab(purchaseOrderViewModel: PurchaseOrderViewModel = viewModel()) {
    val uiState by purchaseOrderViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PurchaseOrderUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StaffPortalButtonBlue)
            }
        }
        is PurchaseOrderUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Couldn't load purchase orders: ${state.message}", color = GasTrackRed)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { purchaseOrderViewModel.loadPurchaseOrders() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                    Text("Retry")
                }
            }
        }
        is PurchaseOrderUiState.Success -> {
            if (state.orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No purchase orders yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.orders, key = { it.PurchaseOrderID }) { po ->
                        SupplierPOCard(po)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierPOCard(po: PurchaseOrderDto) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = po.PONo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = po.SupplierName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = when (po.Status) {
                        "Approved" -> Color(0xFF4CAF50)
                        "Cancelled" -> Color.Red
                        else -> Color(0xFF0D1B6D)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = po.Status, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Amount", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            String.format(Locale.US, "₱ %,.2f", po.TotalAmount.toDoubleOrNull() ?: 0.0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Order Date", fontSize = 10.sp, color = Color.Gray)
                        Text(po.OrderDate.take(10), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Note: your database has no separate "delivery" record for purchase orders —
// approved POs are the closest real signal that stock was received, so we
// reuse the same purchase order list here, filtered to Status = "Approved".
@Composable
fun SupplierDeliveryHistoryTab(purchaseOrderViewModel: PurchaseOrderViewModel = viewModel()) {
    val uiState by purchaseOrderViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PurchaseOrderUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StaffPortalButtonBlue)
            }
        }
        is PurchaseOrderUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Couldn't load delivery history: ${state.message}", color = GasTrackRed)
            }
        }
        is PurchaseOrderUiState.Success -> {
            val delivered = state.orders.filter { it.Status == "Approved" }
            if (delivered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No deliveries received yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(delivered, key = { it.PurchaseOrderID }) { po ->
                        SupplierDeliveryCard(po)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierDeliveryCard(po: PurchaseOrderDto) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = po.PONo, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp)) {
                    Text(text = "RECEIVED", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Supplier", fontSize = 10.sp, color = Color.Gray)
                        Text(po.SupplierName, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Total Value", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            String.format(Locale.US, "₱ %,.2f", po.TotalAmount.toDoubleOrNull() ?: 0.0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierEditDialog(
    isEdit: Boolean,
    existing: SupplierDto?,
    onDismiss: () -> Unit,
    onSave: (name: String, contactPerson: String, email: String, address: String, contact: String, leadTimeDays: Int) -> Unit
) {
    var name by remember { mutableStateOf(existing?.SupplierName ?: "") }
    var contactPerson by remember { mutableStateOf(existing?.ContactPerson ?: "") }
    var email by remember { mutableStateOf(existing?.Email ?: "") }
    var address by remember { mutableStateOf(existing?.Address ?: "") }
    var contact by remember { mutableStateOf(existing?.Contact ?: "") }
    var leadTimeDays by remember { mutableStateOf((existing?.LeadTimeDays ?: 0).toString()) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    if (isEdit) "Edit Supplier" else "Add Supplier",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Supplier Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Contact Person") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = leadTimeDays,
                    onValueChange = { input -> if (input.all { it.isDigit() }) leadTimeDays = input },
                    label = { Text("Lead Time (days)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2))
                    ) {
                        Text("Cancel", color = Color.Black)
                    }
                    Button(
                        enabled = name.isNotBlank(),
                        onClick = {
                            onSave(name, contactPerson, email, address, contact, leadTimeDays.toIntOrNull() ?: 0)
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuppliersEmployeeScreenPreview() {
    SuppliersEmployeeScreen()
}