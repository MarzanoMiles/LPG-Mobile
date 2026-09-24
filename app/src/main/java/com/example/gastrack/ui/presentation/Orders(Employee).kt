package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.TextGray

@Composable
fun OrdersEmployeeScreen(
    onBack: () -> Unit = {},
    onNavigateToMaps: (String) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Pending") }

    // Dynamic Orders State
    val allOrders = remember { mutableStateListOf(
        EmployeeOrder("ORD-2023-081", "10:00 AM - 12:00 PM", "Juan Dela Cruz", "1 × 11kg Standard", "45 Mabini St, Brgy. San Jose", "Pending"),
        EmployeeOrder("ORD-2023-085", "1:00 PM - 3:00 PM", "Maria Clara Res.", "2 × 50kg Commercial", "12 Rizal Ave, Malabon City", "Pending"),
        EmployeeOrder("ORD-2023-090", "09:00 AM - 11:00 AM", "Andres Bonifacio", "1 × 2.7kg Camping", "8 Bonifacio St, Caloocan", "In Transit"),
        EmployeeOrder("ORD-2023-075", "Yesterday", "Emilio Aguinaldo", "5 × 50kg Commercial", "Kawit, Cavite", "Finished")
    ) }

    val pendingOrders = allOrders.filter { it.status == "Pending" }
    val inTransitOrders = allOrders.filter { it.status == "In Transit" }
    val historyOrders = allOrders.filter { it.status == "Finished" }

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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentList) { order ->
                    EmployeeOrderCard(
                        order = order,
                        onViewMaps = { onNavigateToMaps(order.id) },
                        onStartDelivery = {
                            val index = allOrders.indexOf(order)
                            if (index != -1) {
                                allOrders[index] = order.copy(status = "In Transit")
                                selectedTab = "In Transit"
                            }
                        },
                        onInTransitClick = {
                            val index = allOrders.indexOf(order)
                            if (index != -1) {
                                allOrders[index] = order.copy(status = "Finished")
                                selectedTab = "History"
                            }
                        }
                    )
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

@Preview(showBackground = true)
@Composable
fun OrdersEmployeeScreenPreview() {
    OrdersEmployeeScreen()
}
