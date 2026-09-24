package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gastrack.ui.theme.StaffPortalBlue
import com.example.gastrack.ui.theme.StaffPortalButtonBlue

data class UserItemData(
    val id: String,
    val role: String,
    val branch: String,
    val fullName: String,
    val email: String,
    val status: String
)

data class ActivityLogEntry(
    val id: String,
    val role: String,
    val branch: String,
    val module: String,
    val action: String,
    val fullName: String,
    val email: String,
    val timestamp: String
)

@Composable
fun UsersEmployeeScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Users") }
    var showAddUserDialog by remember { mutableStateOf(false) }

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

                // --- Tabs ---
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
                            Text(
                                text = "Users",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
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
                            Text(
                                text = "Activity Log",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = selectedTab, label = "UserTabTransition") { tab ->
                when (tab) {
                    "Users" -> UsersListTab(onAddClick = { showAddUserDialog = true })
                    "Activity Log" -> ActivityLogTab()
                }
            }
        }
    }

    if (showAddUserDialog) {
        UserEditDialog(onDismiss = { showAddUserDialog = false })
    }
}

@Composable
fun UsersListTab(onAddClick: () -> Unit) {
    val users = listOf(
        UserItemData("U-001", "Admin", "Pasig Warehouse", "Juan Dela Cruz", "juan.delacruz@abc.com", "active"),
        UserItemData("U-002", "Manager", "San Juan Warehouse", "Patrick Garcia", "patrick.garcia@abc.com", "active"),
        UserItemData("U-003", "Inventory Staff", "San Juan Warehouse", "Juana Tolentino", "juana.tolentino@abc.com", "active"),
        UserItemData("U-004", "Inventory Staff", "Pasig Warehouse", "Yvonne Cruz", "yvonne.cruz@abc.com", "active")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(users) { user ->
                UserCard(user)
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
fun UserCard(user: UserItemData) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = user.id, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = user.role, fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = user.branch, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Surface(
                        color = if (user.status == "active") Color(0xFF4CAF50) else Color.Red,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = user.status,
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
                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Username/Email", fontSize = 9.sp, color = Color.Gray)
                        Text(user.email, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SalesActionItem(Icons.Default.Description, "View", Color(0xFFFFD54F))
                SalesActionItem(Icons.Default.Edit, "Edit", Color(0xFF4CAF50))
                SalesActionItem(Icons.Default.Delete, "Delete", Color.Red)
            }
        }
    }
}

@Composable
fun ActivityLogTab() {
    val logs = listOf(
        ActivityLogEntry("U-001", "Admin", "Pasig Warehouse", "Export", "Exported Sales", "Juan Dela Cruz", "juan.delacruz@abc.com", "01/01/2026 10:30:00 AM"),
        ActivityLogEntry("U-002", "Manager", "San Juan Warehouse", "User", "Update Username", "Patrick Garcia", "patrick.garcia@abc.com", "01/01/2026 10:30:00 AM"),
        ActivityLogEntry("U-003", "Inventory Staff", "San Juan Warehouse", "Inventory", "Add Stocks", "Juana Tolentino", "juana.tolentino@abc.com", "01/01/2026 10:30:00 AM"),
        ActivityLogEntry("U-004", "Inventory Staff", "Pasig Warehouse", "Inventory", "Deduct Stocks", "Yvonne Cruz", "yvonne.cruz@abc.com", "01/01/2026 10:30:00 AM")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(logs) { log ->
            ActivityLogCard(log)
        }
    }
}

@Composable
fun ActivityLogCard(log: ActivityLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = log.id, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = log.role, fontSize = 11.sp, color = Color.Gray)
                }
                Text(text = log.branch, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        Text(log.module, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Action", fontSize = 9.sp, color = Color.Gray)
                        Text(log.action, fontWeight = FontWeight.Bold)
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
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Full Name", fontSize = 9.sp, color = Color.Gray)
                        Text(log.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Username/Email", fontSize = 9.sp, color = Color.Gray)
                        Text(log.email, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = log.timestamp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UserEditDialog(onDismiss: () -> Unit) {
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
                Text("New User Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("BASIC INFORMATION", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                UserDetailField("Full Name", "ABC Company")
                Spacer(modifier = Modifier.height(8.dp))
                UserDetailField("Username / Email Address", "killianjulian@gmail.com")
                Spacer(modifier = Modifier.height(8.dp))
                UserDetailField("Confirm Password", "● ● ● ● ● ● ● ● ● ● ●")

                Spacer(modifier = Modifier.height(16.dp))
                Text("Role & Assignment", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                UserDropdownField("Role", "Select role")
                Spacer(modifier = Modifier.height(8.dp))
                UserDropdownField("Branch / Warehouse", "Select Branch / Warehouse")
                Spacer(modifier = Modifier.height(8.dp))
                UserDropdownField("Status", "Active")

                Spacer(modifier = Modifier.height(16.dp))
                Text("Module Access", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        ModuleAccessCheckbox("Dashboard", true)
                        ModuleAccessCheckbox("Inventory", false)
                        ModuleAccessCheckbox("Supplier Module", false)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ModuleAccessCheckbox("POS Terminal", false)
                        ModuleAccessCheckbox("Product Module", false)
                        ModuleAccessCheckbox("Data", false)
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
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text("Save User", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(value, fontSize = 13.sp, color = if (value.contains("Select") || value.contains("Enter")) Color.LightGray else Color.Black)
        }
    }
}

@Composable
fun UserDropdownField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = value, fontSize = 13.sp, color = if (value.contains("Select")) Color.Gray else Color.Black)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ModuleAccessCheckbox(label: String, checked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(if (checked) StaffPortalBlue else Color.Transparent, RoundedCornerShape(4.dp))
                .border(1.dp, if (checked) StaffPortalBlue else Color.Gray, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun UsersEmployeeScreenPreview() {
    UsersEmployeeScreen()
}
