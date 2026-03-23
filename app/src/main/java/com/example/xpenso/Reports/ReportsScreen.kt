package com.example.xpenso.Reports

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.Transaction.CustomRangeDialog
import com.example.xpenso.Transaction.EndDatePickerDialog
import com.example.xpenso.Transaction.StartDatePickerDialog
import com.example.xpenso.utils.AppHeaderBar
import android.content.Intent
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.FileProvider
import com.example.xpenso.Reports.ExportActionDialog
import com.example.xpenso.ExpenseViewmodel.BackupRestoreState
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.ExpenseViewmodel.ExportType
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.ExpenseCategory
import java.io.File

private enum class ExportRequest {
    MONTHLY, CATEGORY, JSON, PDF, CSV, CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    currencySymbol: String,
    settingsStore: SettingsStore,
    expenseViewModel: ExpenseViewModel,
    onMonthlyReport: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onPremiumRequired: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isPremium by settingsStore.isPremium.collectAsState(initial = false)
    val startOfMonth by settingsStore.startOfMonth.collectAsState(initial = 1)

    // --- Choice Dialog States ---
    var showExportActionDialog by remember { mutableStateOf(false) }
    var pendingExportRequest by remember { mutableStateOf<ExportRequest?>(null) }

    // --- State for Custom Date Range ---
    var showCustomRangePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var selectedCustomRange by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    // --- State for Category Report ---
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // --- Save Launchers ---
    val customPdfSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            expenseViewModel.exportReport(
                type = ExportType.CUSTOM,
                uri = it,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                customRange = selectedCustomRange
            )
        }
    }

    val categoryPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            expenseViewModel.exportReport(
                type = ExportType.CATEGORY,
                uri = it,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                category = selectedCategory
            )
        }
    }

    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("Reports")
    }

    val backupState by expenseViewModel.backupRestoreState.collectAsState()
    var pendingShareFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(backupState) {
        if (backupState is BackupRestoreState.Success && pendingShareFile != null) {

            pendingShareFile?.let { file ->

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = when (file.extension) {
                        "csv" -> "text/csv"
                        "json" -> "application/json"
                        else -> "application/pdf"
                    }

                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(intent, "Share Report"))

                pendingShareFile = null
                expenseViewModel.resetBackupRestoreState()
            }
        }
    }

    Scaffold(
        topBar = {
            AppHeaderBar(title = "Reports & Export", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionHeader("Insights")

            ReportItem(
                icon = Icons.Default.Assessment,
                title = "Monthly Spending Report",
                subtitle = "Comprehensive summary of your monthly cycle",
                onClick = {
                    pendingExportRequest = ExportRequest.MONTHLY
                    showExportActionDialog = true
                }
            )

            ReportItem(
                icon = Icons.Default.BarChart,
                title = "Category Breakdown",
                subtitle = "Export a report for a specific category",
                locked = !isPremium,
                onClick = {
                    if (!isPremium) onPremiumRequired()
                    else showCategoryDialog = true
                }
            )

            Spacer(Modifier.height(8.dp))
            SectionHeader("Data Export")

            ReportItem(
                icon = Icons.Default.Code,
                title = "Export as JSON",
                subtitle = "Raw data for developers and power users",
                onClick = {
                    pendingExportRequest = ExportRequest.JSON
                    showExportActionDialog = true
                }
            )

            ReportItem(
                icon = Icons.Default.PictureAsPdf,
                title = "Export as PDF",
                subtitle = "Clean, printable document of your records",
                onClick = {
                    pendingExportRequest = ExportRequest.PDF
                    showExportActionDialog = true
                }
            )

            ReportItem(
                icon = Icons.Default.FileDownload,
                title = "Export as CSV (Excel)",
                subtitle = "Open your data in Google Sheets or Excel",
                locked = !isPremium,
                onClick = {
                    if (!isPremium) onPremiumRequired()
                    else {
                        pendingExportRequest = ExportRequest.CSV
                        showExportActionDialog = true
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
            SectionHeader("Advanced Control")

            ReportItem(
                icon = Icons.Default.Tune,
                title = "Custom Date Range",
                subtitle = "Generate reports for any specific period",
                locked = !isPremium,
                onClick = {
                    if (!isPremium) onPremiumRequired()
                    else showCustomRangePicker = true
                }
            )

            Spacer(Modifier.height(40.dp))
        }
    }

    // --- Action Dialog (Share or Save) ---
    if (showExportActionDialog) {
        ExportActionDialog(
            onDismiss = { showExportActionDialog = false },
            onSave = {
                showExportActionDialog = false
                when (pendingExportRequest) {
                    ExportRequest.MONTHLY -> onMonthlyReport()
                    ExportRequest.JSON -> onExportJson()
                    ExportRequest.PDF -> onExportPdf()
                    ExportRequest.CSV -> onExportCsv()
                    ExportRequest.CATEGORY -> categoryPdfLauncher.launch("${selectedCategory?.name?.lowercase()}_report.pdf")
                    ExportRequest.CUSTOM -> customPdfSaveLauncher.launch("xpenso_custom_report_${System.currentTimeMillis()}.pdf")
                    else -> {}
                }
            },
                onShare = {
                    showExportActionDialog = false
                    pendingExportRequest?.let {
                        pendingShareFile = shareReport(
                            it,
                            context,
                            expenseViewModel,
                            currencySymbol,
                            selectedCategory,
                            selectedCustomRange,
                            startOfMonth
                        )
                    } // ⭐ Calls the helper above to share the report
            }
        )
    }

    // --- Category Dialog ---
    if (showCategoryDialog) {
        CategoryReportDialog(
            categories = ExpenseCategory.entries,
            onDismiss = { showCategoryDialog = false },
            onCategorySelected = { category ->
                showCategoryDialog = false
                val hasCategoryData = expenseViewModel.allExpenses.value.any { ExpenseCategory.detect(it.title) == category }
                if (hasCategoryData) {
                    selectedCategory = category
                    pendingExportRequest = ExportRequest.CATEGORY
                    showExportActionDialog = true
                } else {
                    Toast.makeText(context, "No transactions recorded for ${category.name.lowercase().replace("_"," ")}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // --- Custom Range Dialog ---
    if (showCustomRangePicker) {
        CustomRangeDialog(
            startDate = startDate, endDate = endDate,
            onStartClick = { showStartDatePicker = true },
            onEndClick = { showEndDatePicker = true },
            onApply = {
                if (startDate != null && endDate != null) {
                    val hasDataInRange = expenseViewModel.allExpenses.value.any { it.timestamp in startDate!!..endDate!! }
                    if (hasDataInRange) {
                        selectedCustomRange = startDate!! to endDate!!
                        showCustomRangePicker = false
                        pendingExportRequest = ExportRequest.CUSTOM
                        showExportActionDialog = true
                    } else {
                        Toast.makeText(context, "No transactions in selected range", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showCustomRangePicker = false }
        )
    }

    // --- Sub Date Pickers ---
    if (showStartDatePicker) {
        StartDatePickerDialog(
            onDateSelected = {
                startDate = it
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        EndDatePickerDialog(
            onDateSelected = {
                endDate = it
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
fun ReportItem(icon: ImageVector,
               title: String,
               subtitle: String,
               locked: Boolean = false,
               onClick: () -> Unit
){
    val haptic = LocalHapticFeedback.current // ✅ ADD THIS
    Card(
        onClick = {
            if (locked) {
                // 🔥 PREMIUM FEEDBACK
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick() // still call (to show premium sheet)
        },

        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor =
            if (locked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
         )
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (locked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null,
                    modifier = Modifier.size(24.dp),
                    tint = if (locked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary) }
            }

            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (locked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface)
                Text(subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp)
            }
            if (!locked) Icon(Icons.Default.ChevronRight,
                null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )

        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
}

private fun shareReport(
    request: ExportRequest,
    context: Context,
    expenseViewModel: ExpenseViewModel,
    currencySymbol: String,
    selectedCategory: ExpenseCategory?,
    selectedCustomRange: Pair<Long, Long>?,
    startOfMonth: Int
): File? {

    val cacheDir = context.cacheDir
    val file: File

    when (request) {

        ExportRequest.JSON -> {
            file = File(cacheDir, "xpenso_backup.json")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.JSON,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol
            )
        }

        ExportRequest.PDF -> {
            file = File(cacheDir, "xpenso_report.pdf")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.PDF,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol
            )
        }

        ExportRequest.CSV -> {
            file = File(cacheDir, "xpenso_report.csv")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.CSV,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol
            )
        }

        ExportRequest.MONTHLY -> {
            file = File(cacheDir, "xpenso_monthly_report.pdf")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.MONTHLY,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol
            )
        }

        ExportRequest.CATEGORY -> {
            if (selectedCategory == null) return null

            file = File(cacheDir, "xpenso_category_report.pdf")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.CATEGORY,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                category = selectedCategory
            )
        }

        ExportRequest.CUSTOM -> {
            if (selectedCustomRange == null) return null

            file = File(cacheDir, "xpenso_custom_report.pdf")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.CUSTOM,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                customRange = selectedCustomRange
            )
        }
    }
    return file
}