package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.ProfileSaveState
import com.example.gastrack.viewmodel.ProfileUiState
import com.example.gastrack.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    var isEditMode by remember { mutableStateOf(false) }

    // Editable draft fields, populated once profile loads
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var contactNo by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf("Residential") }
    var email by remember { mutableStateOf("") }

    var expandedType by remember { mutableStateOf(false) }
    val customerTypes = listOf("Residential", "Commercial")

    val uiState by profileViewModel.uiState.collectAsState()
    val saveState by profileViewModel.saveState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        val loaded = uiState
        if (loaded is ProfileUiState.Success) {
            val nameParts = loaded.profile.CustomerName.trim().split(" ", limit = 2)
            firstName = nameParts.getOrElse(0) { "" }
            lastName = nameParts.getOrElse(1) { "" }
            contactNo = loaded.profile.ContactNo
            address = loaded.profile.Address
            customerType = loaded.profile.CustomerType
            email = loaded.profile.Email
        }
    }

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is ProfileSaveState.Saved -> {
                android.widget.Toast.makeText(context, "Profile updated", android.widget.Toast.LENGTH_SHORT).show()
                isEditMode = false
                profileViewModel.resetSaveState()
            }
            is ProfileSaveState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                profileViewModel.resetSaveState()
            }
            else -> {}
        }
    }

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
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GasTrackBlue)
                }
            }
            is ProfileUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load profile", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { profileViewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = GasTrackBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is ProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color(0xFFF1F3F4)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = StaffPortalBlue,
                                modifier = Modifier.padding(20.dp).fillMaxSize()
                            )
                        }
                        if (isEditMode) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = GasTrackRed,
                                shadowElevation = 4.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Edit Photo",
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "BASIC INFORMATION",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // First Name & Last Name
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProfileField("FIRST NAME", firstName, isEditMode) { firstName = it }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            ProfileField("LAST NAME", lastName, isEditMode) { lastName = it }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileField("EMAIL ADDRESS", email, enabled = false, keyboardType = KeyboardType.Email) {}

                    Spacer(modifier = Modifier.height(20.dp))

                    // Customer Type Dropdown
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "CUSTOMER TYPE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextGray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedType && isEditMode,
                            onExpandedChange = { if (isEditMode) expandedType = !expandedType }
                        ) {
                            OutlinedTextField(
                                value = customerType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                enabled = isEditMode,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE9ECEF),
                                    focusedBorderColor = GasTrackBlue,
                                    disabledBorderColor = Color(0xFFF1F3F4),
                                    disabledTextColor = TextDark,
                                    disabledLabelColor = TextGray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false }
                            ) {
                                customerTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            customerType = type
                                            expandedType = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileField("CONTACT NO.", contactNo, isEditMode, keyboardType = KeyboardType.Phone) { contactNo = it }

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileField("COMPLETE ADDRESS", address, isEditMode, minLines = 2) { address = it }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Action Buttons
                    if (!isEditMode) {
                        Button(
                            onClick = { isEditMode = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Profile", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = GasTrackRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Logout", color = GasTrackRed, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                profileViewModel.saveProfile(
                                    customerName = "$firstName $lastName".trim(),
                                    contactNo = contactNo,
                                    address = address,
                                    customerType = customerType
                                )
                            },
                            enabled = saveState !is ProfileSaveState.Saving,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            if (saveState is ProfileSaveState.Saving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Save Changes", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                isEditMode = false
                                profileViewModel.loadProfile() // discard unsaved edits
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = TextGray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE9ECEF),
                focusedBorderColor = GasTrackBlue,
                disabledBorderColor = Color(0xFFF1F3F4),
                disabledTextColor = TextDark,
                disabledContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(onBack = {})
}