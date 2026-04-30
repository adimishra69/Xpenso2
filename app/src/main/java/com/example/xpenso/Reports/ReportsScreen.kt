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
import com.example.xpenso.Transaction.CustomDatePickerDialog
import com.example.xpenso.utils.AppHeaderBar
import android.content.Intent
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.FileProvider
import com.example.xpenso.AddExpenses.CategoryBottomSheet
import com.example.xpenso.Reports.YearPickerDialog
import com.example.xpenso.Reports.ExportActionDialog
import com.example.xpenso.ExpenseViewmodel.BackupRestoreState
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.ExpenseViewmodel.ExportType
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.PremiumDatePickerDialog
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

private enum class ExportRequest {
    MONTHLY, CATEGORY, YEARLY, JSON, PDF, CSV, CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    currencySymbol: String,
    settingsStore: SettingsStore,
    expenseViewModel: ExpenseViewModel,
    onMonthlyReport: (Int, Int) -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onYearlyReport: (Int) -> Unit,
    onCategoryReport: (ExpenseCategory?, Int, Int) -> Unit,
    onPremiumRequired: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isPremium by settingsStore.isPremium.collectAsState(initial = false)
    val startOfMonth by settingsStore.startOfMonth.collectAsState(initial = 1)
    val scope = rememberCoroutineScope()
    var selectedCategoryYear by remember {
        mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    }

    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedCategoryMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
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

    var showYearPicker by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    var exportMonth by remember { mutableStateOf<Int?>(null) }
    var exportYear by remember { mutableStateOf<Int?>(null) }
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
                category = selectedCategory,
                month = selectedCategoryMonth
            )
        }
    }

    val yearlyPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            expenseViewModel.exportReport(
                type = ExportType.YEARLY,
                uri = it,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                year = selectedYear
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
                    showMonthPicker = true
                }
            )


            ReportItem(
                icon = Icons.Default.CalendarMonth,
                title = "Yearly Financial Report",
                subtitle = "Full year spending insights and analytics",
               locked = !isPremium,
                onClick = {
                     if (!isPremium) onPremiumRequired()
                    else {
                        showYearPicker = true
                     }
                }
            )
            ReportItem(
                icon = Icons.Default.BarChart,
                title = "Monthly Category Breakdown",
                subtitle = "Export a report for a specific category",
                onClick = {
                    pendingExportRequest = ExportRequest.CATEGORY
                    showCategoryDialog = true
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
                locked = !isPremium,
                onClick = {
                    if (!isPremium) onPremiumRequired()
                    else {
                        pendingExportRequest = ExportRequest.PDF
                        showExportActionDialog = true
                    }
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
                    ExportRequest.MONTHLY ->
                        onMonthlyReport(selectedCategoryMonth, selectedCategoryYear)
                    ExportRequest.JSON -> onExportJson()
                    ExportRequest.PDF -> onExportPdf()
                    ExportRequest.CSV -> onExportCsv()
                    ExportRequest.CATEGORY ->
                        onCategoryReport(selectedCategory, selectedCategoryMonth, selectedCategoryYear)
                    ExportRequest.CUSTOM -> customPdfSaveLauncher.launch("xpenso_custom_report_${System.currentTimeMillis()}.pdf")
                    ExportRequest.YEARLY ->
                        onYearlyReport(selectedYear)
                    else -> {}
                }
            },
            onShare = {
                showExportActionDialog = false

                pendingExportRequest?.let { request ->

                    scope.launch {

                        val hasData: Boolean = when (request) {

                            ExportRequest.MONTHLY ->
                                expenseViewModel.hasDataForExport(
                                    type = ExportType.MONTHLY,
                                    month = selectedCategoryMonth,
                                    year = selectedCategoryYear
                                )

                            ExportRequest.YEARLY ->
                                expenseViewModel.hasDataForExport(
                                    type = ExportType.YEARLY,
                                    year = selectedYear
                                )

                            ExportRequest.CATEGORY ->
                                expenseViewModel.hasDataForExport(
                                    type = ExportType.CATEGORY,
                                    category = selectedCategory,
                                    month = selectedCategoryMonth,
                                    year = selectedCategoryYear
                                )

                            else -> expenseViewModel.hasAnyData()
                        }

                        if (hasData) {
                            pendingShareFile = shareReport(
                                request,
                                context,
                                expenseViewModel,
                                currencySymbol,
                                selectedCategory,
                                selectedCustomRange,
                                startOfMonth,
                                selectedYear,
                                selectedCategoryMonth,
                                selectedCategoryYear
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "No transactions found for selected period", // 👈 SAME AS SAVE
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )
    }

    // --- Category Dialog ---
    if (showCategoryDialog) {
        CategoryBottomSheet(
            currentSelection = selectedCategory,
            isVertical = true,
            onSelect = { category ->

                showCategoryDialog = false

                val hasCategoryData = expenseViewModel.allExpenses.value
                    .any {
                        (ExpenseCategory.entries
                            .find { cat -> cat.name == it.category }
                            ?: ExpenseCategory.OTHER
                                ) == category
                    }

                if (hasCategoryData) {
                    selectedCategory = category
                    showMonthPicker = true
                } else {
                    Toast.makeText(
                        context,
                        "No transactions recorded for ${
                            category.name.lowercase().replace("_"," ")
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = selectedCategoryMonth,
            selectedYear = selectedCategoryYear,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month, year ->

                selectedCategoryMonth = month
                selectedCategoryYear = year

                showMonthPicker = false
                showExportActionDialog = true
            }
        )
    }


    if (showYearPicker) {
        YearPickerDialog(
            selectedYear  = selectedYear,
            onDismiss     = { showYearPicker = false },
            onSelected    = {  year ->
                selectedYear  = year
                showYearPicker = false
                pendingExportRequest = ExportRequest.YEARLY
                showExportActionDialog = true
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
                        Toast.makeText(context, "No transactions in selected period", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showCustomRangePicker = false }
        )
    }

    // --- Sub Date Pickers ---
    if (showStartDatePicker) {
        PremiumDatePickerDialog(
            initialSelectedMillis = startDate ?: System.currentTimeMillis(),
            onDateSelected = {
                startDate = it
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        PremiumDatePickerDialog(
            initialSelectedMillis = endDate ?: System.currentTimeMillis(),
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (locked) 0.dp else 2.dp,
        onClick = {
            if (locked) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (locked)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

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

fun fileSafeDateTime(
    millis: Long = System.currentTimeMillis()
): String {
    val format = java.text.SimpleDateFormat(
        "ddMMyy_HHmmss",
        java.util.Locale.getDefault()
    )

    return format.format(java.util.Date(millis))
}
private fun shareReport(
    request: ExportRequest,
    context: Context,
    expenseViewModel: ExpenseViewModel,
    currencySymbol: String,
    selectedCategory: ExpenseCategory?,
    selectedCustomRange: Pair<Long, Long>?,
    startOfMonth: Int,
    selectedYear: Int,
    selectedCategoryMonth: Int,
    selectedCategoryYear: Int
): File? {

    val cacheDir = context.cacheDir
    val file: File

    when (request) {

        ExportRequest.JSON -> {
            file = File(
                cacheDir,
                "Xpenso_Backup_${fileSafeDateTime()}.json"
            )
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.JSON,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol
            )
        }

        ExportRequest.PDF -> {
            file = File(cacheDir, "Xpenso_Report_${fileSafeDateTime()}.pdf")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.PDF,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol
            )
        }

        ExportRequest.CSV -> {
            file = File(cacheDir, "Xpenso_Transactions_${fileSafeDateTime()}.csv")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            expenseViewModel.exportReport(
                type = ExportType.CSV,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol
            )
        }

        ExportRequest.MONTHLY -> {
            file = File(cacheDir, "xpenso_monthly_report_${
                fileSafeDateTime()}.pdf")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            expenseViewModel.exportReport(
                type = ExportType.MONTHLY,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                month = selectedCategoryMonth,
                year = selectedCategoryYear
            )
        }

        ExportRequest.YEARLY -> {
            file = File(cacheDir, "xpenso_yearly_report${
                fileSafeDateTime()}.pdf")

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            expenseViewModel.exportReport(
                type = ExportType.YEARLY,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                year = selectedYear,
            )
        }

        ExportRequest.CATEGORY -> {
            if (selectedCategory == null) return null

            file = File(cacheDir, "xpenso_category_report${
                fileSafeDateTime()}.pdf")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            expenseViewModel.exportReport(
                type = ExportType.CATEGORY,
                uri = uri,
                startOfMonth = startOfMonth,
                currencySymbol = currencySymbol,
                category = selectedCategory,
                year = selectedCategoryYear,
                month = selectedCategoryMonth
            )
        }

        ExportRequest.CUSTOM -> {
            if (selectedCustomRange == null) return null

            file = File(cacheDir, "xpenso_custom_report${
                fileSafeDateTime()}.pdf")
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
