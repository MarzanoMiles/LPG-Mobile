package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.ui.theme.ButtonOrange
import com.example.gastrack.ui.theme.GasTrackRed
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.TextDark
import com.example.gastrack.ui.theme.TextGray
import kotlinx.coroutines.delay

@Composable
fun MapTrackingScreen(
    onBack: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var isArrived by remember { mutableStateOf(false) }

    // Simulation of driver moving
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(100)
            progress += 0.005f
        }
        isArrived = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7E9))) {
        // --- Simulated Map Pins ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val start = center + Offset(-200f, 300f)
            val end = center + Offset(100f, -200f)

            // Route Line
            drawLine(
                color = Color.LightGray,
                start = start,
                end = end,
                strokeWidth = 12f
            )

            // Progress Line
            drawLine(
                color = StaffPortalBlue,
                start = start,
                end = start + (end - start) * progress,
                strokeWidth = 12f
            )
        }

        // Destination Pin (Customer)
        Surface(
            modifier = Modifier.align(Alignment.Center).offset(x = 50.dp, y = (-100).dp),
            shape = CircleShape,
            color = GasTrackRed,
            shadowElevation = 8.dp
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
        }

        // Driver Pin (Moving)
        val driverXOffset = (-100).dp + (150).dp * progress
        val driverYOffset = (150).dp - (250).dp * progress
        Surface(
            modifier = Modifier.align(Alignment.Center).offset(x = driverXOffset, y = driverYOffset),
            shape = CircleShape,
            color = StaffPortalBlue,
            shadowElevation = 12.dp,
            border = BorderStroke(2.dp, Color.White)
        ) {
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp).size(20.dp))
        }

        // --- Back Button ---
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .clickable { onBack() },
            shape = CircleShape,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
            }
        }

        // --- Estimated Arrival Card ---
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isArrived) "ORDER ARRIVED" else "ESTIMATED ARRIVAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isArrived) GasTrackRed else TextGray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isArrived) "Arrived" else "02:45 PM",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = StaffPortalBlue
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isArrived) Color.Green.copy(alpha = 0.1f) else ButtonOrange.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isArrived) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isArrived) Color.Green else ButtonOrange,
                        modifier = Modifier.padding(14.dp).fillMaxSize()
                    )
                }
            }
        }

        // --- Driver Details Bottom Card ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE9ECEF), CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArrived) "Order Delivered" else "Out for Delivery",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Text(
                            text = if (isArrived) "Please check your delivery" else "Juan is on his way to you",
                            fontSize = 15.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Quick Action Group
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FloatingActionButton(
                            onClick = {},
                            containerColor = Color(0xFFF1F3F4),
                            contentColor = StaffPortalBlue,
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", modifier = Modifier.size(20.dp))
                        }
                        FloatingActionButton(
                            onClick = {},
                            containerColor = StaffPortalBlue,
                            contentColor = Color.White,
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Modern Timeline
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TrackingStep(
                        title = "Order Prepared",
                        time = "01:30 PM",
                        isCompleted = true,
                        isLast = false
                    )
                    TrackingStep(
                        title = "Out for Delivery",
                        time = "01:45 PM",
                        isCompleted = true,
                        isLast = true,
                        isProcessing = !isArrived
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TrackingStep(
    title: String,
    time: String,
    isCompleted: Boolean,
    isLast: Boolean,
    isProcessing: Boolean = false
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = if (isCompleted) GasTrackRed else Color(0xFFE9ECEF)
            ) {}
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(if (isCompleted) GasTrackRed else Color(0xFFE9ECEF))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = if (isCompleted) FontWeight.Black else FontWeight.Bold,
                color = if (isCompleted) TextDark else TextGray
            )
            Text(
                text = if (isProcessing) "Ongoing" else time,
                fontSize = 12.sp,
                color = if (isProcessing) GasTrackRed else TextGray,
                fontWeight = if (isProcessing) FontWeight.Black else FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapTrackingScreenPreview() {
    MapTrackingScreen(onBack = {})
}
