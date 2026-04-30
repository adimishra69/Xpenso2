package com.example.xpenso.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.rememberLazyListState

import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.SettingsStore
import com.example.xpenso.utils.PremiumDatePickerDialog

//@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currencySymbol: String,
    settingsStore: SettingsStore,
    expenseViewModel: ExpenseViewModel = viewModel(),
    onAddExpense: () -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showMonthExpenses by remember { mutableStateOf(false) }

    var isNext by remember { mutableStateOf(true) }
    var  isManualCategory by remember { mutableStateOf(false) }
    val selectedDateMillis by expenseViewModel.selectedDateMillis.collectAsState()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    LaunchedEffect(selectedDateMillis) {
        datePickerState.selectedDateMillis = selectedDateMillis
    }

    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("Home")

        if (selectedDateMillis == 0L) {
            val today = System.currentTimeMillis()
            expenseViewModel.updateSelectedDate(today)
        }
    }

    val startOfMonthSetting by settingsStore.startOfMonth.collectAsState(initial = 1)

    var previewExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    LaunchedEffect(startOfMonthSetting) {
        expenseViewModel.updateStartDay(startOfMonthSetting)
    }

    val listState = rememberLazyListState()
    val screenAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450)
        )
    }

    val selectedDateDisplay = remember(selectedDateMillis) {
        val millis = selectedDateMillis
        val date = Date(millis)
        val fullMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
        val monthDisplay = if (fullMonth.length > 5) {
            SimpleDateFormat("MMM", Locale.getDefault()).format(date)
        } else {
            fullMonth
        }
        val day = SimpleDateFormat("dd", Locale.getDefault()).format(date)
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
        "$day $monthDisplay $year"
    }

    val filteredExpenses by expenseViewModel.filteredExpenses.collectAsState()
    val dailyTotal by expenseViewModel.dailyTotal.collectAsState()

    val allExpenses by expenseViewModel.allExpenses.collectAsState(emptyList())

    val monthlyExpenses = remember(allExpenses, selectedDateMillis, startOfMonthSetting) {
        val millis = selectedDateMillis
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val startCal = Calendar.getInstance().apply {
            set(year, month, startOfMonthSetting, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If selected date is BEFORE the start day of this month, the cycle actually started last month
        if (calendar.before(startCal)) {
            startCal.add(Calendar.MONTH, -1)
        }

        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        allExpenses.filter { it.timestamp in startCal.timeInMillis..endCal.timeInMillis }
    }
    val monthlyTotal = monthlyExpenses.sumOf { it.amount }

    val currentMonthSpent by expenseViewModel.currentMonthSpent.collectAsState()

    // --- FIX: Get the budget for the EFFECTIVE cycle month ---
    val budget = remember(selectedDateMillis, startOfMonthSetting, allExpenses) {

        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val startCal = Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), startOfMonthSetting, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (cal.before(startCal)) {
            startCal.add(Calendar.MONTH, -1)
        }
        settingsStore.getMonthlyBudget(
            month = startCal.get(Calendar.MONTH),
            year = startCal.get(Calendar.YEAR)
        )
    }.collectAsState(initial = 0.0).value

    // 🔥 FIXED: Removed the incorrect currencySymbol parameter
    val insightResult = getDailyBudgetInsight(
        dailyTotal = dailyTotal,
        monthlyTotal = currentMonthSpent,
        monthlyBudget = budget,
        selectedDateMillis = selectedDateMillis,
        startOfMonth = startOfMonthSetting,
        primary = MaterialTheme.colorScheme.primary,
        normal = MaterialTheme.colorScheme.onSurfaceVariant,
        danger = MaterialTheme.colorScheme.error
    )

    Scaffold(
        topBar = {
            HomeAppBar(
                isNext = isNext,
//                showDot = notificationCount > 0,
                selectedDateDisplay = selectedDateDisplay,
                onMonthSummaryClick = { showMonthExpenses = true },
                onDatePickerClick = { showDatePicker = true },
                onPreviousDay = {
                    isNext = false
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        add(Calendar.DAY_OF_MONTH, -1)
                    }
                    expenseViewModel.updateSelectedDate(cal.timeInMillis)
                },

                onNextDay = {
                    isNext = true
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                    expenseViewModel.updateSelectedDate(cal.timeInMillis)
                },
            )
        },

        floatingActionButton = {

            ScrollAwareFab(
                listState = listState,
                visibleCondition = filteredExpenses.isNotEmpty()
            ) {
                onAddExpense()
            }

        }
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 80.dp
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DailyExpenseCard(
                    currencySymbol = currencySymbol,
                    dailyTotal = dailyTotal
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                DailySmartHeader(
                    message = insightResult.message,
                    messageColor = insightResult.color,
                    transactionCount = filteredExpenses.size,
                    monthlyBudget = budget,
                    selectedDateMillis = selectedDateMillis,
                    startOfMonth = startOfMonthSetting
                )
            }

            if (filteredExpenses.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Today's expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(filteredExpenses, key = { it.id }) { expense ->
                    Spacer(Modifier.height(8.dp))
                    ExpenseListItem(
                        expense = expense,
                        currencySymbol = currencySymbol,
                        onClick = {
                            previewExpense = expense
//                            onEditExpense(expense)
                        }
                    )
                }

                if (filteredExpenses.size > 2) {
                    item {
                        Spacer(Modifier.height(10.dp))
                        DailyTopSpendingCard(
                            filteredExpenses = filteredExpenses,
                            currencySymbol = currencySymbol
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "That’s all for today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 40.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                item {
                    NoExpensesToday(
                        onAddExpenseClick = {
                            onAddExpense()
                        }
                    )
                }
            }
        }
    }

    if (showMonthExpenses) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
        }

        val startCal = Calendar.getInstance().apply {
            set(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                startOfMonthSetting,
                0,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }

        if (cal.before(startCal)) {
            startCal.add(Calendar.MONTH, -1)
        }

        val currentMonthName =
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(startCal.time)
        MonthSummaryDialog(
            currentMonthName = currentMonthName,
            currencySymbol = currencySymbol,
            monthlyTotal = monthlyTotal,
            monthlyExpenses = monthlyExpenses,
            onDismiss = { showMonthExpenses = false }
        )
    }

    if (showDatePicker) {
        PremiumDatePickerDialog(
            initialSelectedMillis = selectedDateMillis,
            onDateSelected = { millis ->
                expenseViewModel.updateSelectedDate(millis)
            },
            onDismiss = { showDatePicker = false }
        )
    }

//    if (showAdd) {
//        ExpenseDialog(
//            title = "Add Expense",
//            confirmText = "Add",
//            name = name,
//            amount = amount,
//            paymentMode = paymentMode,
//            onNameChange = { name = it },
//            onAmountChange = { amount = it },
//            onPaymentModeChange = {
//               paymentMode = it
//            },
//            onConfirm = {
//                val selectedDate = selectedDateMillis
//                val finalTimestamp = mergeDateWithCurrentTime(selectedDate)
//
//                expenseViewModel.addExpense(
//                    name,
//                    kotlin.math.abs(amount.toDoubleOrNull() ?: 0.0),
//                    currencySymbol,
//                    finalTimestamp,
//                    paymentMode
//                )
//                showAdd = false
//            },
//            onDismiss = { showAdd = false },
//            notes = notes,
//            onNotesChange = { notes = it },
//        )
//    }
//
//    if (showEdit && selectedExpense != null) {
//        ExpenseDialog(
//            title = "Edit Expense",
//            confirmText = "Update",
//            name = name,
//            amount = amount,
//            paymentMode = paymentMode,
//            onNameChange = { name = it },
//            onAmountChange = { amount = it },
//            onPaymentModeChange = { paymentMode = it },
//            onConfirm = {
//                val updatedExpense = selectedExpense!!.copy(
//                    title = name,
//                    amount = kotlin.math.abs(amount.toDoubleOrNull() ?: 0.0),
//                    paymentMode = paymentMode
//                )
//                expenseViewModel.updateExpense(updatedExpense)
//                showEdit = false
//            },
//            onDelete = {
//                expenseViewModel.deleteExpense(selectedExpense!!)
//                showEdit = false
//            },
//            onDismiss = { showEdit = false },
//            notes = notes,
//            onNotesChange = { notes = it },
//            isEdit = true,
//        )
//
//    }

    previewExpense?.let { expense ->

        ExpensePreviewDialog(

            expense = ExpensePreviewData(
                id = expense.id.toString(),
                amount = expense.amount.toString(),
                title = expense.title,
                category = expense.category,
                categoryEmoji = "💰", // we can map later
                paymentMode = expense.paymentMode,
                dateTime = formatExpenseDate(expense.timestamp),
                notes = expense.notes ?: "",
//                isManualCategory =false,
                currencySymbol = currencySymbol
            ),

            onDismiss = {
                previewExpense = null
            },

            onEdit = {
                previewExpense = null
                onEditExpense(expense)
            },

            onDelete = {
                previewExpense = null
                onDeleteExpense(expense)
            }
        )
    }
}

private fun formatExpenseDate(timestamp: Long): String {

    val sdf = SimpleDateFormat(
        "MMM dd, yyyy hh:mm a",
        Locale.getDefault()
    )

    return sdf.format(Date(timestamp))
}
