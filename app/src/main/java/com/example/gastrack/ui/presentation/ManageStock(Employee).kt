package com.example.gastrack.ui.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.ui.theme.*

@Composable
fun ManageStockScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Current Stock") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StaffPortalButtonBlue, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manage Stock",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Tab Toggle ---
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(2.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .background(
                                    if (selectedTab == "Current Stock") Color.White else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTab = "Current Stock" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Current Stock",
                                color = if (selectedTab == "Current Stock") StaffPortalBlue else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .background(
                                    if (selectedTab == "Recent Activity") Color.White else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTab = "Recent Activity" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Recent Activity",
                                color = if (selectedTab == "Recent Activity") StaffPortalBlue else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- Tab Content ---
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == "Current Stock") {
                _root_ide_package_.com.example.gastrack.ui.presentation.CurrentStockTab()
            } else {
                _root_ide_package_.com.example.gastrack.ui.presentation.RecentActivityTab()
            }
        }
    }
}

@Composable
fun CurrentStockTab() {
    val items = listOf(
        _root_ide_package_.com.example.gastrack.ui.presentation.StockItemData("11kg Standard", 12),
        _root_ide_package_.com.example.gastrack.ui.presentation.StockItemData(
            "2.7kg Camping Refill",
            84
        ),
        _root_ide_package_.com.example.gastrack.ui.presentation.StockItemData("50kg Commercial", 84)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            _root_ide_package_.com.example.gastrack.ui.presentation.ExpandableStockItem(item)
        }
    }
}

data class StockItemData(val name: String, val count: Int)

@Composable
fun ExpandableStockItem(item: com.example.gastrack.ui.presentation.StockItemData) {
    var expanded by remember { mutableStateOf(item.name == "11kg Standard") }
    var adjustmentQty by remember { mutableIntStateOf(50) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = if (expanded) "Physical Count: ${item.count} units" else "Count: ${item.count} units",
                        color = if (expanded) Color.Black else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (expanded) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Stock Adjustment", fontWeight = FontWeight.Medium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "QUANTITY", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextGray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    IconButton(onClick = { if (adjustmentQty > 0) adjustmentQty-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StaffPortalBlue)
                    }
                    Text(
                        text = adjustmentQty.toString(),
                        modifier = Modifier.weight(1f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = TextDark
                    )
                    IconButton(onClick = { adjustmentQty++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = StaffPortalBlue)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Reason / Source", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = "Supplier PO Delivery",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, GasTrackRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "↑ OUT", color = GasTrackRed, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "↓ Confirm IN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivityTab() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        _root_ide_package_.com.example.gastrack.ui.presentation.ActivitySection(
            date = "TODAY",
            activities = listOf(
                _root_ide_package_.com.example.gastrack.ui.presentation.ActivityData(
                    "2.7kg Camping Refill",
                    "Supplier PO Delivery - 10:45 AM",
                    50,
                    true
                ),
                _root_ide_package_.com.example.gastrack.ui.presentation.ActivityData(
                    "50kg Commercial",
                    "Damaged / Defective - 09:15 AM",
                    -2,
                    false
                )
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        _root_ide_package_.com.example.gastrack.ui.presentation.ActivitySection(
            date = "YESTERDAY",
            activities = listOf(
                _root_ide_package_.com.example.gastrack.ui.presentation.ActivityData(
                    "50kg Commercial",
                    "Supplier PO Delivery - 10:45 AM",
                    -1,
                    false
                ),
                _root_ide_package_.com.example.gastrack.ui.presentation.ActivityData(
                    "2.7kg Camping Refill",
                    "Damaged / Defective - 09:15 AM",
                    1,
                    true
                )
            )
        )
    }
}

@Composable
fun ActivitySection(date: String, activities: List<com.example.gastrack.ui.presentation.ActivityData>) {
    Column {
        Text(text = date, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            activities.forEach { activity ->
                ActivityCard(activity)
            }
        }
    }
}

data class ActivityData(val name: String, val detail: String, val qty: Int, val isAddition: Boolean)

@Composable
fun ActivityCard(activity: ActivityData) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = StaffPortalBlue,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    if (activity.qty > 0) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = activity.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = activity.detail, color = Color.Gray, fontSize = 11.sp)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (activity.qty > 0) StaffPortalBlue else Color(0xFFFFEBEE),
                modifier = Modifier.size(width = 44.dp, height = 32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (activity.qty > 0) "+${activity.qty}" else activity.qty.toString(),
                        color = if (activity.qty > 0) Color.White else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManageStockScreenPreview() {
    ManageStockScreen()
}
