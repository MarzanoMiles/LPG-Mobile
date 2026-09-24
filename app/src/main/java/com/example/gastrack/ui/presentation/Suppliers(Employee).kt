package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gastrack.ui.theme.StaffPortalButtonBlue
import java.util.Locale

@Composable
fun SuppliersEmployeeScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Dashboard") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val suppliers = remember { mutableStateListOf(
        SupplierData("SL-001", "ABC Company", "#59 Kahoy St", "Active", "Juan", "0956478512354"),
        SupplierData("SL-002", "XYZ Inc", "#59 Kahoy St", "Active", "Juan", "0956478512354"),
        SupplierData("SL-003", "DEF Company", "#59 Kahoy St", "Inactive", "Juan", "0956478512354"),
        SupplierData("SL-004", "NOV Inc", "#59 Kahoy St", "Active", "Juan", "0956478512354")
    ) }

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

                // --- Tabs ---
                // Based on reference images, there are 4 main views
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
                        suppliers = suppliers,
                        onEditClick = { showEditDialog = true },
                        onAddClick = { showAddDialog = true }
                    )
                    "Products" -> SupplierProductsTab()
                    "Orders" -> SupplierPOTab()
                    "History" -> SupplierDeliveryHistoryTab()
                }
            }
        }
    }

    if (showEditDialog || showAddDialog) {
        SupplierEditDialog(
            isEdit = showEditDialog,
            onDismiss = {
                showEditDialog = false
                showAddDialog = false
            },
            onSave = { name, address ->
                if (showAddDialog) {
                    val newId = "SL-00${suppliers.size + 1}"
                    suppliers.add(SupplierData(newId, name, address, "Active", "Juan", "0956478512354"))
                }
                showEditDialog = false
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
    suppliers: List<SupplierData>,
    onEditClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterDropdown("All Performance", Modifier.weight(1f))
            FilterDropdown("All Status", Modifier.weight(1f))
            FilterDropdown("All Location", Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(suppliers) { supplier ->
                SupplierCard(supplier, onEditClick)
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
fun FilterDropdown(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text, fontSize = 10.sp, color = Color.Black, maxLines = 1)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

data class SupplierData(val id: String, val name: String, val address: String, val status: String, val contactPerson: String, val phone: String)

@Composable
fun SupplierCard(supplier: SupplierData, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = supplier.id, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = supplier.name, fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = supplier.address, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Surface(
                        color = if (supplier.status == "Active") Color(0xFF4CAF50) else Color(0xFFE57373),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = supplier.status,
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
                        Text(supplier.contactPerson, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Phone", fontSize = 10.sp, color = Color.Gray)
                        Text(supplier.phone, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SupplierActionItem(Icons.Default.Description, "View", Color(0xFFFFD54F))
                SupplierActionItem(Icons.Default.Edit, "Edit", Color(0xFF4CAF50), onClick = onEditClick)
                SupplierActionItem(Icons.Default.Delete, "Delete", Color.Red)
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

@Composable
fun SupplierProductsTab() {
    val products = listOf(
        SupplierProductData("P-001", "Gas LPG 2.7kg", "2 Days", "Active", 243.0, 10),
        SupplierProductData("P-002", "Gas LPG 7kg", "2 Days", "Active", 603.0, 10),
        SupplierProductData("P-003", "Gas LPG 11kg", "2 Days", "Active", 921.0, 10),
        SupplierProductData("P-009", "Gas LPG 22kg", "2 Days", "Active", 1726.0, 10),
        SupplierProductData("P-010", "Gas LPG 50kg", "3 Days", "Active", 3964.0, 10)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            SupplierProductCard(product)
        }
    }
}

data class SupplierProductData(val id: String, val name: String, val leadTime: String, val status: String, val cost: Double, val minQty: Int)

@Composable
fun SupplierProductCard(product: SupplierProductData) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = product.id, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = product.leadTime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp)) {
                        Text(text = product.status, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cost Price", fontSize = 10.sp, color = Color.Gray)
                        Text(String.format(Locale.US, "₱ %,.2f", product.cost), fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Minimum Order Qty", fontSize = 10.sp, color = Color.Gray)
                        Text(product.minQty.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierPOTab() {
    val orders = listOf(
        SupplierPOData("P-001", "01/05/2026", "SENT", 4860.0, 20, "01/07/2026"),
        SupplierPOData("P-002", "01/05/2026", "SENT", 12060.0, 20, "01/07/2026"),
        SupplierPOData("P-003", "01/05/2026", "SENT", 27640.0, 30, "01/07/2026"),
        SupplierPOData("P-009", "01/05/2026", "SENT", 34520.0, 20, "01/08/2026"),
        SupplierPOData("P-010", "01/05/2026", "SENT", 39640.0, 10, "01/12/2026")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { po ->
            SupplierPOCard(po)
        }
    }
}

data class SupplierPOData(val id: String, val date: String, val status: String, val total: Double, val qty: Int, val expected: String)

@Composable
fun SupplierPOCard(po: SupplierPOData) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = po.id, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = po.date, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Surface(color = Color(0xFF0D1B6D), shape = RoundedCornerShape(8.dp)) {
                    Text(text = po.status, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Amount", fontSize = 10.sp, color = Color.Gray)
                        Text(String.format(Locale.US, "₱ %,.2f", po.total), fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Quantity", fontSize = 10.sp, color = Color.Gray)
                        Text(po.qty.toString(), fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Expected Date", fontSize = 10.sp, color = Color.Gray)
                        Text(po.expected, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierDeliveryHistoryTab() {
    val deliveries = listOf(
        SupplierDeliveryData("SD-001", "P-001", "RECEIVED", "01/07/2026", 20, "U-001"),
        SupplierDeliveryData("SD-002", "P-002", "RECEIVED", "01/07/2026", 20, "U-002"),
        SupplierDeliveryData("SD-003", "P-003", "RECEIVED", "01/07/2026", 30, "U-002"),
        SupplierDeliveryData("SD-004", "P-009", "RECEIVED", "01/08/2026", 20, "U-002"),
        SupplierDeliveryData("SD-005", "P-010", "RECEIVED", "01/12/2026", 10, "U-001")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(deliveries) { delivery ->
            SupplierDeliveryCard(delivery)
        }
    }
}

data class SupplierDeliveryData(val id: String, val productId: String, val status: String, val date: String, val qty: Int, val receivedBy: String)

@Composable
fun SupplierDeliveryCard(delivery: SupplierDeliveryData) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = delivery.id, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = delivery.productId, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp)) {
                        Text(text = delivery.status, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Date Delivered", fontSize = 10.sp, color = Color.Gray)
                        Text(delivery.date, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Quantity", fontSize = 10.sp, color = Color.Gray)
                        Text(delivery.qty.toString(), fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Received By", fontSize = 10.sp, color = Color.Gray)
                        Text(delivery.receivedBy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierEditDialog(isEdit: Boolean, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                var name by remember { mutableStateOf("") }
                var address by remember { mutableStateOf("") }

                Text("Supplier Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("BASIC INFORMATION", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SupplierDetailField("Supplier ID", "S-001", Modifier.weight(1f))
                    Column(Modifier.weight(2f)) {
                        Text("Full Name", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ABC Company", color = Color.LightGray) },
                            shape = RoundedCornerShape(4.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SupplierDetailField("Supplier Type", "Manufacturer", Modifier.weight(1f))
                    SupplierDetailField("Default Lead Times", "Enter Number", Modifier.weight(1f))
                    SupplierDetailField("Status", "Active", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("CONTACT INFORMATION", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SupplierDetailField("Contact Person", "Juan Dela Cruz", Modifier.weight(1f))
                    SupplierDetailField("Email Address", "abccompany@supplier.com", Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SupplierDetailField("Phone Number", "09875412558", Modifier.weight(1f))
                    Column(Modifier.weight(1f)) {
                        Text("Address", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("146 Malabon City", color = Color.LightGray) },
                            shape = RoundedCornerShape(4.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                    }
                }

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
                        onClick = { onSave(name, address) },
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

@Composable
fun SupplierDetailField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(value, fontSize = 11.sp, color = if (value.contains("Enter")) Color.LightGray else Color.Black)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuppliersEmployeeScreenPreview() {
    SuppliersEmployeeScreen()
}
