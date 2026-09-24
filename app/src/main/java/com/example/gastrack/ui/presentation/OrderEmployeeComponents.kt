package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.TextDark
import com.example.gastrack.ui.theme.TextGray

data class EmployeeOrder(
    val id: String,
    val scheduledTime: String,
    val customerName: String,
    val itemDetails: String,
    val address: String,
    val status: String,
)

@Composable
fun OrderTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None
        )
    }
}

@Composable
fun EmployeeOrderCard(
    order: EmployeeOrder,
    badgeText: String? = null,
    onViewMaps: () -> Unit = {},
    onStartDelivery: () -> Unit = {},
    onInTransitClick: () -> Unit = {},
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
                            text = "Order #${order.id}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Text(
                            text = "Scheduled: ${order.scheduledTime}",
                            fontSize = 13.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Status Badge
                    val displayBadgeText = badgeText ?: order.status
                    val (badgeColor, textColor) = when (displayBadgeText) {
                        "Finished" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        "In Transit" -> Color(0xFFE3F2FD) to StaffPortalBlue
                        else -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
                    }
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = displayBadgeText.uppercase(),
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
                    OrderInfoRow(Icons.Default.Person, order.customerName)
                    OrderInfoRow(Icons.Default.Inventory2, order.itemDetails)
                    OrderInfoRow(Icons.Default.LocationOn, order.address)
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (order.status) {
                    "Pending" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onViewMaps,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, StaffPortalBlue)
                            ) {
                                Text(
                                    text = "View Route",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = StaffPortalBlue
                                )
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
                                Text(
                                    text = "Dispatch",
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                    "In Transit" -> {
                        Button(
                            onClick = onInTransitClick,
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
                                Text(
                                    text = "Track on Map",
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    "Finished" -> {
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
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
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
fun OrderInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF8F9FA)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(4.dp),
                tint = StaffPortalBlue
            )
        }
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
fun EmployeeOrderCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        EmployeeOrderCard(
            order = EmployeeOrder(
                id = "ORD-2023-081",
                scheduledTime = "10:00 AM - 12:00 PM",
                customerName = "Juan Dela Cruz",
                itemDetails = "1 × 11kg Standard",
                address = "45 Mabini St, Brgy. San Jose",
                status = "Pending"
            )
        )
    }
}
