package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.StaffPortalButtonBlue

data class SalesRecord(
    val id: String,
    val date: String,
    val amount: String,
    val type: String,
    val cashier: String,
    val orderId: String
)

@Composable
fun SalesEmployeeScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Dashboard") }
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
                            text = "Sales Management",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SalesTabItem("Dashboard", selectedTab == "Dashboard") { selectedTab = "Dashboard" }
                    SalesTabItem("Records", selectedTab == "Records") { selectedTab = "Records" }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Crossfade(targetState = selectedTab, label = "SalesTabTransition") { tab ->
            when (tab) {
                "Dashboard" -> SalesDashboardTab()
                "Records" -> SalesRecordsTab(onViewClick = { showDetailDialog = true })
            }
        }
    }

    if (showDetailDialog) {
        SalesDetailDialog(onDismiss = { showDetailDialog = false })
    }
}

@Composable
fun SalesTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    )
}

@Composable
fun SalesDashboardTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Today's Performance Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(StaffPortalButtonBlue, StaffPortalButtonBlue.copy(alpha = 0.7f))
                    ),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Column {
                Text(text = "Today's Performance", color = Color.White, fontSize = 16.sp)
                Text(text = "₱ 15,000.00", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stats Grid
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SalesStatCard("TODAY'S SALE", "₱ 15,000.00", Color(0xFF3F51B5), modifier = Modifier.weight(1f))
                SalesStatCard("TRANSACTIONS", "50", Color(0xFF81C784), modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SalesStatCard("AVG ORDER", "₱ 500", Color(0xFFFFD54F), modifier = Modifier.weight(1f))
                SalesStatCard("PEAK HOUR", "12:00PM", Color(0xFFBA68C8), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SalesStatCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Accent Dot
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .align(Alignment.TopEnd)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

@Composable
fun SalesRecordsTab(onViewClick: () -> Unit) {
    val records = listOf(
        SalesRecord("SL-001", "01/05/2026 10:35 AM", "₱243.00", "Delivery", "U-003", "O-001"),
        SalesRecord("SL-002", "01/05/2026 10:22 AM", "₱1000.00", "Delivery", "U-003", "O-002"),
        SalesRecord("SL-003", "01/04/2026 3:30 PM", "₱550.00", "Delivery", "U-003", "O-003"),
        SalesRecord("SL-004", "01/03/2026 10:35 AM", "₱105.00", "Delivery", "U-003", "O-004")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(records) { record ->
            SalesRecordCard(record, onViewClick)
        }
    }
}

@Composable
fun SalesRecordCard(record: SalesRecord, onViewClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = record.id, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(text = record.date, fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = record.amount, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Surface(color = StaffPortalBlue, shape = RoundedCornerShape(8.dp)) {
                        Text(text = record.type, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gray Detail Box
            Surface(
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cashier", fontSize = 10.sp, color = Color.Gray)
                        Text(record.cashier, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Order ID", fontSize = 10.sp, color = Color.Gray)
                        Text(record.orderId, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SalesActionItem(Icons.Default.Description, "View", Color(0xFFFFD54F), onClick = onViewClick)
                SalesActionItem(Icons.Default.Print, "Print", Color.Black)
                SalesActionItem(Icons.Default.Delete, "Delete", Color.Red)
            }
        }
    }
}

@Composable
fun SalesActionItem(icon: ImageVector, text: String, color: Color, onClick: () -> Unit = {}) {
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
fun SalesDetailDialog(onDismiss: () -> Unit) {
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
                Text("Sales Information", fontSize = 14.sp, color = Color.Gray)
                Text(
                    "Store Name\nSales",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailValueRow("Sales ID:", "SL-001")
                DetailValueRow("Date:", "01/05/2026 10:35 AM")
                DetailValueRow("Cashier:", "Teodore Metro")
                DetailValueRow("Order ID:", "O-001")

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Items", fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Table
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Product Name", modifier = Modifier.weight(0.4f), fontSize = 10.sp, color = Color.Gray)
                        Text("Qty", modifier = Modifier.weight(0.1f), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Text("Cost Price", modifier = Modifier.weight(0.25f), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.End)
                        Text("Subtotal", modifier = Modifier.weight(0.25f), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.End)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Gasul LPG 11kg", modifier = Modifier.weight(0.4f), fontSize = 10.sp)
                        Text("1", modifier = Modifier.weight(0.1f), fontSize = 10.sp, textAlign = TextAlign.Center)
                        Text("₱ 950.00", modifier = Modifier.weight(0.25f), fontSize = 10.sp, textAlign = TextAlign.End)
                        Text("₱ 950.00", modifier = Modifier.weight(0.25f), fontSize = 10.sp, textAlign = TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Total Summary", fontWeight = FontWeight.Black, fontSize = 16.sp)
                DetailValueRow("Subtotal:", "₱ 0.00")
                DetailValueRow("VAT (12%):", "₱ 0.00")
                DetailValueRow("Discount:", "₱ 0.00")
                DetailValueRow("TOTAL AMOUNT:", "₱ 0.00", isBold = true)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Payment Details", fontWeight = FontWeight.Black, fontSize = 16.sp)
                DetailValueRow("Method:", "Cash")
                DetailValueRow("Amount Paid:", "₱ 0.00")
                DetailValueRow("Change:", "₱ 0.00")
                Spacer(modifier = Modifier.height(8.dp))
                DetailValueRow("Status:", "Paid")
                DetailValueRow("Paid At:", "01/05/2026 11:00 AM")
                DetailValueRow("Reference No:", "N/A")

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2))
                ) {
                    Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SalesEmployeeScreenPreview() {
    SalesEmployeeScreen()
}
