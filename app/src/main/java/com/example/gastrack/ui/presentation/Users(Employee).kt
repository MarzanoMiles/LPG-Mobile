package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.ActivityLogDto
import com.example.gastrack.data.remote.dto.RoleDto
import com.example.gastrack.data.remote.dto.UserListItemDto
import com.example.gastrack.data.remote.dto.WarehouseDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.*

data class UserFormData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val roleId: Int,
    val warehouseId: Int?,
    val isActive: Boolean
)

@Composable
fun UsersEmployeeScreen(
    onBack: () -> Unit = {},
    userManagementViewModel: UserManagementViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Users") }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserListItemDto?>(null) }

    val roles by userManagementViewModel.roles.collectAsState()
    val warehouses by userManagementViewModel.warehouses.collectAsState()
    val actionMessage by userManagementViewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            userManagementViewModel.clearActionMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StaffPortalButtonBlue)
                .padding(top = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(
                            text = "Users",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            color = Color.White,
                        )
                        Text(
                            text = "Admin",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(44.dp)
                        .shadow(2.dp, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(2.dp)
                                .background(
                                    if (selectedTab == "Users") StaffPortalBlue else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTab = "Users" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Users", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(2.dp)
                                .background(
                                    if (selectedTab == "Activity Log") StaffPortalBlue else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTab = "Activity Log" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Activity Log", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = selectedTab, label = "UserTabTransition") { tab ->
                when (tab) {
                    "Users" -> UsersListTab(
                        userManagementViewModel = userManagementViewModel,
                        onAddClick = { showAddUserDialog = true },
                        onEditClick = { user -> editingUser = user }
                    )
                    "Activity Log" -> ActivityLogTab(userManagementViewModel = userManagementViewModel)
                }
            }
        }
    }

    // --- Create ---
    if (showAddUserDialog) {
        UserFormDialog(
            existing = null,
            roles = roles,
            warehouses = warehouses,
            onDismiss = { showAddUserDialog = false },
            onSave = { form ->
                userManagementViewModel.addUser(
                    firstName = form.firstName,
                    lastName = form.lastName,
                    email = form.email,
                    password = form.password,
                    roleId = form.roleId,
                    warehouseId = form.warehouseId
                )
                showAddUserDialog = false
            }
        )
    }

    // --- Edit ---
    editingUser?.let { user ->
        UserFormDialog(
            existing = user,
            roles = roles,
            warehouses = warehouses,
            onDismiss = { editingUser = null },
            onSave = { form ->
                userManagementViewModel.updateUser(
                    userId = user.UserID,
                    firstName = form.firstName,
                    lastName = form.lastName,
                    roleId = form.roleId,
                    warehouseId = form.warehouseId,
                    status = if (form.isActive) "Active" else "Inactive"
                )
                editingUser = null
            }
        )
    }
}

@Composable
fun UsersListTab(
    userManagementViewModel: UserManagementViewModel,
    onAddClick: () -> Unit,
    onEditClick: (UserListItemDto) -> Unit
) {
    val uiState by userManagementViewModel.userListState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is UserListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is UserListUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load users", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { userManagementViewModel.loadUsers() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                        Text("Retry")
                    }
                }
            }
            is UserListUiState.Success -> {
                if (state.users.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No staff accounts yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.users, key = { it.UserID }) { user ->
                            UserCard(
                                user = user,
                                onEdit = { onEditClick(user) },
                                onDeactivate = { userManagementViewModel.deactivateUser(user.UserID) },
                                onReactivate = {
                                    userManagementViewModel.updateUser(
                                        userId = user.UserID,
                                        firstName = user.FirstName,
                                        lastName = user.LastName,
                                        roleId = user.RoleID,
                                        warehouseId = user.WarehouseID,
                                        status = "Active"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Add User", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun UserCard(
    user: UserListItemDto,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit
) {
    val isActive = user.Status == "Active"

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "U-${user.UserID}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = user.RoleName, fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = user.WarehouseName ?: "Unassigned", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Surface(
                        color = if (isActive) Color(0xFF4CAF50) else Color.Red,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = user.Status,
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                        Text("Full Name", fontSize = 9.sp, color = Color.Gray)
                        Text("${user.FirstName} ${user.LastName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Email", fontSize = 9.sp, color = Color.Gray)
                        Text(user.Email, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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

@Composable
fun ActivityLogTab(userManagementViewModel: UserManagementViewModel) {
    val uiState by userManagementViewModel.activityLogState.collectAsState()

    when (val state = uiState) {
        is ActivityLogUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StaffPortalButtonBlue)
            }
        }
        is ActivityLogUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Couldn't load activity log: ${state.message}", color = GasTrackRed)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { userManagementViewModel.loadActivityLog() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                    Text("Retry")
                }
            }
        }
        is ActivityLogUiState.Success -> {
            if (state.logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No activity recorded yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.logs, key = { it.ActivityID }) { log ->
                        ActivityLogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLogCard(log: ActivityLogDto) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "LOG-${log.ActivityID}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = log.RoleName, fontSize = 11.sp, color = Color.Gray)
                }
                Text(text = log.Description ?: log.ActivityType, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Module", fontSize = 9.sp, color = Color.Gray)
                        Text(log.Module, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Action", fontSize = 9.sp, color = Color.Gray)
                        Text(log.ActivityType, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${log.FirstName} ${log.LastName}",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = log.ActivityDate.take(19).replace("T", " "),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * One dialog for both creating and editing a user.
 * - existing == null  -> create mode (email + temporary password required)
 * - existing != null  -> edit mode (email is read-only, no password field, status switch shown)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormDialog(
    existing: UserListItemDto?,
    roles: List<RoleDto>,
    warehouses: List<WarehouseDto>,
    onDismiss: () -> Unit,
    onSave: (UserFormData) -> Unit
) {
    val isEdit = existing != null

    var firstName by remember { mutableStateOf(existing?.FirstName ?: "") }
    var lastName by remember { mutableStateOf(existing?.LastName ?: "") }
    var email by remember { mutableStateOf(existing?.Email ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedRoleId by remember { mutableStateOf(existing?.RoleID) }
    var selectedWarehouseId by remember { mutableStateOf(existing?.WarehouseID) }
    var isActive by remember { mutableStateOf(existing?.Status != "Inactive") }

    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var warehouseDropdownExpanded by remember { mutableStateOf(false) }

    // Roles load asynchronously: pick a sensible default for "create" once they arrive.
    LaunchedEffect(roles) {
        if (selectedRoleId == null) selectedRoleId = roles.firstOrNull()?.RoleID
    }

    val canSave = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            selectedRoleId != null &&
            (isEdit || (email.contains("@") && password.length >= 4))

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEdit) "Edit User" else "New User Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("BASIC INFORMATION", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    enabled = !isEdit,
                    supportingText = if (isEdit) {
                        { Text("Email can't be changed after the account is created") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                if (!isEdit) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Temporary Password") },
                        supportingText = { Text("At least 4 characters") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Role & Assignment", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                // --- Role ---
                if (roles.isEmpty()) {
                    Text("Loading roles…", fontSize = 12.sp, color = Color.Gray)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = roleDropdownExpanded,
                        onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = roles.find { it.RoleID == selectedRoleId }?.RoleName ?: "Select role",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = roleDropdownExpanded,
                            onDismissRequest = { roleDropdownExpanded = false }
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.RoleName) },
                                    onClick = {
                                        selectedRoleId = role.RoleID
                                        roleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Warehouse ---
                ExposedDropdownMenuBox(
                    expanded = warehouseDropdownExpanded,
                    onExpandedChange = { warehouseDropdownExpanded = !warehouseDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = warehouses.find { it.WarehouseID == selectedWarehouseId }?.WarehouseName ?: "Unassigned",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Branch / Warehouse") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = warehouseDropdownExpanded,
                        onDismissRequest = { warehouseDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Unassigned") },
                            onClick = {
                                selectedWarehouseId = null
                                warehouseDropdownExpanded = false
                            }
                        )
                        warehouses.forEach { warehouse ->
                            DropdownMenuItem(
                                text = { Text(warehouse.WarehouseName) },
                                onClick = {
                                    selectedWarehouseId = warehouse.WarehouseID
                                    warehouseDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // --- Status (edit only) ---
                if (isEdit) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Account Active", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = if (isActive) "User can log in" else "User is blocked from logging in",
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
                            selectedRoleId?.let { roleId ->
                                onSave(
                                    UserFormData(
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        email = email.trim(),
                                        password = password,
                                        roleId = roleId,
                                        warehouseId = selectedWarehouseId,
                                        isActive = isActive
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text(if (isEdit) "Save Changes" else "Save User", color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsersEmployeeScreenPreview() {
    UsersEmployeeScreen()
}