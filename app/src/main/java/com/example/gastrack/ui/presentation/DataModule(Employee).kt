package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.DataActivityLogDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.DataLogUiState
import com.example.gastrack.viewmodel.DataModuleViewModel
import com.example.gastrack.viewmodel.ExportState

enum class DataTab {
    EXPORT, EXPORT_LOGS, IMPORT, IMPORT_LOGS
}

@Composable
fun DataModuleEmployeeScreen(
    onBack: () -> Unit = {},
    dataModuleViewModel: DataModuleViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(DataTab.EXPORT) }

    val exportState by dataModuleViewModel.exportState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportState.Success -> {
                android.widget.Toast.makeText(
                    context,
                    "Saved to ${state.filePath}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                dataModuleViewModel.resetExportState()
            }
            is ExportState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                dataModuleViewModel.resetExportState()
            }
            else -> {}
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
                            text = "Data Module",
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DataTabItem("Export", selectedTab == DataTab.EXPORT) { selectedTab = DataTab.EXPORT }
                    DataTabItem("Export Logs", selectedTab == DataTab.EXPORT_LOGS) { selectedTab = DataTab.EXPORT_LOGS }
                    DataTabItem("Import", selectedTab == DataTab.IMPORT) { selectedTab = DataTab.IMPORT }
                    DataTabItem("Import Logs", selectedTab == DataTab.IMPORT_LOGS) { selectedTab = DataTab.IMPORT_LOGS }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Crossfade(targetState = selectedTab, label = "DataTabTransition") { tab ->
            when (tab) {
                DataTab.EXPORT -> ExportContent(
                    dataModuleViewModel = dataModuleViewModel,
                    isExporting = exportState is ExportState.Loading
                )
                DataTab.IMPORT -> ImportComingSoonContent()
                DataTab.EXPORT_LOGS -> LogsContent(dataModuleViewModel = dataModuleViewModel, activityType = "Export")
                DataTab.IMPORT_LOGS -> LogsContent(dataModuleViewModel = dataModuleViewModel, activityType = "Import")
            }
        }
    }
}

@Composable
fun DataTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        )
        if (isSelected) {
            Box(modifier = Modifier.width(20.dp).height(2.dp).background(Color.White).padding(top = 2.dp))
        }
    }
}

@Composable
fun ExportContent(
    dataModuleViewModel: DataModuleViewModel,
    isExporting: Boolean
) {
    var dataType by remember { mutableStateOf("Sales") }
    var range by remember { mutableStateOf("Today") }
    var dataTypeExpanded by remember { mutableStateOf(false) }
    var rangeExpanded by remember { mutableStateOf(false) }

    val dataTypes = listOf("Sales", "Inventory")
    val ranges = listOf("Today", "Week", "Month")
    val rangeLabel = mapOf("Today" to "Today", "Week" to "This Week", "Month" to "This Month")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Data Export",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                DataDropdownField(
                    label = "DATA TYPE",
                    value = dataType,
                    options = dataTypes,
                    expanded = dataTypeExpanded,
                    onExpandedChange = { dataTypeExpanded = it },
                    onSelect = { dataType = it; dataTypeExpanded = false }
                )

                Spacer(modifier = Modifier.height(24.dp))

                DataDropdownField(
                    label = "DATA RANGE",
                    value = rangeLabel[range] ?: range,
                    options = ranges.map { rangeLabel[it] ?: it },
                    expanded = rangeExpanded,
                    onExpandedChange = { rangeExpanded = it },
                    onSelect = { selectedLabel ->
                        range = ranges.first { (rangeLabel[it] ?: it) == selectedLabel }
                        rangeExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF1F3F4).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextDark
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ready to Export",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Generates a real CSV from your database",
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { dataModuleViewModel.exportData(dataType, range) },
                            enabled = !isExporting,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export as CSV", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataDropdownField(
    label: String,
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = TextDark,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                color = Color(0xFFF1F3F4),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option) })
                }
            }
        }
    }
}

@Composable
fun ImportComingSoonContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Bulk Import Not Yet Available", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Importing data requires file upload, parsing, and validation on the server. This hasn't been built yet.",
            fontSize = 13.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LogsContent(dataModuleViewModel: DataModuleViewModel, activityType: String) {
    val uiState by dataModuleViewModel.logState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF1F3F4),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Data $activityType Validation History",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = uiState) {
            is DataLogUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is DataLogUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load logs", fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { dataModuleViewModel.loadLogs() }, colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)) {
                        Text("Retry")
                    }
                }
            }
            is DataLogUiState.Success -> {
                val filtered = state.logs.filter { it.activityType == activityType || it.activityType == "Generate Report" && activityType == "Export" }
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No $activityType activity yet", color = TextGray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.logId }) { log ->
                            LogCard(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogCard(log: DataActivityLogDto) {
    var expanded by remember { mutableStateOf(false) }
    val isSuccess = log.status == "Successful"

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = log.fileName ?: "Log #${log.logId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Surface(
                    color = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFE57373),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = log.status?.uppercase() ?: "UNKNOWN",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LogDetailItem("TYPE", log.dataType ?: "—", Modifier.weight(1f))
                LogDetailItem("FORMAT", log.fileFormat ?: "—", Modifier.weight(1f))
                LogDetailItem("DATE", log.activityDate.take(10), Modifier.weight(1.5f))

                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Activity: ${log.activityType ?: "—"}",
                    fontSize = 12.sp,
                    color = TextGray
                )
                if (log.dateFrom != null && log.dateTo != null) {
                    Text(
                        text = "Covers: ${log.dateFrom.take(10)} to ${log.dateTo.take(10)}",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = "By ${log.firstName} ${log.lastName}",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
fun LogDetailItem(label: String, value: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isError) Color.Red else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DataModuleEmployeeScreenPreview() {
    DataModuleEmployeeScreen()
}