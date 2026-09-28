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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.R
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.AddressViewModel
import com.example.gastrack.viewmodel.CartViewModel
import com.example.gastrack.viewmodel.CheckoutUiState
import com.example.gastrack.viewmodel.CheckoutViewModel
import java.util.Locale

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPlaceOrder: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToAddresses: () -> Unit = {},
    cartViewModel: CartViewModel = viewModel(),
    checkoutViewModel: CheckoutViewModel = viewModel(),
    addressViewModel: AddressViewModel = viewModel()
) {
    var selectedPayment by remember { mutableStateOf("GCash") }
    var deliveryType by remember { mutableStateOf("Express") }

    val lineItems = cartViewModel.getLineItems()
    val subtotal = cartViewModel.subtotal()
    val deliveryCharge = 30.0
    val totalAmount = subtotal + deliveryCharge

    val primaryAddress by addressViewModel.primaryAddress.collectAsState()

    val checkoutState by checkoutViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Refresh the primary address every time this screen becomes visible
    // (e.g. returning from AddressesScreen after changing the selection)
    LaunchedEffect(Unit) {
        addressViewModel.loadPrimaryAddress()
    }

    LaunchedEffect(checkoutState) {
        when (val state = checkoutState) {
            is CheckoutUiState.Success -> {
                checkoutViewModel.resetState()
                cartViewModel.clear()
                onPlaceOrder()
                onNavigateToTracking()
            }
            is CheckoutUiState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                checkoutViewModel.resetState()
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
                        text = "Checkout",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            checkoutViewModel.placeOrder(
                                items = cartViewModel.quantities.toMap(),
                                orderType = "Delivery",
                                paymentMethod = selectedPayment,
                                amountPaid = totalAmount,
                                deliveryAddress = primaryAddress?.AddressLine ?: "No address on file",
                                deliveryCharge = deliveryCharge
                            )
                        },
                        enabled = lineItems.isNotEmpty() && primaryAddress != null && checkoutState !is CheckoutUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(12.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonOrange,
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (checkoutState is CheckoutUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Place Order",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
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

            // --- Delivery Address (real, from backend) ---
            Text(
                text = "DELIVERY ADDRESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextGray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
            CheckoutSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = GasTrackBlue.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = GasTrackBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = primaryAddress?.Label ?: "No saved address",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                            Text(
                                text = primaryAddress?.AddressLine ?: "Add a delivery address to continue",
                                fontSize = 13.sp,
                                color = TextGray,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    TextButton(onClick = onNavigateToAddresses) {
                        Text(
                            text = if (primaryAddress == null) "Add" else "Change",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = GasTrackRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Delivery Time ---
            Text(
                text = "DELIVERY OPTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextGray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .clickable { deliveryType = "Express" }
                    .border(
                        1.dp,
                        if (deliveryType == "Express") ButtonOrange else Color.Transparent,
                        RoundedCornerShape(24.dp)
                    ),
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
                        color = ButtonOrange.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = ButtonOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Express Delivery",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Text(
                            text = "Estimated arrival: 30 - 45 mins",
                            fontSize = 13.sp,
                            color = ButtonOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    RadioButton(
                        selected = deliveryType == "Express",
                        onClick = { deliveryType = "Express" },
                        colors = RadioButtonDefaults.colors(selectedColor = ButtonOrange)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Payment Method ---
            Text(
                text = "PAYMENT METHOD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextGray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentOption(
                    name = "GCash",
                    painter = painterResource(id = R.drawable.gcash_logo),
                    isSelected = selectedPayment == "GCash"
                ) { selectedPayment = "GCash" }

                PaymentOption(
                    name = "Maya",
                    painter = painterResource(id = R.drawable.mayalogo),
                    isSelected = selectedPayment == "Maya"
                ) { selectedPayment = "Maya" }

                PaymentOption(
                    name = "Cash on Delivery",
                    icon = Icons.Default.AccountBalanceWallet,
                    isSelected = selectedPayment == "Cash on Delivery"
                ) { selectedPayment = "Cash on Delivery" }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Order Summary (real cart data) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SummaryRow("Subtotal", "₱${String.format(Locale.US, "%,.2f", subtotal)}")
                    SummaryRow("Delivery Fee", "₱${String.format(Locale.US, "%,.2f", deliveryCharge)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F3F4))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Total",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Text(
                            text = "₱${String.format(Locale.US, "%,.2f", totalAmount)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = GasTrackRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CheckoutSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun PaymentOption(
    name: String,
    isSelected: Boolean,
    icon: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                if (isSelected) StaffPortalBlue else Color.Transparent,
                RoundedCornerShape(20.dp)
            ),
        color = Color.White,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8F9FA)
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).fillMaxSize()
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = StaffPortalBlue,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = StaffPortalBlue,
                    unselectedColor = Color.LightGray
                )
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    CheckoutScreen(onBack = {}, onPlaceOrder = {}, onNavigateToTracking = {})
}