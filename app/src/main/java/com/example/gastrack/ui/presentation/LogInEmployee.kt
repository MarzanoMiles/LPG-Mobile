package com.example.gastrack.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gastrack.R
import com.example.gastrack.ui.theme.*

@Composable
fun LogInEmployeeScreen(
    onBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var loginIdentifier by remember { mutableStateOf("") }
    var securityPin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Decorative background blur element (Staff Blue theme)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 150.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(StaffPortalButtonBlue.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Top Bar (Back Button) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Logo Section (Matching Customer Design) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.favicon),
                    contentDescription = "Logo Icon",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

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
                        fontSize = 38.sp,
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

            Spacer(modifier = Modifier.height(48.dp))

            // --- Subtitle ---
            Text(
                text = "Staff Portal: Operations Access Only",
                fontSize = 16.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(44.dp))

            // --- Email or Employee ID Input ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "EMAIL OR EMPLOYEE ID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = loginIdentifier,
                    onValueChange = { loginIdentifier = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFBFBFE), RoundedCornerShape(16.dp)),
                    placeholder = { Text("Email or Employee ID", color = Color.LightGray) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = StaffPortalButtonBlue)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = StaffPortalButtonBlue,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Security PIN Input ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SECURITY PIN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = securityPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) securityPin = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFBFBFE), RoundedCornerShape(16.dp)),
                    placeholder = { Text("● ● ● ●", color = Color.LightGray) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StaffPortalButtonBlue)
                    },
                    trailingIcon = {
                        val icon = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(imageVector = icon, contentDescription = null, tint = TextGray)
                        }
                    },
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = StaffPortalButtonBlue,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                Text(
                    text = "Forgot PIN?",
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable { showForgotPinDialog = true },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StaffPortalButtonBlue
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            // --- Log In Button (Modern Premium Pill) ---
            Button(
                enabled = loginIdentifier.isNotEmpty() && securityPin.isNotEmpty(),
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StaffPortalButtonBlue,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Access Portal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Modern Footer ---
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Staff? ",
                    fontSize = 15.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Request ID Activation",
                    fontSize = 15.sp,
                    color = StaffPortalButtonBlue,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }

    if (showForgotPinDialog) {
        _root_ide_package_.com.example.gastrack.ui.presentation.ForgotPinDialog(onDismiss = {
            showForgotPinDialog = false
        })
    }
}

@Composable
fun ForgotPinDialog(onDismiss: () -> Unit) {
    var email by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset Security PIN",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your work email to receive a temporary reset code.",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("work@gastrack.com", color = Color.LightGray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = StaffPortalButtonBlue
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    enabled = email.isNotEmpty(),
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                ) {
                    Text("Request Reset", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextGray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogInEmployeeScreenPreview() {
    _root_ide_package_.com.example.gastrack.ui.presentation.LogInEmployeeScreen(
        onBack = {},
        onNavigateToSignUp = {},
        onNavigateToHome = {})
}
