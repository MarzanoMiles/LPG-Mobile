package com.example.gastrack.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.R
import com.example.gastrack.ui.theme.ButtonOrange
import com.example.gastrack.ui.theme.GasTrackRed
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.TextDark
import com.example.gastrack.ui.theme.TextGray
import java.util.Locale

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val imageRes: Int,
    val stock: Int,
    val brand: String,
    val isPopular: Boolean = false,
)

@Composable
fun ProductCard(
    product: Product,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onBuyNow: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Product Image with stylized background
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = product.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = StaffPortalBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = product.brand.uppercase(Locale.US),
                                color = StaffPortalBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                letterSpacing = 0.5.sp,
                            )
                        }
                        if (product.isPopular) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = GasTrackRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                            ) {
                                Text(
                                    text = "POPULAR",
                                    color = GasTrackRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    letterSpacing = 0.5.sp,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val formattedPrice = String.format(Locale.US, "%,d", product.price)
                Column {
                    Text(
                        text = "PRICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = "₱$formattedPrice",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = StaffPortalBlue,
                    )
                }

                if (quantity > 0) {
                    QuantitySelector(
                        quantity = quantity,
                        onIncrement = { onQuantityChange(quantity + 1) },
                        onDecrement = { onQuantityChange(quantity - 1) },
                    )
                } else {
                    Button(
                        onClick = { onQuantityChange(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD TO CART", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBuyNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("BUY NOW", fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StaffPortalBlue, modifier = Modifier.size(16.dp))
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center,
        )
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = StaffPortalBlue, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        ProductCard(
            product = Product(
                id = 1,
                name = "11kg Standard Refill",
                description = "Perfect for household cooking.",
                price = 950,
                imageRes = R.drawable.tank_11kg,
                stock = 15,
                brand = "Petron",
                isPopular = true,
            ),
            quantity = 0,
            onQuantityChange = {},
        )
    }
}
