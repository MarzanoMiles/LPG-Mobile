package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.DeliveryDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.DeliveryUiState
import com.example.gastrack.viewmodel.DeliveryViewModel
import java.util.Locale

@Composable
fun OrdersEmployeeScreen(
    onBack: () -> Unit = {},
    onNavigateToMaps: (String) -> Unit = {},
    deliveryViewModel: DeliveryViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Pending") }

    val uiState by deliveryViewModel.uiState.collectAsState()
    val actionMessage by deliveryViewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            deliveryViewModel.clearActionMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFE))
    ) {
        // --- Modern Header with Gradient ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(StaffPortalBlue, Color(0xFF000540))
                    ),
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Assigned Orders",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                )
                Text(
                    text = "Logistics Operations",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = uiState) {
            is DeliveryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalBlue)
                }
            }
            is DeliveryUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load orders", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { deliveryViewModel.loadDeliveries() },
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is DeliveryUiState.Success -> {
                val pendingOrders = state.deliveries.filter { it.DeliveryStatus == "Pending" }
                val inTransitOrders = state.deliveries.filter { it.DeliveryStatus == "In Transit" }
                val historyOrders = state.deliveries.filter { it.DeliveryStatus == "Delivered" }

                // --- Material Tab Toggle ---
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F3F4)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OrderTabToggle(
                            text = "Pending (${pendingOrders.size})",
                            isSelected = selectedTab == "Pending",
                            modifier = Modifier.weight(1f)
                        ) { selectedTab = "Pending" }

                        OrderTabToggle(
                            text = "In Transit (${inTransitOrders.size})",
                            isSelected = selectedTab == "In Transit",
                            modifier = Modifier.weight(1f)
                        ) { selectedTab = "In Transit" }

                        OrderTabToggle(
                            text = "History",
                            isSelected = selectedTab == "History",
                            modifier = Modifier.weight(1f)
                        ) { selectedTab = "History" }
                    }
                }

                // --- Tab Content ---
                Box(modifier = Modifier.weight(1f)) {
                    val currentList = when (selectedTab) {
                        "Pending" -> pendingOrders
                        "In Transit" -> inTransitOrders
                        else -> historyOrders
                    }

                    if (currentList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No orders in this category", color = TextGray, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(currentList, key = { it.DeliveryID }) { delivery ->
                                DeliveryOrderCard(
                                    delivery = delivery,
                                    onViewMaps = { onNavigateToMaps(delivery.DeliveryID.toString()) },
                                    onStartDelivery = {
                                        deliveryViewModel.updateStatus(delivery.DeliveryID, "In Transit")
                                        selectedTab = "In Transit"
                                    },
                                    onMarkDelivered = {
                                        deliveryViewModel.updateStatus(delivery.DeliveryID, "Delivered")
                                        selectedTab = "History"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTabToggle(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) StaffPortalBlue else TextGray,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun DeliveryOrderCard(
    delivery: DeliveryDto,
    onViewMaps: () -> Unit,
    onStartDelivery: () -> Unit,
    onMarkDelivered: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Yellow Accent Border (Logistics Signature)
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFD600), Color(0xFFFF9100))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = delivery.DRNo,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Text(
                            text = "Sale: ${delivery.SaleNo} • ${delivery.SaleDate.take(10)}",
                            fontSize = 13.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    val (badgeColor, textColor) = when (delivery.DeliveryStatus) {
                        "Delivered" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        "In Transit" -> Color(0xFFE3F2FD) to StaffPortalBlue
                        else -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
                    }
                    Surface(color = badgeColor, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = delivery.DeliveryStatus.uppercase(),
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DeliveryInfoRow(delivery.CustomerName)
                    DeliveryInfoRow(delivery.ItemSummary ?: "${delivery.ItemCount} item(s)")
                    DeliveryInfoRow(delivery.DeliveryAddress)
                    DeliveryInfoRow("₱${String.format(Locale.US, "%,.2f", delivery.TotalAmount.toDoubleOrNull() ?: 0.0)}")
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (delivery.DeliveryStatus) {
                    "Pending" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onViewMaps,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, StaffPortalBlue)
                            ) {
                                Text("View Route", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StaffPortalBlue)
                            }
                            Button(
                                onClick = onStartDelivery,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text("Dispatch", fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 15.sp)
                            }
                        }
                    }
                    "In Transit" -> {
                        Button(
                            onClick = onMarkDelivered,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mark as Delivered", fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 16.sp)
                            }
                        }
                    }
                    "Delivered" -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF1F3F4),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order Successfully Delivered",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryInfoRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF8F9FA)
        ) {}
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrdersEmployeeScreenPreview() {
    OrdersEmployeeScreen()
}