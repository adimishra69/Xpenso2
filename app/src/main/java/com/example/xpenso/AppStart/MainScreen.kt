package com.example.xpenso.AppStart

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.utils.BottomTab
import com.example.xpenso.Viewmodel.BackupRestoreState
import com.example.xpenso.Viewmodel.BudgetViewModel
import com.example.xpenso.Viewmodel.ExpenseViewModel
import com.example.xpenso.Viewmodel.ExportType
import com.example.xpenso.Home.ExpenseDialog
import com.example.xpenso.OnBoarding.OnboardingScreen
import com.example.xpenso.PremiumHandler.PremiumActivationScreen
import com.example.xpenso.PremiumHandler.PremiumFeatureSheet
import com.example.xpenso.PremiumHandler.PremiumDetailsScreen
import com.example.xpenso.Reports.ReportsScreen
import com.example.xpenso.Settings.AboutScreen
import com.example.xpenso.Settings.SecurityScreen
import com.example.xpenso.Settings.TermsScreen
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.Transaction.TransactionFilter
import com.example.xpenso.utils.mergeDateWithCurrentTime
import kotlinx.coroutines.launch
import java.util.Calendar


@Composable
fun MainScreen(
    currencySymbol: String,
    settingsStore: SettingsStore,
    startInNotificationScreen: Boolean = false,
    onPinCreated: () -> Unit   // 👈 ADD THIS
) {


    val expenseViewModel: ExpenseViewModel = viewModel()
    val budgetViewModel: BudgetViewModel = viewModel()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val startOfMonth by settingsStore.startOfMonth.collectAsState(initial = 1)
    val selectedCurrencySymbol by settingsStore.currencySymbol.collectAsState(initial = "₹")
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.HOME) }

    var exportMonth by remember { mutableStateOf<Int?>(null) }
    var exportYear by remember { mutableStateOf<Int?>(null) }
    var showPremiumDetails by rememberSaveable { mutableStateOf(false) }
    var exportYearOnly by remember { mutableStateOf<Int?>(null) }
    var exportCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var showPremiumActivation by rememberSaveable { mutableStateOf(false) }
    // --- Launchers ---

    val text  = "No transactions found for selected period"
    val createJsonBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                expenseViewModel.exportReport(
                    type = ExportType.JSON,
                    uri = it,
                    startOfMonth = startOfMonth,
                    currencySymbol = currencySymbol
                )
            }
        }

    val createPdfLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri?.let {
                expenseViewModel.exportReport(
                    type = ExportType.PDF,
                    uri = it,
                    startOfMonth = startOfMonth,
                    currencySymbol = currencySymbol
                )
            }
        }

    val createCsvLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            uri?.let {
                expenseViewModel.exportReport(
                    type = ExportType.CSV,
                    uri = it,
                    startOfMonth = startOfMonth,
                    currencySymbol = currencySymbol
                )
            }
        }

    val createMonthlyPdfLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri?.let {
                expenseViewModel.exportReport(
                    type = ExportType.MONTHLY,
                    uri = it,
                    startOfMonth = startOfMonth,
                    currencySymbol = currencySymbol,
                    month = exportMonth,
                    year = exportYear
                )
            }
        }

    val createYearlyPdfLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri?.let {
                expenseViewModel.exportReport(
                    type = ExportType.YEARLY,
                    uri = it,
                    startOfMonth = startOfMonth,
                    currencySymbol = currencySymbol,
                    year = exportYearOnly
                )
            }
        }

    val createCategoryPdfLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri?.let {
                expenseViewModel.exportReport(
                    type = ExportType.CATEGORY,
                    uri = it,
                    startOfMonth = startOfMonth,
                    currencySymbol = currencySymbol,
                    category = exportCategory,
                    month = exportMonth,
                    year = exportYear
                )
            }
        }
    // --- State Management ---
    val backupState by expenseViewModel.backupRestoreState.collectAsState()
    LaunchedEffect(backupState) {
        when (backupState) {
            is BackupRestoreState.Success -> {
                Toast.makeText(context, (backupState as BackupRestoreState.Success).message, Toast.LENGTH_SHORT).show()
                expenseViewModel.resetBackupRestoreState()
            }
            is BackupRestoreState.Error -> {
                Toast.makeText(context, (backupState as BackupRestoreState.Error).message, Toast.LENGTH_SHORT).show()
                expenseViewModel.resetBackupRestoreState()
            }
            else -> {}
        }
    }

    val isPremium by settingsStore.isPremium.collectAsState(initial = false )//false)
    val selectedDateMillis by expenseViewModel.selectedDateMillis.collectAsState()
    var showPremiumSheet by rememberSaveable { mutableStateOf(false) }
    var premiumMessage by rememberSaveable { mutableStateOf("") }
    var showReports by rememberSaveable { mutableStateOf(false) }
    var showPremium by rememberSaveable { mutableStateOf(false) }
    var showSecuritySettings by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
//    var showYearlyBudget by rememberSaveable { mutableStateOf(false) }
    var showTerms by rememberSaveable { mutableStateOf(false) }
//    var showNotificationHistory by remember { mutableStateOf(startInNotificationScreen) }

    var showExpenseScreen by remember { mutableStateOf(false) }
    var initialTransactionFilter by rememberSaveable { mutableStateOf(TransactionFilter.ALL) }
    var initialTransactionSearch by rememberSaveable { mutableStateOf("") }
    var initialCustomStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var initialCustomEnd by rememberSaveable { mutableStateOf<Long?>(null) }
    var isChangingCurrency by rememberSaveable { mutableStateOf(false) }

    var showAddExpense by rememberSaveable { mutableStateOf(false) }
    var showEditExpense by rememberSaveable { mutableStateOf(false) }

    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var paymentMode by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }


    var isTransactionFromInsights by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showAddExpense) {
        showAddExpense = false
    }

    BackHandler(enabled = showEditExpense) {
        showEditExpense = false
    }

//    BackHandler(enabled = showYearlyBudget) {
//        showYearlyBudget = false
//        selectedTab = BottomTab.BUDGETS
//    }

    BackHandler(enabled = showTerms) { showTerms = false }

    BackHandler(enabled = showPremium) { showPremium = false }

    BackHandler(enabled = showReports) { showReports = false; selectedTab = BottomTab.SETTINGS }

    BackHandler(enabled = showAbout && !showTerms) { showAbout = false }

    BackHandler(enabled = showSecuritySettings) { showSecuritySettings = false ; selectedTab = BottomTab.SETTINGS }

    BackHandler(enabled = isChangingCurrency) { isChangingCurrency = false }

    val isDialogOpen =
                showTerms ||
                showAbout ||
                showSecuritySettings ||
                isChangingCurrency ||
                showPremium ||
                showReports||
                showAddExpense || showEditExpense

    BackHandler(enabled = selectedTab != BottomTab.HOME && !isDialogOpen &&!showExpenseScreen) {
        selectedTab = BottomTab.HOME
    }

    // --- Navigation Logic ---
    if (isChangingCurrency) {
        OnboardingScreen(settingsStore = settingsStore, onlyCurrency = true, onFinish = { symbol ->
            scope.launch { settingsStore.saveCurrencySymbol(symbol); isChangingCurrency = false }
        }, onBack = { isChangingCurrency = false })

    } else if (showSecuritySettings) {
        SecurityScreen(settingsStore = settingsStore, onBack = { showSecuritySettings = false } , onPinCreated = onPinCreated)
    } else if (showTerms) {
        TermsScreen(onBack = { showTerms = false })
    } else if (showAbout) {
        AboutScreen(onBack = { showAbout = false }, onNavigateToTerms = { showTerms = true })
    } else if (showPremiumActivation) {
        PremiumActivationScreen(
            settingsStore = settingsStore,
            onBack = { showPremiumActivation = false }
        )
    } else if (showPremiumDetails) {
        PremiumDetailsScreen(
            isPremium = isPremium,
            onBackClick = { showPremiumDetails = false },
            onUpgradeClick = {
                showPremiumActivation = true
            }
        )
    } else if (showReports) {
        ReportsScreen(
            currencySymbol = currencySymbol,
            settingsStore = settingsStore,
            expenseViewModel = expenseViewModel,

            onMonthlyReport = { month, year ->

                scope.launch {
                    if (expenseViewModel.hasDataForExport(
                            type = ExportType.MONTHLY,
                            month = month,
                            year = year
                    )
                        )
                    {

                        exportMonth = month
                        exportYear = year
                    createMonthlyPdfLauncher.launch(
                            "Xpenso_Monthly_Report_${System.currentTimeMillis()}.pdf"
                        )
                    } else {
                        Toast.makeText(
                            context,
                            text,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },

            onExportPdf = {
                scope.launch {
                    if (expenseViewModel.hasAnyData()) createPdfLauncher.launch("Xpenso_report_${System.currentTimeMillis()}.pdf")
                    else Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
            },

            onExportCsv = {
                scope.launch {
                    if (expenseViewModel.hasAnyData()) createCsvLauncher.launch("Xpenso_excel_report_${System.currentTimeMillis()}.csv")
                    else Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
            },

            onExportJson = {
                scope.launch {
                    if (expenseViewModel.hasAnyData()) createJsonBackupLauncher.launch("Xpenso_backup_${System.currentTimeMillis()}.json")
                    else Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
            },

            onYearlyReport = { year ->

                scope.launch {

                    if (expenseViewModel.hasDataForExport(
                            type = ExportType.YEARLY,
                            year = year
                        )
                    ) {

                        exportYearOnly = year

                        createYearlyPdfLauncher.launch(
                            "Xpenso_Yearly_Report_${System.currentTimeMillis()}.pdf"
                        )

                    } else {
                        Toast.makeText(
                            context,
                            text,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },

            onCategoryReport = { category, month, year ->

                scope.launch {

                    if (expenseViewModel.hasDataForExport(
                            type = ExportType.CATEGORY,
                            category = category,
                            month = month,
                            year = year
                        )
                    ) {

                        exportCategory = category
                        exportMonth = month
                        exportYear = year

                        createCategoryPdfLauncher.launch(
                            "${category?.name?.lowercase()}_report_${System.currentTimeMillis()}.pdf"
                        )

                    } else {
                        Toast.makeText(
                            context,
                            text,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },

            onPremiumRequired = { premiumMessage = "PDF, CSV exports and Advanced Reports are available in Xpenso Premium"; showPremiumSheet = true },
            onBack = { showReports = false; selectedTab = BottomTab.SETTINGS },
        )
    }
//    else if (showYearlyBudget) {
//
//            var selectedYear by remember {
//                mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
//            }
//
//            val yearlyInsights = remember(
//                selectedYear,
//                expenseViewModel.allExpenses.collectAsState().value
//            ) {
//                expenseViewModel.getYearlyInsights(selectedYear)
//            }
//
//            YearlyBudgetInsightsScreen(
//                insights = yearlyInsights,
//                currencySymbol = currencySymbol,
//                selectedYear = selectedYear,
//                onPrevYear = { selectedYear-- },
//                onNextYear = { selectedYear++ }
//            )
//        }

    else  if (showAddExpense) {

        ExpenseDialog(
            selectedDate = selectedDateMillis,
            title = "Add Expense",
            confirmText = "Add",
            name = name,
            amount = amount,
            paymentMode = paymentMode,
            onNameChange = { name = it },
            onAmountChange = { amount = it },
            onPaymentModeChange = {
               paymentMode = it
            },
            currencySymbol = selectedCurrencySymbol,
            initialCategory = null,
            onConfirm = { category , isNegative ->
                val selectedDate = selectedDateMillis
                val finalTimestamp = mergeDateWithCurrentTime(selectedDate)

                expenseViewModel.addExpense(
                    name,
                    kotlin.math.abs(amount.toDoubleOrNull() ?: 0.0),
                    currencySymbol,
                    finalTimestamp,
                    paymentMode,
                    category.name,
                    notes
                )
                showAddExpense = false

                scope.launch {

                    kotlinx.coroutines.delay(
                        if (isNegative) 1000 else 200
                    )

                    Toast.makeText(
                        context,
                        "Expense added",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDismiss = { showAddExpense = false },
            notes = notes,
            onNotesChange = { notes = it },
        )
    }
    else if (showEditExpense && selectedExpense != null) {

        ExpenseDialog(
            selectedDate = selectedDateMillis,
            title = "Edit Expense",
            confirmText = "Update",
            name = name,
            amount = amount,
            paymentMode = paymentMode,
            onNameChange = { name = it },
            onAmountChange = { amount = it },
            onPaymentModeChange = { paymentMode = it },
            currencySymbol = selectedCurrencySymbol,
            initialCategory =
                selectedExpense?.category
                    ?.let { ExpenseCategory.valueOf(it) },
            onConfirm = { category , isNegative,  ->
                expenseViewModel.updateExpense(
                    selectedExpense!!,
                    name,
                    amount,
                    paymentMode,
                    category.name,
                    notes

                )

                showEditExpense = false
                scope.launch {
                    kotlinx.coroutines.delay(
                        if (isNegative) 1000 else 200
                    )

                    Toast.makeText(
                        context,
                        "Expense updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            },

            onDismiss = { showEditExpense = false },
            notes = notes,
            onNotesChange = { notes = it },
            isEdit = true,
        )
    }
    else {
        // ⭐ Use the NEW AppScaffold here
        AppScaffold(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it

                if (it == BottomTab.TRANSACTIONS) {
                    isTransactionFromInsights = false   // 🔥 CRITICAL
                    initialTransactionFilter = TransactionFilter.ALL
                    initialTransactionSearch = ""
                    initialCustomStart = null
                    initialCustomEnd = null
                }
            },
//            onYearlyBudgetClick = {
//                showYearlyBudget = true
//            },
            currencySymbol = currencySymbol,
            settingsStore = settingsStore,
            expenseViewModel = expenseViewModel,
            budgetViewModel = budgetViewModel,
//            onNavigateToNotifications = { showNotificationHistory = true },
            onNavigateToPremium = { showPremiumDetails = true },
            DirectPremium = { showPremiumActivation  = true},
            onNavigateToSecurity = { showSecuritySettings = true },
            onNavigateToAbout = { showAbout = true },
            onChangeCurrency = { isChangingCurrency = true },
            onNavigateToReports = { showReports = true },
            initialTransactionFilter = initialTransactionFilter,
            initialTransactionSearch = initialTransactionSearch,
            initialCustomStart = initialCustomStart,
            initialCustomEnd = initialCustomEnd,
            onCategoryClick = { categoryName, start, end ->
                val today = Calendar.getInstance()
                val isCurrentMonth = today.timeInMillis in start..end
                initialTransactionSearch = categoryName
                if (isCurrentMonth) {
                    initialTransactionFilter = TransactionFilter.THIS_CYCLE
                } else {
                    initialTransactionFilter = TransactionFilter.CUSTOM
                    initialCustomStart = start
                    initialCustomEnd = end
                }
                isTransactionFromInsights = true
                selectedTab = BottomTab.TRANSACTIONS
            },
            isTransactionFromInsights = isTransactionFromInsights,
            onAddExpenseClick = {

                name = ""
                amount = ""
                paymentMode = ""
                notes = ""

                showAddExpense = true
            },

            onEditExpenseClick = { expense ->

                selectedExpense = expense
                name = expense.title
                amount = expense.amount.toString()
                paymentMode = expense.paymentMode
                notes = expense.notes ?: ""
                showEditExpense = true

            },

            onDeleteExpenseClick = { expense ->
                expenseViewModel.deleteExpense(expense)
            },
        )
    }

    if (showPremiumSheet) {
        PremiumFeatureSheet(
            message = premiumMessage,
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                showPremiumSheet = false
                showPremiumActivation = true
//                showPremiumDetails = true
            }

        )
    }
}
