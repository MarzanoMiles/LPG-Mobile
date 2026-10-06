package com.example.gastrack.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastrack.data.remote.dto.ComplianceReportDto
import com.example.gastrack.ui.theme.*
import com.example.gastrack.viewmodel.*

private fun statusColor(status: String): Pair<Color, Color> = when (status) {
    "Submitted" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    "Overdue" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    "Due Soon" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
    else -> Color(0xFFE3F2FD) to StaffPortalBlue // "Upcoming"
}

@Composable
fun ReportsComplianceEmployeeScreen(
    onBack: () -> Unit = {},
    complianceViewModel: ComplianceViewModel = viewModel()
) {
    var showScheduleDialog by remember { mutableStateOf(false) }

    val uiState by complianceViewModel.uiState.collectAsState()
    val summaryState by complianceViewModel.summaryState.collectAsState()
    val busyReportId by complianceViewModel.busyReportId.collectAsState()
    val actionMessage by complianceViewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            complianceViewModel.clearActionMessage()
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
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = "Reports & Compliance",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                    )
                    Text(text = "Admin", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
                IconButton(onClick = { showScheduleDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Schedule Report", tint = Color.White)
                }
            }
        }

        // --- Summary stat row ---
        val summary = summaryState
        if (summary is ComplianceSummaryUiState.Success) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatChip("Due Soon", summary.summary.dueSoon, Color(0xFFEF6C00), Modifier.weight(1f))
                SummaryStatChip("Overdue", summary.summary.overdue, Color(0xFFC62828), Modifier.weight(1f))
                SummaryStatChip("Submitted", summary.summary.submittedThisMonth, Color(0xFF2E7D32), Modifier.weight(1f))
                SummaryStatChip("Total", summary.summary.total, StaffPortalBlue, Modifier.weight(1f))
            }
        }

        when (val state = uiState) {
            is ComplianceUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StaffPortalButtonBlue)
                }
            }
            is ComplianceUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GasTrackRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Couldn't load reports", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.message, fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { complianceViewModel.loadReports() },
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is ComplianceUiState.Success -> {
                if (state.reports.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No compliance reports scheduled", color = TextGray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap + to schedule one", color = TextGray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(state.reports, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                isBusy = busyReportId == report.id,
                                onGenerate = { complianceViewModel.generateReport(report.id) },
                                onSubmit = { complianceViewModel.submitReport(report.id) },
                                onDelete = { complianceViewModel.deleteReport(report.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showScheduleDialog) {
        ScheduleReportDialog(
            onDismiss = { showScheduleDialog = false },
            onSave = { reportName, reportType, periodLabel, periodStart, periodEnd, dueDate ->
                complianceViewModel.createReport(reportName, reportType, periodLabel, periodStart, periodEnd, dueDate)
                showScheduleDialog = false
            }
        )
    }
}

@Composable
fun SummaryStatChip(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        }
    }
}

@Composable
fun ReportCard(
    report: ComplianceReportDto,
    isBusy: Boolean,
    onGenerate: () -> Unit,
    onSubmit: () -> Unit,
    onDelete: () -> Unit
) {
    val (bg, fg) = statusColor(report.status)
    val isSubmitted = report.status == "Submitted"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = fg.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = report.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = "${report.type} • ${report.period}", fontSize = 11.sp, color = TextGray)
                    Text(text = "Due: ${report.dueDate.take(10)}", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    if (isSubmitted && report.submittedAt != null) {
                        Text(text = "Submitted: ${report.submittedAt.take(10)}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                    }
                }

                Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = report.status,
                        color = fg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }

            if (!report.fileName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "File: ${report.fileName}", fontSize = 11.sp, color = TextGray)
            }

            if (!isSubmitted) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onGenerate,
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (report.fileName.isNullOrBlank()) "Generate" else "Regenerate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Mark Submitted", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleReportDialog(
    onDismiss: () -> Unit,
    onSave: (reportName: String, reportType: String, periodLabel: String, periodStart: String, periodEnd: String, dueDate: String) -> Unit
) {
    var reportName by remember { mutableStateOf("") }
    var reportType by remember { mutableStateOf("Sales Summary") }
    var typeExpanded by remember { mutableStateOf(false) }
    var periodLabel by remember { mutableStateOf("") }
    var periodStart by remember { mutableStateOf("") }
    var periodEnd by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    val types = listOf("Sales Summary", "Inventory Audit", "Restocking Logs")
    val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")

    val canSave = reportName.isNotBlank() && periodLabel.isNotBlank() &&
            dateRegex.matches(periodStart) && dateRegex.matches(periodEnd) && dateRegex.matches(dueDate)

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Schedule Report", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reportName,
                    onValueChange = { reportName = it },
                    label = { Text("Report Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = reportType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Report Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        types.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { reportType = option; typeExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = periodLabel,
                    onValueChange = { periodLabel = it },
                    label = { Text("Period Label") },
                    placeholder = { Text("e.g. Q3 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = periodStart,
                        onValueChange = { periodStart = it },
                        label = { Text("Period Start") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = periodEnd,
                        onValueChange = { periodEnd = it },
                        label = { Text("Period End") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel")
                    }
                    Button(
                        enabled = canSave,
                        onClick = { onSave(reportName.trim(), reportType, periodLabel.trim(), periodStart, periodEnd, dueDate) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StaffPortalButtonBlue)
                    ) {
                        Text("Schedule")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsComplianceEmployeeScreenPreview() {
    ReportsComplianceEmployeeScreen()
}