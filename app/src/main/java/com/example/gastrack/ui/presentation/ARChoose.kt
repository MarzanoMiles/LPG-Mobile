package com.example.gastrack.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.R
import com.example.gastrack.ui.theme.*

@Composable
fun ARChooseScreen(
    onContinue: (GasTankType) -> Unit
) {
    var selectedType by remember { mutableStateOf("11kg Standard") }

    Scaffold(
        containerColor = Color(0xFFFBFBFE),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GasTrackBlue, StaffPortalBlue)
                        )
                    )
                    .padding(top = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Visual Scan",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Button(
                    onClick = { onContinue(GasTankType.fromUiLabel(selectedType)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GasTrackBlue)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Start AI Scan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
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

            // --- Intro Card ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GasTrackBlue.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GasTrackBlue)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Select your LPG tank model to ensure accurate volume measurement through your camera.",
                        fontSize = 13.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SELECT YOUR TANK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextGray,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Tank Options ---
            ARTankOption(
                name = "11kg Standard",
                description = "Standard household gas cylinder",
                imageRes = R.drawable.tank_11kg,
                isSelected = selectedType == "11kg Standard"
            ) { selectedType = "11kg Standard" }

            Spacer(modifier = Modifier.height(16.dp))

            ARTankOption(
                name = "2.7kg Camping",
                description = "Small portable camping tank",
                imageRes = R.drawable.tank_2_7kg,
                isSelected = selectedType == "2.7kg Camping"
            ) { selectedType = "2.7kg Camping" }

            Spacer(modifier = Modifier.height(16.dp))

            ARTankOption(
                name = "50kg Commercial",
                description = "Industrial grade large cylinder",
                imageRes = R.drawable.tank_50kg,
                isSelected = selectedType == "50kg Commercial"
            ) { selectedType = "50kg Commercial" }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ARTankOption(
    name: String,
    description: String,
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(
                2.dp,
                if (isSelected) GasTrackBlue else Color.Transparent,
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8F9FA)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = GasTrackBlue)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ARChooseScreenPreview() {
    ARChooseScreen(onContinue = {})
}