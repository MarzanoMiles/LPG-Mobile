package com.example.gastrack.ui.presentation


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.R
import com.example.gastrack.ui.theme.GasTrackBlue
import com.example.gastrack.ui.theme.GasTrackRed
import com.example.gastrack.ui.theme.TextDark
import com.example.gastrack.ui.theme.TextGray

@Composable
fun LogOutCustomerScreen(
    onLogout: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Logo Section ---
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.favicon),
                contentDescription = "Logo Icon",
                modifier = Modifier.padding(20.dp).fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Are you sure you want to log out?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You will need to sign in again to access your orders and history.",
            fontSize = 15.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(64.dp))

        // --- Logout Button ---
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GasTrackRed),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "Log Out Now",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Cancel Button ---
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = "Stay Logged In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogOutCustomerScreenPreview() {
    LogOutCustomerScreen(
        onLogout = {},
        onCancel = {})
}
