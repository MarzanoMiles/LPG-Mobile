package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.SaleDetailDto
import com.example.gastrack.data.remote.dto.SalesListItemDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.*
import java.util.Locale

@Composable
fun SalesEmployeeScreen(
    onBack: () -> Unit = {},
    salesListViewModel: SalesListViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Dashboard") }
    var selectedSaleId by remember { mutableStateOf<Int?>(null) }

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
                "Dashboard" -> SalesDashboardTab(salesListViewModel = salesListViewModel)
                "Records" -> SalesRecordsTab(
                    salesListViewModel = salesListViewModel,
                    onViewClick = { saleId -> selectedSaleId = saleId }
                )
            }
        }
    }

    if (selectedSaleId != null) {
        SalesDetailDialog(
            saleId = selectedSaleId!!,
            onDismiss = { selectedSaleId = null }
        )
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
fun SalesDashboardTab(salesListViewModel: SalesListViewModel) {
    val statsState by salesListViewModel.statsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        when (val state = statsState) {
            is SalesStatsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is SalesStatsUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load sales stats", fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { salesListViewModel.loadStats() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                        Text("Retry")
                    }
                }
            }
            is SalesStatsUiState.Success -> {
                val stats = state.stats

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
                        Text(
                            text = "₱ ${String.format(Locale.US, "%,.2f", stats.todaysSalesTotal)}",
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Stats Grid
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SalesStatCard("TODAY'S SALE", "₱ ${String.format(Locale.US, "%,.2f", stats.todaysSalesTotal)}", Color(0xFF3F51B5), modifier = Modifier.weight(1f))
                        SalesStatCard("TRANSACTIONS", stats.todaysTransactionCount.toString(), Color(0xFF81C784), modifier = Modifier.weight(1f))
                    }
                    SalesStatCard("AVG ORDER", "₱ ${String.format(Locale.US, "%,.0f", stats.averageOrderValue)}", Color(0xFFFFD54F), modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(32.dp))
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
fun SalesRecordsTab(
    salesListViewModel: SalesListViewModel,
    onViewClick: (Int) -> Unit
) {
    val listState by salesListViewModel.listState.collectAsState()

    when (val state = listState) {
        is SalesListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StaffPortalButtonBlue)
            }
        }
        is SalesListUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Couldn't load sales records", fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(state.message, fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { salesListViewModel.loadSalesList() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                    Text("Retry")
                }
            }
        }
        is SalesListUiState.Success -> {
            if (state.sales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No sales recorded yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.sales, key = { it.SaleID }) { record ->
                        SalesRecordCard(record, onViewClick = { onViewClick(record.SaleID) })
                    }
                }
            }
        }
    }
}

@Composable
fun SalesRecordCard(record: SalesListItemDto, onViewClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = record.SaleNo, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(text = record.SaleDate.take(19).replace("T", " "), fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₱${String.format(Locale.US, "%,.2f", record.TotalAmount.toDoubleOrNull() ?: 0.0)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Surface(color = StaffPortalBlue, shape = RoundedCornerShape(8.dp)) {
                        Text(text = record.CustomerName, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
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
                        Text("Cashier", fontSize = 10.sp, color = Color.Gray)
                        Text("${record.FirstName} ${record.LastName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Order ID", fontSize = 10.sp, color = Color.Gray)
                        Text("O-${record.OrderID}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SalesActionItem(Icons.Default.Description, "View Receipt", Color(0xFFFFD54F), onClick = onViewClick)
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
fun SalesDetailDialog(
    saleId: Int,
    onDismiss: () -> Unit,
    saleDetailViewModel: SaleDetailViewModel = viewModel()
) {
    val uiState by saleDetailViewModel.uiState.collectAsState()

    LaunchedEffect(saleId) {
        saleDetailViewModel.loadDetail(saleId)
    }

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
                when (val state = uiState) {
                    is SaleDetailUiState.Loading, SaleDetailUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = StaffPortalButtonBlue)
                        }
                    }
                    is SaleDetailUiState.Error -> {
                        Text("Couldn't load receipt: ${state.message}", color = GasTrackRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2))) {
                            Text("Close", color = Color.Black)
                        }
                    }
                    is SaleDetailUiState.Success -> {
                        val detail = state.detail

                        Text("Sales Receipt", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            detail.SaleNo,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailValueRow("Sale ID:", "SL-${detail.SaleID}")
                        DetailValueRow("Date:", detail.SaleDate.take(19).replace("T", " "))
                        DetailValueRow("Order ID:", "O-${detail.OrderID}")

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Items", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("Product Name", modifier = Modifier.weight(0.4f), fontSize = 10.sp, color = Color.Gray)
                                Text("Qty", modifier = Modifier.weight(0.1f), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                Text("Unit Price", modifier = Modifier.weight(0.25f), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.End)
                                Text("Subtotal", modifier = Modifier.weight(0.25f), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.End)
                            }
                            detail.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(item.ProductName, modifier = Modifier.weight(0.4f), fontSize = 10.sp)
                                    Text(item.Quantity.toString(), modifier = Modifier.weight(0.1f), fontSize = 10.sp, textAlign = TextAlign.Center)
                                    Text(
                                        "₱ ${String.format(Locale.US, "%,.2f", item.UnitPrice.toDoubleOrNull() ?: 0.0)}",
                                        modifier = Modifier.weight(0.25f), fontSize = 10.sp, textAlign = TextAlign.End
                                    )
                                    Text(
                                        "₱ ${String.format(Locale.US, "%,.2f", item.Subtotal.toDoubleOrNull() ?: 0.0)}",
                                        modifier = Modifier.weight(0.25f), fontSize = 10.sp, textAlign = TextAlign.End
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        val discount = detail.SalesDiscount.toDoubleOrNull() ?: 0.0
                        val itemsTotal = detail.items.sumOf { it.Subtotal.toDoubleOrNull() ?: 0.0 }

                        Text("Total Summary", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        DetailValueRow("Subtotal:", "₱ ${String.format(Locale.US, "%,.2f", itemsTotal)}")
                        DetailValueRow("Discount:", "-₱ ${String.format(Locale.US, "%,.2f", discount)}")
                        DetailValueRow("TOTAL AMOUNT:", "₱ ${String.format(Locale.US, "%,.2f", detail.TotalAmount.toDoubleOrNull() ?: 0.0)}", isBold = true)

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Payment Details", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        detail.payments.firstOrNull()?.let { payment ->
                            DetailValueRow("Method:", payment.PaymentMethod)
                            DetailValueRow("Amount Paid:", "₱ ${String.format(Locale.US, "%,.2f", payment.AmountPaid.toDoubleOrNull() ?: 0.0)}")
                            DetailValueRow("Paid At:", payment.PaymentDate.take(19).replace("T", " "))
                        } ?: run {
                            Text("No payment record found", fontSize = 12.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2))
                        ) {
                            Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
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