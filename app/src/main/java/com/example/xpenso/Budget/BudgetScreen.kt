package com.example.xpenso.Budget

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.SettingsStore
import com.example.xpenso.ui.theme.LocalBudgetColors
import com.example.xpenso.utils.AppTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun BudgetScreen(
    currencySymbol: String,
    settingsStore: SettingsStore,
    expenseViewModel: ExpenseViewModel
) {

    val scope = rememberCoroutineScope()

    val isInitialLoadComplete by expenseViewModel.isInitialLoadComplete.collectAsState()

    var viewMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonth = viewMonth.get(Calendar.MONTH)
    val currentYear = viewMonth.get(Calendar.YEAR)


    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("Budgets")
    }

    // --- PASTE THE androidx.compose.runtime.LaunchedEffect HERE ---
    LaunchedEffect(Unit) {
        expenseViewModel.updateSelectedMonthAndYear(currentMonth, currentYear)
    }

    val currentStartDay by settingsStore.startOfMonth.collectAsState(initial = 1) // Collect the actual setting

    LaunchedEffect(currentStartDay) {
        // tell the ViewModel to update its internal _startDay state.
        expenseViewModel.updateStartDay(currentStartDay)
    }
    // --- END PASTE ---
    val budget by settingsStore.getMonthlyBudget(currentMonth, currentYear)
        .collectAsState(initial = 0.0)

    val spentInSelectedMonth by expenseViewModel.currentMonthSpent.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    val selectedMonthYearDisplay = remember(viewMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(viewMonth.time)
    }

    val remaining = (budget - spentInSelectedMonth).coerceAtLeast(0.0)
    val usagePercentage = if (budget > 0) (spentInSelectedMonth / budget).toFloat() else 0f

    val displayPercentage = (usagePercentage * 100).coerceAtMost(100f).toInt()

    val percentageText = when {
        budget <= 0 -> ""
        usagePercentage > 0f && usagePercentage <= 0.05f -> ""
        spentInSelectedMonth == 0.0 -> "No spending yet"
        usagePercentage >= 1f -> "100% used"
        else -> "$displayPercentage% used"
    }

    val budgetStatusText = when {
        budget <= 0 -> "No budget set for this month"
        budget > 0 && usagePercentage == 0f -> "No spending yet — great start!"
        usagePercentage > 0f && usagePercentage <= 0.05f -> "Just getting started — all good so far"
        usagePercentage < 0.60f -> "Safe spending"
        usagePercentage <= 0.80f -> "On track — stay mindful"
        usagePercentage < 1f -> "Near limit"
        else -> "Budget limit reached"
    }

    // 1. Get your theme colors at the top of your BudgetScreen function
    val customColors = LocalBudgetColors.current

// 2. Update the status color logic
    val budgetStatusColor = when {
        budget <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        usagePercentage == 0f -> customColors.blueTitle
        usagePercentage <= 0.05f -> customColors.blueTitle
        usagePercentage < 0.60f -> customColors.greenTitle
        usagePercentage < 0.80f -> customColors.yellowTitle // <--- USES THEME AUTOMATICALLY
        usagePercentage < 1f -> customColors.orangeTitle
        else -> Color(0xFFF44336)
    }


    // ---------- TARGET COLOR ----------
    val targetProgressColor = when {
        budget <= 0 || spentInSelectedMonth <= 0.0 ->
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

        usagePercentage <= 0.05f -> Color(0xFF4A90E2)
        usagePercentage < 0.60f -> Color(0xFF4CAF50)
        usagePercentage < 0.80f -> Color(0xFFFFC107)
        usagePercentage < 1f -> Color(0xFFF57C00)
        else -> Color(0xFFF44336)
    }

// ---------- ANIMATED COLOR (same variable name as before) ----------
    val progressColor by animateColorAsState(
        targetValue = targetProgressColor,
        animationSpec = tween(durationMillis = 800),
        label = "ProgressColorAnim"
    )


    // ================= SCREEN =================
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppTopBar("Budgets")
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {

            var showContent by remember { mutableStateOf(false) }

            LaunchedEffect(isInitialLoadComplete) {
                if (isInitialLoadComplete) {
                    showContent = true
                }
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) +
                        scaleIn(
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            ),
                            initialScale = 0.94f
                        )
            ) {

                BudgetContent(
                    currencySymbol = currencySymbol,
                    viewMonth = viewMonth,
                    selectedMonthYearDisplay = selectedMonthYearDisplay,
                    budget = budget,
                    spentInSelectedMonth = spentInSelectedMonth,
                    remaining = remaining,
                    usagePercentage = usagePercentage,
                    displayPercentage = displayPercentage,
                    percentageText = percentageText,
                    budgetStatusText = budgetStatusText,
                    budgetStatusColor = budgetStatusColor,
                    progressColor = progressColor,
                    showDialog = showDialog,
                    onPrevMonth = {
                        val newCal = viewMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        viewMonth = newCal
                        expenseViewModel.updateSelectedMonthAndYear(
                            newCal.get(Calendar.MONTH),
                            newCal.get(Calendar.YEAR)
                        )
                    },
                    onNextMonth = {
                        val newCal = viewMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        viewMonth = newCal
                        expenseViewModel.updateSelectedMonthAndYear(
                            newCal.get(Calendar.MONTH),
                            newCal.get(Calendar.YEAR)
                        )
                    },
                    onEditBudget = { showDialog = true },
                    onSetBudget = { showDialog = true },
                    expenseViewModel = expenseViewModel
                )
            }
        }
    }
    // ================= DIALOG =================

    if (showDialog) {
        BudgetDialog(
            initialValue = if (budget > 0) budget.toString() else "",
            isEditMode = budget > 0,
            onDismiss = { showDialog = false },
            onSave = { newBudget ->
                scope.launch {
                    settingsStore.saveMonthlyBudget(currentMonth, currentYear, newBudget)
                    showDialog = false
                }
            },
            onRemove = {
                scope.launch {
                    settingsStore.removeMonthlyBudget(currentMonth, currentYear)
                    showDialog = false
                }
            }
        )
    }
}
