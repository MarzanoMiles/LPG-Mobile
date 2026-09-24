package com.example.gastrack.ui.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastrack.ui.theme.*

enum class DataTab {
    EXPORT, EXPORT_LOGS, IMPORT, IMPORT_LOGS
}

@Composable
fun DataModuleEmployeeScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(_root_ide_package_.com.example.gastrack.ui.presentation.DataTab.EXPORT) }

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
                    _root_ide_package_.com.example.gastrack.ui.presentation.DataTabItem(
                        "Export",
                        selectedTab == _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.EXPORT
                    ) {
                        selectedTab =
                            _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.EXPORT
                    }
                    _root_ide_package_.com.example.gastrack.ui.presentation.DataTabItem(
                        "Export Logs",
                        selectedTab == _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.EXPORT_LOGS
                    ) {
                        selectedTab =
                            _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.EXPORT_LOGS
                    }
                    _root_ide_package_.com.example.gastrack.ui.presentation.DataTabItem(
                        "Import",
                        selectedTab == _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.IMPORT
                    ) {
                        selectedTab =
                            _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.IMPORT
                    }
                    _root_ide_package_.com.example.gastrack.ui.presentation.DataTabItem(
                        "Import Logs",
                        selectedTab == _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.IMPORT_LOGS
                    ) {
                        selectedTab =
                            _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.IMPORT_LOGS
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Crossfade(targetState = selectedTab, label = "DataTabTransition") { tab ->
            when (tab) {
                _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.EXPORT -> _root_ide_package_.com.example.gastrack.ui.presentation.ExportImportContent(
                    isExport = true
                )
                _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.IMPORT -> _root_ide_package_.com.example.gastrack.ui.presentation.ExportImportContent(
                    isExport = false
                )
                _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.EXPORT_LOGS -> _root_ide_package_.com.example.gastrack.ui.presentation.LogsContent(
                    isExportLogs = true
                )
                _root_ide_package_.com.example.gastrack.ui.presentation.DataTab.IMPORT_LOGS -> _root_ide_package_.com.example.gastrack.ui.presentation.LogsContent(
                    isExportLogs = false
                )
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
fun ExportImportContent(isExport: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = if (isExport) "Data Export" else "Data Import",
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
                // Data Type Dropdown
                _root_ide_package_.com.example.gastrack.ui.presentation.DataOptionField(
                    label = "DATA TYPE",
                    value = "Sales Data"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Data Range Dropdown
                _root_ide_package_.com.example.gastrack.ui.presentation.DataOptionField(
                    label = "DATA RANGE",
                    value = "Today"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status Box
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
                            imageVector = if (isExport) Icons.Default.Description else Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextDark
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isExport) "Ready to Export" else "Ready to Import",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Select your preferred format to download",
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            _root_ide_package_.com.example.gastrack.ui.presentation.FormatButton(
                                "CSV",
                                Icons.Default.FileDownload,
                                Modifier.weight(1f)
                            )
                            _root_ide_package_.com.example.gastrack.ui.presentation.FormatButton(
                                "Excel",
                                Icons.Default.TableChart,
                                Modifier.weight(1f)
                            )
                            _root_ide_package_.com.example.gastrack.ui.presentation.FormatButton(
                                "PDF",
                                Icons.Default.PictureAsPdf,
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataOptionField(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = TextDark,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
fun FormatButton(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextDark)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LogsContent(isExportLogs: Boolean) {
    val logs = listOf(
        _root_ide_package_.com.example.gastrack.ui.presentation.DataLog(
            "sales.xlsx",
            "Sales",
            "2026-05-15",
            3,
            true
        ),
        _root_ide_package_.com.example.gastrack.ui.presentation.DataLog(
            "inventory.csv",
            "Inventory",
            "2026-05-18",
            0,
            false
        ),
        _root_ide_package_.com.example.gastrack.ui.presentation.DataLog(
            "inventory.csv",
            "Inventory",
            "2026-05-15",
            0,
            true
        ),
        _root_ide_package_.com.example.gastrack.ui.presentation.DataLog(
            "gasulpetron.csv",
            "Sales",
            "2026-05-15",
            0,
            true
        ),
        _root_ide_package_.com.example.gastrack.ui.presentation.DataLog(
            "pos.pdf",
            "Inventory",
            "2026-05-15",
            0,
            false
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Validation History Header
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
                    text = if (isExportLogs) "Data Export Validation History" else "Data Import Validation History",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs) { log ->
                _root_ide_package_.com.example.gastrack.ui.presentation.LogCard(log)
            }
        }
    }
}

data class DataLog(val filename: String, val type: String, val date: String, val errors: Int, val success: Boolean)

@Composable
fun LogCard(log: com.example.gastrack.ui.presentation.DataLog) {
    var expanded by remember { mutableStateOf(log.filename == "sales.xlsx" && log.success) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
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
                    Text(text = log.filename, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Surface(
                    color = if (log.success) Color(0xFF4CAF50) else Color(0xFFE57373),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (log.success) "SUCCESS" else "FAILED",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                _root_ide_package_.com.example.gastrack.ui.presentation.LogDetailItem(
                    "TYPE",
                    log.type,
                    Modifier.weight(1f)
                )
                _root_ide_package_.com.example.gastrack.ui.presentation.LogDetailItem(
                    "DATE",
                    log.date,
                    Modifier.weight(1.5f)
                )
                _root_ide_package_.com.example.gastrack.ui.presentation.LogDetailItem(
                    "ERRORS",
                    log.errors.toString(),
                    Modifier.weight(1f),
                    isError = log.errors > 0
                )

                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Errors", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Error Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Retry Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
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
    _root_ide_package_.com.example.gastrack.ui.presentation.DataModuleEmployeeScreen()
}
