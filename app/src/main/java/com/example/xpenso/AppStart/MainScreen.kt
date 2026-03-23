package com.example.xpenso.AppStart

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.xpenso.Home.NotificationScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xpenso.utils.BottomTab
import com.example.xpenso.ExpenseViewmodel.BackupRestoreState
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.ExpenseViewmodel.ExportType
import com.example.xpenso.NotificationsWork.RequestNotificationPermissionOnce
import com.example.xpenso.OnBoarding.OnboardingScreen
import com.example.xpenso.PremiumHandler.PremiumFeatureSheet
import com.example.xpenso.PremiumHandler.PremiumScreen
import com.example.xpenso.Reports.ReportsScreen
import com.example.xpenso.Settings.AboutScreen
import com.example.xpenso.Settings.SecurityScreen
import com.example.xpenso.Settings.TermsScreen
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.TransactionFilter
import com.example.xpesno.AppStart.AppScaffold
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val startOfMonth by settingsStore.startOfMonth.collectAsState(initial = 1)

    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    // --- Launchers ---
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
                    currencySymbol = currencySymbol
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

    var showPremiumSheet by remember { mutableStateOf(false) }
    var premiumMessage by remember { mutableStateOf("") }
    var showReports by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showNotificationHistory by remember { mutableStateOf(startInNotificationScreen) }

    var initialTransactionFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var initialTransactionSearch by remember { mutableStateOf("") }
    var initialCustomStart by remember { mutableStateOf<Long?>(null) }
    var initialCustomEnd by remember { mutableStateOf<Long?>(null) }
    var isChangingCurrency by remember { mutableStateOf(false) }

    val isNotificationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
       checkSelfPermission(context, POST_NOTIFICATIONS) == PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    var isTransactionFromInsights by remember { mutableStateOf(false) }

    val notificationHistory by settingsStore.notificationHistory.collectAsState(initial = emptyList())
    var triggerPermissionRequest by remember { mutableStateOf(false) }
    if (triggerPermissionRequest) { RequestNotificationPermissionOnce(settingsStore = settingsStore) }

    // --- Back Handling ---
    BackHandler(enabled = showNotificationHistory) { showNotificationHistory = false }
    BackHandler(enabled = showTerms) { showTerms = false }
    BackHandler(enabled = showPremium) { showPremium = false }
    BackHandler(enabled = showReports) { showReports = false; selectedTab = BottomTab.SETTINGS }
    BackHandler(enabled = showAbout && !showTerms) { showAbout = false }
    BackHandler(enabled = showSecuritySettings) { showSecuritySettings = false; selectedTab = BottomTab.SETTINGS }
    BackHandler(enabled = isChangingCurrency) { isChangingCurrency = false }
    BackHandler(enabled = selectedTab != BottomTab.HOME && !showNotificationHistory && !showTerms && !showAbout && !showSecuritySettings && !isChangingCurrency && !showPremium && !showReports) {
        selectedTab = BottomTab.HOME
    }

    // --- Navigation Logic ---
    if (isChangingCurrency) {
        OnboardingScreen(settingsStore = settingsStore, onlyCurrency = true, onFinish = { symbol ->
            scope.launch { settingsStore.saveCurrencySymbol(symbol); isChangingCurrency = false }
        }, onBack = { isChangingCurrency = false })
    } else if (showNotificationHistory) {
        NotificationScreen(notifications = notificationHistory, isNotificationEnabled = isNotificationEnabled, onBack = { showNotificationHistory = false }, onEnableNotifications = {
            val intent = Intent().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName); putExtra("app_uid", context.applicationInfo.uid)
                }
            }
            context.startActivity(intent)
        })
    } else if (showSecuritySettings) {
        SecurityScreen(settingsStore = settingsStore, onBack = { showSecuritySettings = false } , onPinCreated = onPinCreated)
    } else if (showTerms) {
        TermsScreen(onBack = { showTerms = false })
    } else if (showAbout) {
        AboutScreen(onBack = { showAbout = false }, onNavigateToTerms = { showTerms = true })
    } else if (showPremium) {
        PremiumScreen(settingsStore = settingsStore, onBack = { showPremium = false })
    } else if (showReports) {
        ReportsScreen(
            currencySymbol = currencySymbol,
            settingsStore = settingsStore,
            expenseViewModel = expenseViewModel,

                onMonthlyReport = {
                    scope.launch {
                        if (expenseViewModel.hasAnyData()) createMonthlyPdfLauncher.launch("Xpenso_Monthly_Report_${System.currentTimeMillis()}.pdf")
                        else Toast.makeText(
                            context,
                            "No transactions available to export",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },

            onExportPdf = {
                scope.launch {
                    if (expenseViewModel.hasAnyData()) createPdfLauncher.launch("Xpenso_report_${System.currentTimeMillis()}.pdf")
                    else Toast.makeText(context, "No transactions available to export", Toast.LENGTH_SHORT).show()
                }
            },

            onExportCsv = {
                scope.launch {
                    if (expenseViewModel.hasAnyData()) createCsvLauncher.launch("Xpenso_excel_report_${System.currentTimeMillis()}.csv")
                    else Toast.makeText(context, "No transactions available to export", Toast.LENGTH_SHORT).show()
                }
            },

            onExportJson = {
                scope.launch {
                    if (expenseViewModel.hasAnyData()) createJsonBackupLauncher.launch("Xpenso_backup_${System.currentTimeMillis()}.json")
                    else Toast.makeText(context, "No transactions available to export", Toast.LENGTH_SHORT).show()
                }
            },

            onPremiumRequired = { premiumMessage = "PDF, CSV exports and Advanced Reports are available in Xpenso Premium"; showPremiumSheet = true },
            onBack = { showReports = false; selectedTab = BottomTab.SETTINGS },
        )
    } else {
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
            currencySymbol = currencySymbol,
            settingsStore = settingsStore,
            expenseViewModel = expenseViewModel,
            onNavigateToNotifications = { showNotificationHistory = true },
            onNavigateToPremium = { showPremium = true },
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
            isTransactionFromInsights = isTransactionFromInsights
        )
    }

    if (showPremiumSheet) {
        PremiumFeatureSheet(
            message = premiumMessage,
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = { showPremiumSheet = false; showPremium = true }
        )
    }
}
