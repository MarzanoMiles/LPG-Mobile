package com.example.gastrack.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.R
import com.example.gastrack.ui.theme.*

// Modern Home Colors
val HomeBackground = Color(0xFFFBFBFE)
val CardDark = Color(0xFF161D27)
val RefillGradient = listOf(Color(0xFFFFB87A), Color(0xFFFF8E3C))
val BuyTankGradient = listOf(Color(0xFF8D99D5), Color(0xFF5C6BC0))

@Composable
fun CustomerHomeScreen(
    onNavigateToOrder: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToCheckout: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    Scaffold(
        containerColor = _root_ide_package_.com.example.gastrack.ui.presentation.HomeBackground,
        bottomBar = {
            _root_ide_package_.com.example.gastrack.ui.presentation.BottomNavigationBar(
                currentRoute = "home",
                onOrderClick = onNavigateToOrder,
                onMenuClick = onNavigateToMenu
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- Modern Header ---
            _root_ide_package_.com.example.gastrack.ui.presentation.HomeHeader(
                onAddressClick = onNavigateToAddresses,
                onNotificationClick = onNavigateToNotifications
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Smart Refill Card ---
            _root_ide_package_.com.example.gastrack.ui.presentation.SmartRefillCard(onReorderClick = onNavigateToCheckout)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "What do you need?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Modern Action Tiles ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                _root_ide_package_.com.example.gastrack.ui.presentation.ActionTile(
                    text = "Refill\nLPG",
                    subText = "Quick order",
                    gradient = _root_ide_package_.com.example.gastrack.ui.presentation.RefillGradient,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToOrder
                )
                _root_ide_package_.com.example.gastrack.ui.presentation.ActionTile(
                    text = "New\nTank",
                    subText = "Buy cylinder",
                    gradient = _root_ide_package_.com.example.gastrack.ui.presentation.BuyTankGradient,
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToOrder
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- AR Experience Button ---
            _root_ide_package_.com.example.gastrack.ui.presentation.ARFloatingButton(onClick = onNavigateToAR)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeHeader(
    onAddressClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddressClick() }
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.favicon),
                    contentDescription = "Mini Logo",
                    modifier = Modifier.padding(8.dp).fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "DELIVER TO",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Malabon City",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = GasTrackRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .background(Color.White, CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextDark)
        }
    }
}

@Composable
fun SmartRefillCard(onReorderClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .background(_root_ide_package_.com.example.gastrack.ui.presentation.CardDark, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
    ) {
        // Aesthetic Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(GasTrackBlue.copy(alpha = 0.3f), Color.Transparent),
                        radius = 400f
                    )
                )
        )

        // Subtle Icon Background
        Icon(
            imageVector = Icons.Default.ViewInAr,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.05f),
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
        )

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Last order: 42 days ago",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Column {
                Text(
                    text = "Time for a refill?",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 34.sp
                )
                Text(
                    text = "Based on your previous usage",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onReorderClick,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "1-Click Reorder",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ActionTile(text: String, subText: String, gradient: List<Color>, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .height(140.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Small decorative icon at bottom right
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-8).dp)
        )
    }
}

@Composable
fun ARFloatingButton(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .shadow(12.dp, RoundedCornerShape(32.dp))
                .background(Color.White, RoundedCornerShape(32.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(GasTrackBlue.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = "AR",
                    modifier = Modifier.size(40.dp),
                    tint = GasTrackBlue
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "AI Visual Scan",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onHomeClick: () -> Unit = {},
    onOrderClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            _root_ide_package_.com.example.gastrack.ui.presentation.NavigationBarItem(
                isSelected = currentRoute == "home",
                icon = Icons.Default.Home,
                label = "Home",
                onClick = onHomeClick
            )
            _root_ide_package_.com.example.gastrack.ui.presentation.NavigationBarItem(
                isSelected = currentRoute == "order",
                icon = Icons.Default.ShoppingCart,
                label = "Order",
                onClick = onOrderClick
            )
            _root_ide_package_.com.example.gastrack.ui.presentation.NavigationBarItem(
                isSelected = currentRoute == "menu",
                icon = Icons.Default.Menu,
                label = "Menu",
                onClick = onMenuClick
            )
        }
    }
}

@Composable
fun NavigationBarItem(isSelected: Boolean, icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) GasTrackRed else TextGray,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) GasTrackRed else TextGray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerHomeScreenPreview() {
    _root_ide_package_.com.example.gastrack.ui.presentation.CustomerHomeScreen(
        onNavigateToOrder = {},
        onNavigateToAR = {},
        onNavigateToMenu = {})
}
