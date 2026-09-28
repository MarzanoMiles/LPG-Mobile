package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.PurchaseOrderDto
import com.example.gastrack.data.remote.dto.RestockRecommendationDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.*
import java.util.Locale

enum class RestockingState {
    STANDARD, SMART
}

@Composable
fun RestockingEmployeeScreen(
    onBack: () -> Unit = {},
    restockingViewModel: RestockingViewModel = viewModel()
) {
    var currentState by remember { mutableStateOf(RestockingState.STANDARD) }
    var confirmingApprovePOId by remember { mutableStateOf<Int?>(null) }
    var confirmingApproveRestockId by remember { mutableStateOf<Int?>(null) }

    val actionMessage by restockingViewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            restockingViewModel.clearActionMessage()
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
                        text = if (currentState == RestockingState.STANDARD) "Restocking" else "Smart Restocking",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                    )
                    Text(
                        text = "Admin",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        currentState = if (currentState == RestockingState.STANDARD) RestockingState.SMART else RestockingState.STANDARD
                    },
                    modifier = Modifier.background(
                        if (currentState == RestockingState.SMART) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Smart Restocking",
                        tint = if (currentState == RestockingState.SMART) Color.Yellow else Color.White
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (currentState == RestockingState.STANDARD) {
                RestockingStandardContent(
                    restockingViewModel = restockingViewModel,
                    onApproveClick = { poId -> confirmingApprovePOId = poId }
                )
            } else {
                SmartRestockingContent(
                    restockingViewModel = restockingViewModel,
                    onApproveClick = { restockId -> confirmingApproveRestockId = restockId }
                )
            }
        }
    }

    confirmingApprovePOId?.let { poId ->
        RestockingConfirmPopup(
            title = "Confirm Purchase Order",
            message = "Are you sure you want to approve this purchase order? Once approved, stock levels will be updated immediately.",
            onDismiss = { confirmingApprovePOId = null },
            onConfirm = {
                restockingViewModel.approvePurchaseOrder(poId)
                confirmingApprovePOId = null
            }
        )
    }

    confirmingApproveRestockId?.let { restockId ->
        RestockingConfirmPopup(
            title = "Approve & Generate PO",
            message = "This will create a new purchase order from this AI recommendation. Continue?",
            onDismiss = { confirmingApproveRestockId = null },
            onConfirm = {
                restockingViewModel.approveRecommendation(restockId)
                confirmingApproveRestockId = null
            }
        )
    }
}

@Composable
fun RestockingStandardContent(
    restockingViewModel: RestockingViewModel,
    onApproveClick: (Int) -> Unit
) {
    val uiState by restockingViewModel.pendingPOState.collectAsState()

    when (val state = uiState) {
        is PendingPOUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StaffPortalBlue)
            }
        }
        is PendingPOUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Couldn't load purchase orders", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(state.message, fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { restockingViewModel.loadPendingPurchaseOrders() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue)) {
                    Text("Retry")
                }
            }
        }
        is PendingPOUiState.Success -> {
            if (state.orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No pending purchase orders", color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Switch to Smart Restocking to generate one from low-stock items", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    state.orders.forEach { po ->
                        RestockingPOCard(po = po, onApproveClick = { onApproveClick(po.PurchaseOrderID) })
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RestockingPOCard(po: PurchaseOrderDto, onApproveClick: () -> Unit) {
    RestockingSectionCard(title = "Purchase Order ${po.PONo}") {
        RestockingDetailRow("Supplier", po.SupplierName)
        RestockingDetailRow("Order Date", po.OrderDate.take(10))
        RestockingDetailRow("Status", po.Status)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Total Amount:", fontWeight = FontWeight.Bold, color = StaffPortalBlue)
            Text(
                text = "₱ ${String.format(Locale.US, "%,.2f", po.TotalAmount.toDoubleOrNull() ?: 0.0)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = StaffPortalBlue
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onApproveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Approve & Confirm Order",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun SmartRestockingContent(
    restockingViewModel: RestockingViewModel,
    onApproveClick: (Int) -> Unit
) {
    val uiState by restockingViewModel.recommendationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- AI Recommendation Header ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            color = StaffPortalButtonBlue.copy(alpha = 0.1f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StaffPortalButtonBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI Recommendation",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = StaffPortalBlue
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Products currently at or below their reorder level. Generate a new scan any time stock changes.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { restockingViewModel.generateNewRecommendations() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rescan Low Stock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = uiState) {
            is RecommendationUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is RecommendationUiState.Error -> {
                Text("Couldn't load recommendations: ${state.message}", color = GasTrackRed, fontSize = 13.sp)
            }
            is RecommendationUiState.Success -> {
                if (state.recommendations.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active recommendations", color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap Rescan to check current stock levels", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    state.recommendations.forEach { rec ->
                        RecommendationCard(rec = rec, onApproveClick = { onApproveClick(rec.RestockID) })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(rec: RestockRecommendationDto, onApproveClick: () -> Unit) {
    val isCritical = rec.StockOnHand == 0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = rec.ProductName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Supplier: ${rec.SupplierName}", fontSize = 12.sp, color = Color.Gray)
                }
                Surface(color = if (isCritical) Color.Red else Color(0xFFF57C00), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (isCritical) "Critical" else "Low",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Current: ${rec.StockOnHand} units", fontSize = 14.sp, color = Color.Gray)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Predicted: ${rec.PredictedDemand} units", fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Recommended Qty:", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                Text(text = "${rec.RecommendedQuantity} units", fontWeight = FontWeight.Black, fontSize = 18.sp, color = StaffPortalBlue)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApproveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Approve & Generate PO", fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RestockingSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun RestockingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
    }
}

@Composable
fun RestockingConfirmPopup(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
fun RestockingEmployeeScreenPreview() {
    RestockingEmployeeScreen()
}