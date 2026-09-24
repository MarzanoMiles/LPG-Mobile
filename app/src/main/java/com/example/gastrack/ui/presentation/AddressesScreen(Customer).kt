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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gastrack.ui.theme.*

data class AddressItem(
    val id: Int,
    val label: String,
    val address: String,
    val icon: ImageVector,
    val isPrimary: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val addresses = remember { mutableStateListOf(
        _root_ide_package_.com.example.gastrack.ui.presentation.AddressItem(
            1,
            "Home",
            "45 Mabini St, Brgy. Concepcion, Malabon City",
            Icons.Default.Home,
            true
        ),
        _root_ide_package_.com.example.gastrack.ui.presentation.AddressItem(
            2,
            "Work",
            "GasTrack HQ, Bonifacio Global City, Taguig",
            Icons.Default.Work
        ),
        _root_ide_package_.com.example.gastrack.ui.presentation.AddressItem(
            3,
            "Mom's House",
            "12 Rizal Ave, Brgy. San Jose, Navotas",
            Icons.Default.LocationOn
        )
    ) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(addresses) { address ->
                _root_ide_package_.com.example.gastrack.ui.presentation.AddressCard(
                    address = address,
                    onSelect = {
                        // Logic to set as primary
                        val index = addresses.indexOf(address)
                        if (index != -1) {
                            val newList = addresses.map { it.copy(isPrimary = it.id == address.id) }
                            addresses.clear()
                            addresses.addAll(newList)
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        _root_ide_package_.com.example.gastrack.ui.presentation.AddAddressDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { label, location, type ->
                val icon = when (type) {
                    "Home" -> Icons.Default.Home
                    "Work" -> Icons.Default.Work
                    else -> Icons.Default.LocationOn
                }
                val newId = (addresses.maxOfOrNull { it.id } ?: 0) + 1
                addresses.add(
                    _root_ide_package_.com.example.gastrack.ui.presentation.AddressItem(
                        newId,
                        label,
                        location,
                        icon
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddressCard(address: com.example.gastrack.ui.presentation.AddressItem, onSelect: () -> Unit) {
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
                color = if (address.isPrimary) GasTrackRed.copy(alpha = 0.1f) else Color(0xFFF1F3F4)
            ) {
                Icon(
                    imageVector = address.icon,
                    contentDescription = null,
                    tint = if (address.isPrimary) GasTrackRed else StaffPortalBlue,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.label,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                    if (address.isPrimary) {
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
                    text = address.address,
                    fontSize = 13.sp,
                    color = TextGray,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            RadioButton(
                selected = address.isPrimary,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = GasTrackRed)
            )
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
    _root_ide_package_.com.example.gastrack.ui.presentation.AddressesScreen(onBack = {})
}
