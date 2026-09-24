package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.R
import com.example.gastrack.ui.theme.GasTrackRed
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.TextDark
import com.example.gastrack.ui.theme.TextGray

@Composable
fun MenuEmployeeScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogout: () -> Unit,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToPOS: () -> Unit = {},
    onNavigateToSales: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToRestocking: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToSuppliers: () -> Unit = {},
    onNavigateToData: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
) {
    Scaffold(
        containerColor = Color(0xFFFBFBFE),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- High-Quality Header (Faithful to Reference) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(StaffPortalBlue, Color(0xFF000540))
                        ),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // --- Branded Logo Section ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.favicon),
                                    contentDescription = "Logo",
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) {
                                        append("Gas")
                                    }
                                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)) {
                                        append("Track")
                                    }
                                },
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Staff Portal",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                        Text(
                            text = "Admin Account",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Circular Profile Icon (Matching Reference)
                    Surface(
                        modifier = Modifier.size(56.dp),
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
            }

            // --- Menu Items List (Premium Cards) ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { MenuSectionHeader("CORE OPERATIONS") }
                item { EmployeeMenuItem(Icons.Default.GridView, "Dashboard", "Daily stats and overview", onNavigateToDashboard) }
                item { EmployeeMenuItem(Icons.AutoMirrored.Filled.ListAlt, "Assigned Orders", "Track and manage deliveries", onNavigateToOrders) }
                item { EmployeeMenuItem(Icons.Default.ShoppingCart, "POS Terminal", "Retail sales and checkouts", onNavigateToPOS) }
                item { EmployeeMenuItem(Icons.Default.AttachMoney, "Sales Records", "Transaction history and reports", onNavigateToSales) }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { MenuSectionHeader("INVENTORY & LOGISTICS") }
                item { EmployeeMenuItem(Icons.Default.Category, "Stock Inventory", "Monitor cylinder levels", onNavigateToInventory) }
                item { EmployeeMenuItem(Icons.Default.Inventory, "Products Catalog", "Manage item list and prices", onNavigateToProducts) }
                item { EmployeeMenuItem(Icons.Default.LocalShipping, "Restocking", "Generate supplier POs", onNavigateToRestocking) }
                item { EmployeeMenuItem(Icons.Default.Groups, "Suppliers", "Distributor contact info", onNavigateToSuppliers) }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { MenuSectionHeader("ADMINISTRATION") }
                item { EmployeeMenuItem(Icons.Default.Storage, "System Data", "Database and backup", onNavigateToData) }
                item { EmployeeMenuItem(Icons.Default.Person, "User Accounts", "Staff access management", onNavigateToUsers) }
                item { EmployeeMenuItem(Icons.Default.Settings, "Compliance", "Reports and settings", onNavigateToReports) }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Button(
                        onClick = onNavigateToLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDECEA))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = GasTrackRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Logout from Portal", color = GasTrackRed, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun MenuSectionHeader(text: String) {
    Text(
        text = text,
        color = TextGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun EmployeeMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = StaffPortalBlue.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StaffPortalBlue,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuEmployeeScreenPreview() {
    MenuEmployeeScreen(onNavigateToDashboard = {}, onNavigateToLogout = {})
}
