package com.example.gastrack.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.gastrack.ui.theme.BorderColor
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.GasTrackRed
import com.example.gastrack.ui.theme.TextDark
import com.example.gastrack.ui.theme.TextGray

@Composable
fun StartUpScreen(
    onNavigateToCustomer: () -> Unit,
    onNavigateToEmployee: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF8F9FA))
                )
            )
    ) {
        // Decorative background element
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .clip(CircleShape)
                .background(GasTrackBlue.copy(alpha = 0.03f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Logo Section ---
            LogoSection()

            Spacer(modifier = Modifier.height(64.dp))

            // --- Header Text ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome to GasTrack",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = GasTrackBlue,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Please select your account type to continue to the correct portal.",
                    fontSize = 16.sp,
                    color = TextGray,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Account Type Cards ---
            AccountTypeCard(
                icon = Icons.Default.Person,
                overlineText = "I'm a",
                titleText = "Customer",
                subtitleText = "Order gas, track deliveries",
                accentColor = GasTrackRed,
                onClick = onNavigateToCustomer
            )

            Spacer(modifier = Modifier.height(20.dp))

            AccountTypeCard(
                icon = Icons.Default.LocalShipping,
                overlineText = "I'm an",
                titleText = "Employee",
                subtitleText = "Manage stock, do deliveries",
                accentColor = GasTrackBlue,
                onClick = onNavigateToEmployee
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Footer
            Text(
                text = "v1.0.4 • Powered by GasTrack AI",
                fontSize = 12.sp,
                color = BorderColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LogoSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.favicon),
                contentDescription = "Logo Icon",
                modifier = Modifier.padding(12.dp).fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = GasTrackBlue, fontWeight = FontWeight.Black)) {
                        append("Gas")
                    }
                    withStyle(style = SpanStyle(color = GasTrackRed, fontWeight = FontWeight.Black)) {
                        append("Track")
                    }
                },
                fontSize = 36.sp,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Optimize Your LPG, Effortlessly.",
                fontSize = 12.sp,
                color = GasTrackBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AccountTypeCard(
    icon: ImageVector,
    overlineText: String,
    titleText: String,
    subtitleText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = titleText,
                    tint = accentColor,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = overlineText.uppercase(),
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = titleText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
                    fontSize = 14.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Continue",
                tint = BorderColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartUpScreenPreview() {
    StartUpScreen(onNavigateToCustomer = {}, onNavigateToEmployee = {})
}
