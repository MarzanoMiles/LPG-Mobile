package com.example.gastrack.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.ui.theme.*

@Composable
fun DashboardEmployeeScreen(
    onNavigateToMenu: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToRestocking: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToMenu) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = "Good Morning, Rei",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = "Admin",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- Stats Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                _root_ide_package_.com.example.gastrack.ui.presentation.StatCard(
                    label = "TODAY'S ORDERS",
                    value = "42",
                    accentColor = StaffPortalButtonBlue,
                    modifier = Modifier.weight(1f)
                )
                _root_ide_package_.com.example.gastrack.ui.presentation.StatCard(
                    label = "DELIVERED",
                    value = "18",
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Inventory Alerts ---
            Text(
                text = "Inventory Alerts",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
            )
            Spacer(modifier = Modifier.height(16.dp))
            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryAlertCard(
                onRestockClick = onNavigateToInventory
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Assigned Tasks ---
            Text(
                text = "Assigned Tasks",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                fontStyle = FontStyle.Italic,
            )
            Spacer(modifier = Modifier.height(16.dp))
            _root_ide_package_.com.example.gastrack.ui.presentation.TaskItem(
                title = "Review Restocking Order",
                due = "Due today, 2:00 PM",
                priority = "High",
                priorityColor = Color.Red,
                onClick = onNavigateToRestocking
            )
            Spacer(modifier = Modifier.height(16.dp))
            _root_ide_package_.com.example.gastrack.ui.presentation.TaskItem(
                title = "Check Del #4 Maintenance",
                due = "Due tomorrow, 9:00 AM",
                priority = "Normal",
                priorityColor = StaffPortalButtonBlue
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Accent Border
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = value, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }
        }
    }
}

@Composable
fun InventoryAlertCard(onRestockClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Orange Accent
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(70.dp)
                    .background(Color(0xFFF57C00), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Low Stock: 11kg LPG",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
                Text(
                    text = "Only 15 cylinders left",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onRestockClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "Restock",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun TaskItem(title: String, due: String, priority: String, priorityColor: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mock Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.Black)
                Text(text = due, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }

            Surface(
                color = priorityColor,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = priority,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardEmployeeScreenPreview() {
    _root_ide_package_.com.example.gastrack.ui.presentation.DashboardEmployeeScreen()
}
