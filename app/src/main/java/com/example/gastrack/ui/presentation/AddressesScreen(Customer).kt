package com.example.gastrack.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.AddressDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.AddressUiState
import com.example.gastrack.viewmodel.AddressViewModel

private fun iconForAddressType(type: String): ImageVector = when (type) {
    "Home" -> Icons.Default.Home
    "Work" -> Icons.Default.Work
    else -> Icons.Default.LocationOn
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    onBack: () -> Unit,
    addressViewModel: AddressViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val uiState by addressViewModel.uiState.collectAsState()
    val actionMessage by addressViewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            addressViewModel.clearActionMessage()
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
                        text = "Saved Address",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ButtonOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Address")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is AddressUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GasTrackBlue)
                }
            }
            is AddressUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load addresses", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { addressViewModel.loadAddresses() },
                        colors = ButtonDefaults.buttonColors(containerColor = GasTrackBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is AddressUiState.Success -> {
                if (state.addresses.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No saved addresses yet", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap + to add your first delivery address", color = TextGray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.addresses, key = { it.AddressID }) { address ->
                            AddressCard(
                                address = address,
                                onSelect = { addressViewModel.setPrimary(address.AddressID) },
                                onDelete = { addressViewModel.deleteAddress(address.AddressID) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAddressDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { label, location, type ->
                addressViewModel.addAddress(label = label, addressLine = location, addressType = type)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddressCard(address: AddressDto, onSelect: () -> Unit, onDelete: () -> Unit) {
    val isPrimary = address.IsPrimary == 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clickable { onSelect() },
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isPrimary) GasTrackRed.copy(alpha = 0.1f) else Color(0xFFF1F3F4)
            ) {
                Icon(
                    imageVector = iconForAddressType(address.AddressType),
                    contentDescription = null,
                    tint = if (isPrimary) GasTrackRed else StaffPortalBlue,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.Label,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                    if (isPrimary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = GasTrackRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PRIMARY",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = address.AddressLine,
                    fontSize = 13.sp,
                    color = TextGray,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            RadioButton(
                selected = isPrimary,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = GasTrackRed)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.ErrorOutline, contentDescription = "Delete", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Home") }

    val types = listOf("Home", "Work", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add New Address",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ADDRESS LABEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. My Apartment", color = BorderColor) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = GasTrackBlue
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "LOCATION DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Complete address here...", color = BorderColor) },
                    shape = RoundedCornerShape(16.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9ECEF),
                        focusedBorderColor = GasTrackBlue
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ADDRESS TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { selectedType = type }
                                .border(
                                    1.dp,
                                    if (isSelected) StaffPortalBlue else Color(0xFFE9ECEF),
                                    RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) StaffPortalBlue.copy(alpha = 0.05f) else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StaffPortalBlue else TextGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", color = TextDark)
                    }
                    Button(
                        enabled = label.isNotEmpty() && location.isNotEmpty(),
                        onClick = { onAdd(label, location, selectedType) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange)
                    ) {
                        Text("Save Address", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddressesScreenPreview() {
    AddressesScreen(onBack = {})
}