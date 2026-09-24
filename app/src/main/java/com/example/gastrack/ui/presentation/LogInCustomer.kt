package com.example.gastrack.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.R
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.AuthUiState
import com.example.gastrack.viewmodel.AuthViewModel

@Composable
fun LogInCustomerScreen(
    onBack: () -> Unit = {},
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                authViewModel.resetState()
                onNavigateToHome()
            }
            is AuthUiState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Decorative background blur element
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 150.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(GasTrackBlue.copy(alpha = 0.05f))
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

            // --- Logo Section (Maintained from previous design) ---
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
                text = "Log in to manage your LPG deliveries.",
                fontSize = 16.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(44.dp))

            // --- Email or Phone Number Input (Modernized Card Style) ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "EMAIL OR PHONE NUMBER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFBFBFE), RoundedCornerShape(16.dp)),
                    placeholder = { Text("Email or Phone Number", color = Color.LightGray) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = StaffPortalBlue)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = GasTrackBlue,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Password Input (Modernized) ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PASSWORD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFBFBFE), RoundedCornerShape(16.dp)),
                    placeholder = { Text("Password", color = Color.LightGray) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StaffPortalBlue)
                    },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = null, tint = TextGray)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = GasTrackBlue,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
                Text(
                    text = "Forgot Password?",
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable { showForgotPasswordDialog = true },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GasTrackBlue
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            // --- Log In Button (Modern Premium Pill) ---
            Button(
                enabled = emailOrPhone.isNotEmpty() && password.isNotEmpty() && uiState !is AuthUiState.Loading,
                onClick = { authViewModel.loginCustomer(emailOrPhone, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonOrange,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = 20.sp,
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
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Modern Footer ---
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New to GasTrack? ",
                    fontSize = 15.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Create Account",
                    fontSize = 15.sp,
                    color = GasTrackRed,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(onDismiss = { showForgotPasswordDialog = false })
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit) {
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
                    text = "Reset Password",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your email or phone to receive reset instructions.",
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
                    placeholder = { Text("Email or Phone", color = Color.LightGray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = GasTrackBlue
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
                    colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue)
                ) {
                    Text("Send Reset Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
fun LogInCustomerScreenPreview() {
    LogInCustomerScreen(onNavigateToSignUp = {}, onNavigateToHome = {})
}