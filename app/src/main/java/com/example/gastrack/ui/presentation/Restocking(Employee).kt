package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.StaffPortalButtonBlue

enum class RestockingState {
    STANDARD, SMART
}

@Composable
fun RestockingEmployeeScreen(
    onBack: () -> Unit = {}
) {
    var currentState by remember { mutableStateOf(RestockingState.STANDARD) }
    var showConfirmPopup by remember { mutableStateOf(false) }

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

                // AI Toggle Button
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
                RestockingStandardContent(onApproveClick = { showConfirmPopup = true })
            } else {
                SmartRestockingContent()
            }
        }
    }

    if (showConfirmPopup) {
        RestockingConfirmPopup(
            onDismiss = { showConfirmPopup = false },
            onConfirm = { showConfirmPopup = false }
        )
    }
}

@Composable
fun RestockingStandardContent(onApproveClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Supplier Details Card ---
        RestockingSectionCard(title = "Supplier Details") {
            RestockingDetailRow("Supplier", "Petron Gasul Dist.")
            RestockingDetailRow("PO Number", "PO-2023-089")
            RestockingDetailRow("Expected Delivery", "Tomorrow, 8:00 AM")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Order Items Card ---
        RestockingSectionCard(title = "Order Items") {
            OrderItemRow("11kg LPG Cylinder", "Qty: 50", "Unit Price: ₱ 850.00", "Total: ₱ 42,500.00")
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            OrderItemRow("50kg LPG Cylinder", "Qty: 10", "Unit Price: ₱ 4,300.00", "Total: ₱ 43,000.00")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Total Amount ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = StaffPortalButtonBlue.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Amount:", fontWeight = FontWeight.Bold, color = StaffPortalBlue)
                Text(text = "₱ 93,000.00", fontSize = 24.sp, fontWeight = FontWeight.Black, color = StaffPortalBlue)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Approve Button ---
        Button(
            onClick = onApproveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Approve & Confirm Order",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun SmartRestockingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- AI Recommendation Box ---
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
                    text = "Based on current sales velocity (+12%) and upcoming holiday weekend demand, we recommend the following Purchase Order.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Recommendation Item
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "11kg Standard Refill", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Surface(color = Color.Red, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "Critical",
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
                                Text(text = "Current: 15 units", fontSize = 14.sp, color = Color.Gray)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Target: 100 units", fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Qty to Order:", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            POSQuantitySelector(quantity = 85, onUpdateQuantity = {})
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Approve & Generate PO",
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
fun OrderItemRow(name: String, qty: String, unitPrice: String, total: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = qty, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = unitPrice, color = Color.Gray, fontSize = 12.sp)
            Text(text = total, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun RestockingConfirmPopup(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirm Purchase Order", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Text(text = "Are you sure you want to confirm this purchase order? Once confirmed, the purchase order will be submitted and an email will be sent to the supplier.")
        },
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
