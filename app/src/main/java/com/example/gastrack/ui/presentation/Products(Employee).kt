package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.BrandDto
import com.example.gastrack.data.remote.dto.CategoryDto
import com.example.gastrack.data.remote.dto.ProductDto
import com.example.gastrack.data.remote.dto.SupplierDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.util.resolveProductImageRes
import com.example.gastrack.viewmodel.ProductManagementViewModel
import com.example.gastrack.viewmodel.ProductMgmtUiState
import java.util.Locale

data class ProductFormData(
    val name: String,
    val categoryId: Int,
    val brandId: Int,
    val supplierId: Int,
    val unit: String,
    val unitPrice: Double,
    val costPrice: Double,
    val reorderLevel: Int,
    val isActive: Boolean
)

@Composable
fun ProductsEmployeeScreen(
    onBack: () -> Unit = {},
    productManagementViewModel: ProductManagementViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductDto?>(null) }

    val uiState by productManagementViewModel.uiState.collectAsState()
    val categories by productManagementViewModel.categories.collectAsState()
    val brands by productManagementViewModel.brands.collectAsState()
    val suppliers by productManagementViewModel.suppliers.collectAsState()
    val actionMessage by productManagementViewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            productManagementViewModel.clearActionMessage()
        }
    }

    // Most common unit among existing products, used as the default for new ones.
    val defaultUnit = (uiState as? ProductMgmtUiState.Success)
        ?.products
        ?.groupingBy { it.Unit }
        ?.eachCount()
        ?.maxByOrNull { it.value }
        ?.key
        ?: "pcs"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(bottomEnd = 48.dp))
                .background(StaffPortalButtonBlue, RoundedCornerShape(bottomEnd = 48.dp))
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = "Product List",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                    )
                    Text(
                        text = "Manage your catalog",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
                }
            }
        }

        when (val state = uiState) {
            is ProductMgmtUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is ProductMgmtUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load products", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { productManagementViewModel.loadProducts() },
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is ProductMgmtUiState.Success -> {
                if (state.products.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No products yet", color = TextGray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap + to add your first product", color = TextGray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.products, key = { it.ProductID }) { product ->
                            ProductManagementCard(
                                product = product,
                                onEdit = { editingProduct = product },
                                onDeactivate = { productManagementViewModel.deactivateProduct(product.ProductID) },
                                onReactivate = { productManagementViewModel.reactivateProduct(product) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Create ---
    if (showAddDialog) {
        ProductFormDialog(
            existing = null,
            defaultUnit = defaultUnit,
            categories = categories,
            brands = brands,
            suppliers = suppliers,
            onDismiss = { showAddDialog = false },
            onSave = { form ->
                productManagementViewModel.addProduct(
                    name = form.name,
                    categoryId = form.categoryId,
                    brandId = form.brandId,
                    supplierId = form.supplierId,
                    unit = form.unit,
                    unitPrice = form.unitPrice,
                    costPrice = form.costPrice,
                    reorderLevel = form.reorderLevel
                )
                showAddDialog = false
            }
        )
    }

    // --- Edit ---
    editingProduct?.let { product ->
        ProductFormDialog(
            existing = product,
            defaultUnit = defaultUnit,
            categories = categories,
            brands = brands,
            suppliers = suppliers,
            onDismiss = { editingProduct = null },
            onSave = { form ->
                productManagementViewModel.updateProduct(
                    id = product.ProductID,
                    name = form.name,
                    categoryId = form.categoryId,
                    brandId = form.brandId,
                    supplierId = form.supplierId,
                    unit = form.unit,
                    unitPrice = form.unitPrice,
                    costPrice = form.costPrice,
                    reorderLevel = form.reorderLevel,
                    status = if (form.isActive) "Active" else "Inactive"
                )
                editingProduct = null
            }
        )
    }
}

@Composable
fun ProductManagementCard(
    product: ProductDto,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit
) {
    val isActive = product.Status == "Active"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = resolveProductImageRes(product.ProductName)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.ProductName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "${product.Category} • ${product.Brand}", color = Color.Gray, fontSize = 12.sp)
                    Text(text = "Supplier: ${product.SupplierName}", color = Color.Gray, fontSize = 11.sp)
                }
                Surface(
                    color = if (isActive) Color(0xFF4CAF50) else Color(0xFFE57373),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = product.Status,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unit Price", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            String.format(Locale.US, "₱%,.2f", product.UnitPrice.toDoubleOrNull() ?: 0.0),
                            color = StaffPortalButtonBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cost Price", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            String.format(Locale.US, "₱%,.2f", product.CostPrice.toDoubleOrNull() ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reorder At", fontSize = 10.sp, color = Color.Gray)
                        Text("${product.ReorderLevel} ${product.Unit}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SalesActionItem(
                    icon = Icons.Default.Edit,
                    text = "Edit",
                    color = Color(0xFF4CAF50),
                    onClick = onEdit
                )
                if (isActive) {
                    SalesActionItem(
                        icon = Icons.Default.Delete,
                        text = "Deactivate",
                        color = Color.Red,
                        onClick = onDeactivate
                    )
                } else {
                    SalesActionItem(
                        icon = Icons.Default.CheckCircle,
                        text = "Reactivate",
                        color = StaffPortalButtonBlue,
                        onClick = onReactivate
                    )
                }
            }
        }
    }
}

/** Generic dropdown used by the product form for category / brand / supplier. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ProductSelectField(
    label: String,
    options: List<T>,
    selectedId: Int?,
    idOf: (T) -> Int,
    labelOf: (T) -> String,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { idOf(it) == selectedId }?.let(labelOf) ?: "Select"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(labelOf(option)) },
                    onClick = {
                        onSelected(idOf(option))
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * One dialog for both creating and editing a product.
 * - existing == null -> create mode
 * - existing != null -> edit mode (pre-filled, shows the Active switch)
 */
@Composable
fun ProductFormDialog(
    existing: ProductDto?,
    defaultUnit: String,
    categories: List<CategoryDto>,
    brands: List<BrandDto>,
    suppliers: List<SupplierDto>,
    onDismiss: () -> Unit,
    onSave: (ProductFormData) -> Unit
) {
    val isEdit = existing != null

    var name by remember { mutableStateOf(existing?.ProductName ?: "") }
    var categoryId by remember { mutableStateOf(existing?.CategoryID) }
    var brandId by remember { mutableStateOf(existing?.BrandID) }
    var supplierId by remember { mutableStateOf(existing?.SupplierID) }
    var unit by remember { mutableStateOf(existing?.Unit ?: defaultUnit) }
    var unitPrice by remember { mutableStateOf(existing?.UnitPrice?.toDoubleOrNull()?.toString() ?: "") }
    var costPrice by remember { mutableStateOf(existing?.CostPrice?.toDoubleOrNull()?.toString() ?: "") }
    var reorderLevel by remember { mutableStateOf((existing?.ReorderLevel ?: 0).toString()) }
    var isActive by remember { mutableStateOf(existing?.Status != "Inactive") }

    // Inactive suppliers can't be chosen for new assignments, but keep the current one visible when editing.
    val selectableSuppliers = suppliers.filter { it.Status == "Active" || it.SupplierID == existing?.SupplierID }

    val unitPriceValue = unitPrice.toDoubleOrNull()
    val costPriceValue = costPrice.toDoubleOrNull()
    val reorderValue = reorderLevel.toIntOrNull()

    val canSave = name.isNotBlank() &&
            categoryId != null && brandId != null && supplierId != null &&
            unit.isNotBlank() &&
            unitPriceValue != null && costPriceValue != null && reorderValue != null

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEdit) "Edit Product" else "Add Product",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProductSelectField(
                    label = "Category",
                    options = categories,
                    selectedId = categoryId,
                    idOf = { it.CategoryID },
                    labelOf = { it.Category },
                    onSelected = { categoryId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProductSelectField(
                    label = "Brand",
                    options = brands,
                    selectedId = brandId,
                    idOf = { it.BrandID },
                    labelOf = { it.Brand },
                    onSelected = { brandId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProductSelectField(
                    label = "Supplier",
                    options = selectableSuppliers,
                    selectedId = supplierId,
                    idOf = { it.SupplierID },
                    labelOf = { it.SupplierName },
                    onSelected = { supplierId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { input ->
                            if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) unitPrice = input
                        },
                        label = { Text("Unit Price (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { input ->
                            if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) costPrice = input
                        },
                        label = { Text("Cost Price (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reorderLevel,
                    onValueChange = { input -> if (input.all { it.isDigit() }) reorderLevel = input },
                    label = { Text("Reorder Level") },
                    supportingText = { Text("Low-stock alerts trigger at or below this quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                if (isEdit) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Product Active", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = if (isActive) "Visible to customers" else "Hidden from the customer marketplace",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2))
                    ) {
                        Text("Cancel", color = Color.Black)
                    }
                    Button(
                        enabled = canSave,
                        onClick = {
                            val cat = categoryId
                            val brand = brandId
                            val sup = supplierId
                            if (cat != null && brand != null && sup != null &&
                                unitPriceValue != null && costPriceValue != null && reorderValue != null
                            ) {
                                onSave(
                                    ProductFormData(
                                        name = name.trim(),
                                        categoryId = cat,
                                        brandId = brand,
                                        supplierId = sup,
                                        unit = unit.trim(),
                                        unitPrice = unitPriceValue,
                                        costPrice = costPriceValue,
                                        reorderLevel = reorderValue,
                                        isActive = isActive
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text(if (isEdit) "Save Changes" else "Add Product", color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductsEmployeeScreenPreview() {
    ProductsEmployeeScreen()
}