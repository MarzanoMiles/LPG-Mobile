package com.example.gastrack.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.CartLineItem
import com.example.gastrack.viewmodel.CartViewModel
import java.util.Locale

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onContinueShopping: () -> Unit,
    cartViewModel: CartViewModel = viewModel()
) {
    val lineItems = cartViewModel.getLineItems()

    val subtotal = cartViewModel.subtotal()
    val deliveryCharge = if (lineItems.isEmpty()) 0.0 else 30.0
    val totalAmount = subtotal + deliveryCharge
    val totalItems = cartViewModel.totalItems()

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
                        text = "Cart Summary",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
        },
        bottomBar = {
            if (lineItems.isNotEmpty()) {
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
                            onClick = onNavigateToCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = "Proceed to Checkout",
                                fontSize = 18.sp,
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
        ) {
            if (lineItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your cart is empty", color = TextGray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(onClick = onContinueShopping) {
                            Text("Continue Shopping", color = GasTrackBlue, fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(lineItems, key = { it.productId }) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { newQty -> cartViewModel.setQuantity(item.productId, newQty) },
                            onRemove = { cartViewModel.removeItem(item.productId) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        // Summary Section
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(24.dp)),
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "ORDER SUMMARY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextGray,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                CartSummaryRow("Total Items", totalItems.toString())
                                CartSummaryRow("Subtotal", "₱${String.format(Locale.US, "%,.2f", subtotal)}")
                                CartSummaryRow("Delivery Charge", "₱${String.format(Locale.US, "%,.2f", deliveryCharge)}")
                                CartSummaryRow("Discount", "-₱0.00", isDiscount = true)

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFF1F3F4))
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Total Amount", fontSize = 17.sp, fontWeight = FontWeight.Black, color = TextDark)
                                    Text(
                                        text = "₱${String.format(Locale.US, "%,.2f", totalAmount)}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GasTrackRed
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = onContinueShopping) {
                                Text("Continue Shopping", color = GasTrackBlue, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { cartViewModel.clear() }) {
                                Text("Clear Cart", color = GasTrackRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartLineItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product Image
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8F9FA)
                ) {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp).fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextDark)
                    Text(
                        text = "Unit Price: ₱${String.format(Locale.US, "%,.2f", item.unitPrice)}",
                        fontSize = 13.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Subtotal: ₱${String.format(Locale.US, "%,.2f", item.unitPrice * item.quantity)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = StaffPortalBlue
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = GasTrackRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuantitySelector(
                    quantity = item.quantity,
                    onIncrement = { onQuantityChange(item.quantity + 1) },
                    onDecrement = { onQuantityChange(item.quantity - 1) }
                )
            }
        }
    }
}

@Composable
fun CartSummaryRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = TextGray, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = if (isDiscount) Color(0xFF2E7D32) else TextDark
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    CartScreen(onBack = {}, onNavigateToCheckout = {}, onContinueShopping = {})
}