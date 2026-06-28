package com.example.xpenso.Settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.NotificationsWork.ReminderScheduler
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xpenso.Viewmodel.BackupRestoreState
import com.example.xpenso.Viewmodel.ExpenseViewModel
import com.example.xpenso.PremiumHandler.PremiumFeatureSheet
import com.example.xpenso.PremiumHandler.PremiumUpgradeCard
import com.example.xpenso.SettingsStore
import com.example.xpenso.utils.AppTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    expenseViewModel: ExpenseViewModel,
    onNavigateToSecurity: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onChangeCurrency: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToReports: () -> Unit   // NEW
) {

    val viewModel: SettingsViewModel = viewModel()
    val isPremium by settingsStore.isPremium.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true}
    val animatedOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = tween(
            durationMillis = 140,
            easing = FastOutSlowInEasing
        ),
        label = "settingsOffset"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 120
        ),
        label = "settingsAlpha"
    )
    // DataStore states
    val themeMode by settingsStore.themeMode.collectAsState(initial = 0)
    val screenMode by settingsStore.screenMode.collectAsState(initial = 1)
    val colorStyle by settingsStore.colorStyle.collectAsState(initial = 0)
    val analyticsEnabled by settingsStore.analyticsEnabled.collectAsState(initial = true)
    val startOfMonth by settingsStore.startOfMonth.collectAsState(initial = 1)

    // Local UI states
    var showStartOfMonthDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    var showPremiumSheet by remember { mutableStateOf(false) }
    var premiumMessage by remember { mutableStateOf("") }

    val backupRestoreState by expenseViewModel.backupRestoreState.collectAsState()

    var isLoaded by rememberSaveable { mutableStateOf(false) }

//    LaunchedEffect(Unit) {
//        if (!isLoaded) {
//            kotlinx.coroutines.delay(300) // only first time
//            isLoaded = true
//        }
//    }

//    val isNotificationEnabled =
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            NotificationManagerCompat.from(context).areNotificationsEnabled()
//        }


//    val reminderEnabled by settingsStore.reminderEnabled.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("Settings")
    }
    LaunchedEffect(Unit) {
        when (val state = backupRestoreState) {
            is BackupRestoreState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                expenseViewModel.resetBackupRestoreState()
            }

            is BackupRestoreState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                expenseViewModel.resetBackupRestoreState()
            }

            else -> {}
        }
    }

    var showContent by remember { mutableStateOf(true) }

//    if (!isLoaded) {
//        SettingsShimmer()
//    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                AppTopBar("Settings")
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
//                AnimatedVisibility(
//                    visible = true,
//                    enter = fadeIn(tween(0)) +
//                            scaleIn(
//                                animationSpec = tween(
//                                    durationMillis = 220,
//                                    easing = FastOutSlowInEasing
//                                ),
//                                initialScale = 0.94f
//                            )
//                ) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
//                            .graphicsLayer {
//                            translationY = animatedOffset
//                            alpha = animatedAlpha
//                        },
                    , state = viewModel.listState,
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 8.dp
                    )
                ) {
                    item {

                        PremiumUpgradeCard(
                            isPremium = isPremium,
                            onClick = onNavigateToPremium
                        )

//                SettingsSection(title = "Xpenso Premium") {
//                    SettingsItem(
//                        icon = Icons.Default.WorkspacePremium,
//                        title = "Upgrade to Premium",
//                        subtitle = "Cloud backup, sync across devices and more",
//                        onClick = {
//                            // open premium screen
//                        }
//                    )
//                }

                        // --- SECTION: APPEARANCE ---
                        SettingsSection(title = "Appearance") {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Theme",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    listOf(
                                        "System",
                                        "Light",
                                        "Dark"
                                    ).forEachIndexed { index, label ->
                                        SegmentedButton(
                                            selected = themeMode == index,
                                            onClick = {
                                                scope.launch {
                                                    settingsStore.saveThemeMode(
                                                        index
                                                    )
                                                }
                                            },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = 3
                                            ),
                                            colors = SegmentedButtonDefaults.colors(
                                                activeContainerColor = MaterialTheme.colorScheme.primary.copy(
                                                    0.8f
                                                ),
                                                activeContentColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    .9f
                                                )
                                            )
                                        ) {
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    text = "Screen Mode",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    listOf(
                                        "Standard",
                                        "Full Screen",
                                        "Immersive"
                                    ).forEachIndexed { index, label ->
                                        SegmentedButton(
                                            selected = screenMode == index,
                                            onClick = {
                                                scope.launch {
                                                    settingsStore.saveScreenMode(
                                                        index
                                                    )
                                                }
                                            },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = 3
                                            ),
                                            colors = SegmentedButtonDefaults.colors(
                                                activeContainerColor = MaterialTheme.colorScheme.primary.copy(
                                                    0.8f
                                                ),
                                                activeContentColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    0.9f
                                                )
                                            )
                                        ) {
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                ColorStyleSetting(
                                    colorStyle = colorStyle,
                                    settingsStore = settingsStore,
                                    scope = scope
                                )
                            }
                            // ... existing Screen Mode SingleChoiceSegmentedButtonRow ends here ...

                        }


                        // --- SECTION: SECURITY ---
                        SettingsSection(title = "Security") {
                            SettingsItem(
                                icon = Icons.Default.Lock,
                                title = "Authentication",
                                subtitle = "Manage PIN and Biometric lock",
                                onClick = onNavigateToSecurity
                            )
                        }

                        // --- SECTION: PREFERENCES ---
                        SettingsSection(title = "Preferences") {
                            SettingsItem(
                                icon = Icons.Default.CurrencyExchange,
                                title = "Currency Symbol",
                                subtitle = "Change how prices are displayed",
                                onClick = onChangeCurrency
                            )
                            SettingsItem(
                                icon = Icons.Default.CalendarToday,
                                title = "Start of Month",
                                subtitle = "Day $startOfMonth",
                                onClick = { showStartOfMonthDialog = true }
                            )
                        }
//
//                        // --- SECTION: NOTIFICATIONS ---
                        SettingsSection(title = "Notifications") {

                            val intent = Intent().apply {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    action =
                                        android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    putExtra(
                                        android.provider.Settings.EXTRA_APP_PACKAGE,
                                        context.packageName
                                    )
                                } else {
                                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                                    putExtra("app_package", context.packageName)
                                    putExtra("app_uid", context.applicationInfo.uid)
                                }
                            }

                            // Check System Notification Permission
                            val isNotificationEnabled =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    checkSelfPermission(
                                        context,
                                        POST_NOTIFICATIONS
                                    ) == PERMISSION_GRANTED
                                } else {
                                    NotificationManagerCompat.from(context)
                                        .areNotificationsEnabled()
                                }

                            LaunchedEffect(isNotificationEnabled) {
                                if (isNotificationEnabled) {
                                    ReminderScheduler.schedule(context)
                                } else {
                                    ReminderScheduler.cancel(context)
                                }
                            }

//                            if (!isNotificationEnabled) {
//                                // CASE A: Notifications OFF
//                                SettingsItem(
//                                    icon = Icons.Default.NotificationsOff,
//                                    title = "Enable Daily Reminder",
//                                    subtitle = "Notifications disabled",
//                                    onClick = {
//                                        context.startActivity(intent)
//                                    }
//                                )
//                            } else {
//
//                                // CASE B: Notifications ON
//                                SettingsItem(
//                                    icon = Icons.Default.Notifications,
//                                    title = "Daily Reminder Enabled",
//                                    subtitle = "Tap to manage notification settings",
//                                    onClick = {
//                                        context.startActivity(intent)
//                                    }
//                                )
//                            }
                            val title =
                                if (isNotificationEnabled)
                                    "Reminders Active"
                                else
                                    "Enable Reminders"

                             val subtitle =
                                if (isNotificationEnabled)
                                    "One reminder every 2–3 days"
                                else
                                    "Get an occasional reminder"

                            val icon =
                                if (isNotificationEnabled)
                                    Icons.Default.Notifications
                                else
                                    Icons.Default.NotificationsOff

                            SettingsItem(
                                icon = icon,
                                title = title,
                                subtitle = subtitle,
                                onClick = {
                                    context.startActivity(intent)
                                }
                            )
                        }
                        // --- SECTION: DATA MANAGEMENT ---
                        SettingsSection(title = "Data Management") {
                            SettingsItem(
                                icon = Icons.Default.CloudUpload,
                                title = "Backup Data",
                                subtitle = "Save a backup file to your device",
                                onClick = {
                                    onNavigateToReports()
//                            scope.launch {
//
//                                val hasData = expenseViewModel.hasAnyData()
//
//                                if (hasData) {
//                                    showBackupDialog = true
//                                } else {
//                                    Toast.makeText(
//                                        context,
//                                        "No data available to backup",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
                                }
                            )
                            SettingsItem(
                                icon = Icons.Default.CloudDownload,
                                title = "Restore Data",
                                subtitle = "Import data from a backup file",
                                onClick = { showRestoreDialog = true }
                            )
                        }

                        // --- SECTION: PRIVACY ---
                        SettingsSection(title = "Privacy") {
                            SettingsSwitchItem(
                                icon = Icons.Default.Insights,
                                title = "Usage Statistics",
                                subtitle = "Send anonymous client data",
                                checked = analyticsEnabled,
                                onCheckedChange = { enabled -> // ✅ Using explicit name instead of 'it'
                                    scope.launch {
                                        settingsStore.saveAnalyticsEnabled(enabled)
                                    }
                                }
                            )
                        }

                        // --- SECTION: GENERAL ---
                        SettingsSection(title = "General") {
                            SettingsItem(
                                icon = Icons.Default.Info,
                                title = "About Xpenso",
                                subtitle = "Version 1.0.0 (Build 1)",
                                onClick = onNavigateToAbout
                            )
                            SettingsItem(
                                icon = Icons.Default.Star,
                                title = "Rate us on Play Store",
                                subtitle = "Your feedback helps us improve",
                                onClick = {
                                    val packageName = context.packageName
                                    try {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("market://details?id=$packageName")
                                            )
                                        )
                                    } catch (e: ActivityNotFoundException) {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                            )
                                        )
                                    }
                                }
                            )
                        }

                        // --- SECTION: DANGER ZONE ---
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "Danger Zone",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            )
                        ) {
                            SettingsItem(
                                icon = Icons.Default.DeleteForever,
                                title = "Clear All Data",
                                subtitle = "Reset all settings and delete expenses",
                                textColor = Color.Red,
                                onClick = { showClearDataDialog = true }
                            )
                        }


                    }
                }
                // --- DIALOGS ---

                DataManagementDialogs(
                    expenseViewModel = expenseViewModel,
//                    isPremium = isPremium,
//                    onNavigateToPremium = {
//                        premiumMessage = "PDF export is available in Xpenso Premium"
//                        showPremiumSheet = true
//                    },
//                    showBackup = showBackupDialog,
//                    onDismissBackup = { showBackupDialog = false },
                    showRestore = showRestoreDialog,
                    onDismissRestore = { showRestoreDialog = false }
                )

                if (showStartOfMonthDialog) {

                    AlertDialog(
                        onDismissRequest = { showStartOfMonthDialog = false },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = MaterialTheme.colorScheme.surface,

                        title = {
                            Text(
                                text = "Start of Month",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },

                        text = {

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp),
                            ) {

                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                ) {

                                    (1..28).forEach { day ->

                                        val isSelected = day == startOfMonth

                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = "Day $day",
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            },

                                            leadingContent = {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            },

                                            colors = ListItemDefaults.colors(
                                                containerColor =
                                                    if (isSelected)
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                    else
                                                        Color.Transparent
                                            ),

                                            modifier = Modifier.clickable {
                                                scope.launch {
                                                    settingsStore.saveStartOfMonth(day)
                                                    showStartOfMonthDialog = false
                                                }
                                            }
                                        )

                                        if (day != 28) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(start = 72.dp),
                                                thickness = 0.9.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }
                            }
                        },

                        confirmButton = {
                            TextButton(onClick = { showStartOfMonthDialog = false }) {
                                Text(
                                    "Cancel",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }

                if (showClearDataDialog) {
                    ClearDataDialog(
                        onConfirm = {
                            scope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                settingsStore.clearAll()
                                try {
                                    expenseViewModel.deleteAllData()

                                    Toast.makeText(
                                        context,
                                        "Data deleted Successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } catch (e: Exception) {

                                    Toast.makeText(
                                        context,
                                        e.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        onDismiss = { showClearDataDialog = false }
                    )
                }
                if (showPremiumSheet) {
                    PremiumFeatureSheet(
                        message = premiumMessage,
                        onDismiss = { showPremiumSheet = false },
                        onUpgradeClick = {
                            showPremiumSheet = false
                            onNavigateToPremium()
                        }
                    )
                }
            }
        }
}