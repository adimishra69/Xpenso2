package com.example.xpenso.InsightYearView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.InsightsModel.YearlyInsightStats
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.Viewmodel.BudgetViewModel
import com.example.xpenso.Viewmodel.ExpenseViewModel
import com.example.xpenso.InsightMonthComponents.toDisplayUpper
import com.example.xpenso.InsightsModel.MonthlySummary
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun YearView(
    currencySymbol: String,
    expenseViewModel: ExpenseViewModel,
    settingsStore: SettingsStore,
    budgetViewModel: BudgetViewModel
) {
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    val allExpenses by expenseViewModel.allExpenses.collectAsState(initial = emptyList())
    val startOfMonth by settingsStore.startOfMonth.collectAsState(initial = 1)
    val allBudgets by budgetViewModel.allBudgets.collectAsState()

    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("InsightsYear")
    }

    // 1. --- HELPER: CALCULATE 12 CYCLES FOR ANY YEAR ---
    fun getCyclesForYear(year: Int): List<Pair<MonthlySummary, LongRange>> {
        return (0..11).map { monthIndex ->
            val cycleStart = Calendar.getInstance().apply {
                set(year, monthIndex, startOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val cycleEnd = (cycleStart.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }
            val range = cycleStart.timeInMillis..cycleEnd.timeInMillis
            val total = allExpenses.filter { it.timestamp in range }.sumOf { it.amount }
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cycleStart.time)
            MonthlySummary(monthName, total) to range
        }
    }

    // 2. --- CORE CALCULATIONS ---
    val currentYearCycles = remember(allExpenses, selectedYear, startOfMonth) { getCyclesForYear(selectedYear) }
    val prevYearCycles = remember(allExpenses, selectedYear, startOfMonth) { getCyclesForYear(selectedYear - 1) }

    // Collect 12 monthly budgets for selected year
    val monthlyBudgets = (0..11).map { monthIndex ->
        allBudgets
            .firstOrNull {
                it.month == monthIndex && it.year == selectedYear
            }
            ?.amount ?: 0.0
    }

    // 🔥 FIXED: Added monthlyBudgets to remember keys so the stats recalculate when budgets load or change
    val yearlyInsightStats by remember(currentYearCycles, prevYearCycles, allExpenses, monthlyBudgets) {
        derivedStateOf {
            val totalYearlyExpense = currentYearCycles.sumOf { it.first.totalExpense }

            // 🔥 Yearly Net Balance = Sum of (Budget - Expense)
            val yearlyNetBalance = currentYearCycles.mapIndexedNotNull { index, cycle ->

                val expense = cycle.first.totalExpense
                val budget = monthlyBudgets.getOrElse(index) { 0.0 }

                if (budget > 0) {
                    budget - expense
                } else {
                    null
                }
            }.sum()

            val totalPreviousYearExpense = prevYearCycles.sumOf { it.first.totalExpense }

            val highestMonth = currentYearCycles.map { it.first }.filter { it.totalExpense > 0 }.maxByOrNull { it.totalExpense }
            val lowestMonth = currentYearCycles.map { it.first }.filter { it.totalExpense > 0 }.minByOrNull { it.totalExpense }

            val highestQuarter = currentYearCycles.chunked(3).mapIndexed { index, quarterData ->
                "Q${index + 1}" to quarterData.sumOf { it.first.totalExpense }
            }.maxByOrNull { it.second }?.takeIf { it.second > 0 }

            val yearStart = currentYearCycles.first().second.first
            val yearEnd = currentYearCycles.last().second.last
            val expensesInYearRange = allExpenses.filter { it.timestamp in yearStart..yearEnd }

            val mostExpensiveCategory = expensesInYearRange
                .groupBy {
                    ExpenseCategory.entries
                        .find { cat -> cat.name == it.category }
                        ?: ExpenseCategory.OTHER
                }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }
                .filter { it.first != ExpenseCategory.OTHER } // 🔥 remove OTHER
                .firstOrNull() // ✅ takes top valid OR null if none

            val yearOverYearChange = if (totalPreviousYearExpense > 0) {
                ((totalYearlyExpense - totalPreviousYearExpense) / totalPreviousYearExpense) * 100
            } else null

            val totalDaysInCycles = currentYearCycles.sumOf { (it.second.last - it.second.first) / (1000 * 60 * 60 * 24) + 1 }
            val avgDaily = if (totalYearlyExpense > 0) totalYearlyExpense / totalDaysInCycles else 0.0

            val monthsWithExpenses = currentYearCycles.count { it.first.totalExpense > 0 }
            val canShowProjection = selectedYear == Calendar.getInstance().get(Calendar.YEAR) && totalYearlyExpense > 0 && monthsWithExpenses >= 4

            val projectedYearTotal = if (canShowProjection) {
                val latestExpenseTimestamp = expensesInYearRange.maxByOrNull { it.timestamp }?.timestamp ?: 0L
                val firstCycleStart = currentYearCycles.first().second.first
                val passedDays = if (latestExpenseTimestamp > firstCycleStart) {
                    ((latestExpenseTimestamp - firstCycleStart) / (1000 * 60 * 60 * 24)) + 1
                } else 1L
                (totalYearlyExpense / passedDays) * totalDaysInCycles
            } else null

            YearlyInsightStats(
                totalYearlyExpense = totalYearlyExpense,
                highestSpendingMonth = highestMonth,
                lowestSpendingMonth = lowestMonth,
                averageDailySpending = avgDaily,
                yearOverYearChange = yearOverYearChange,
                mostExpensiveCategory = mostExpensiveCategory,
                highestSpendingQuarter = highestQuarter,
                totalPreviousYearExpense = totalPreviousYearExpense,
                projectedYearlyExpense = projectedYearTotal,
                yearlyNetBalance = yearlyNetBalance
            )
        }
    }

    val monthlyBreakdown = remember(currentYearCycles) {
        val firstCycleWithExpenseIndex = currentYearCycles.indexOfFirst { it.first.totalExpense > 0 }
        if (firstCycleWithExpenseIndex != -1) {
            val reorderedCycles = currentYearCycles.subList(firstCycleWithExpenseIndex, 12) + currentYearCycles.subList(0, firstCycleWithExpenseIndex)
            reorderedCycles.map { it.first }.filter { it.totalExpense > 0 }
        } else {
            emptyList()
        }
    }

    val hasYearData = yearlyInsightStats.totalYearlyExpense > 0
    val monthsWithExpenses = currentYearCycles.count { it.first.totalExpense > 0 }
    val canShowProjection = selectedYear == Calendar.getInstance().get(Calendar.YEAR) && hasYearData && monthsWithExpenses >= 4

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { YearSelector(selectedYear = selectedYear, onYearChange = { selectedYear = it }) }
        item { TotalYearlyExpenseCard(currencySymbol, yearlyInsightStats.totalYearlyExpense, selectedYear) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                yearlyInsightStats.highestSpendingMonth?.let {
                    InsightMetricCard(Icons.Default.TrendingUp, "Highest Month", it.monthName, Modifier.weight(1f))
                }
                yearlyInsightStats.lowestSpendingMonth?.takeIf { it != yearlyInsightStats.highestSpendingMonth }?.let {
                    InsightMetricCard(Icons.Default.TrendingDown, "Lowest Month", it.monthName, Modifier.weight(1f))
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                yearlyInsightStats.mostExpensiveCategory?.let { (category, _) ->
                    InsightMetricCard(Icons.Default.Category, "Top Category", category.name.toDisplayUpper(), Modifier.weight(1f))
                }

                yearlyInsightStats.highestSpendingQuarter?.let { (quarter, _) ->
                    InsightMetricCard(Icons.Default.Timeline, "Top Quarter", quarter, Modifier.weight(1f))
                }

            }
        }
        val hasPrevious = yearlyInsightStats.totalPreviousYearExpense != 0.0
        if (hasYearData && hasPrevious) {
            item {
                val yoy = yearlyInsightStats.yearOverYearChange ?: 0.0

                val isIncrease = yoy > 0
                val yoyColor = when {
                    isIncrease -> MaterialTheme.colorScheme.error
                    else -> Color(0xFF4CAF50)
                }

                LongInsightMetricCard(
                    icon = Icons.Default.CompareArrows,
                    label = "Year-over-Year",
                    value = buildAnnotatedString {

                            val amount = if (isIncrease)
                                yearlyInsightStats.totalYearlyExpense - yearlyInsightStats.totalPreviousYearExpense
                            else
                                yearlyInsightStats.totalPreviousYearExpense - yearlyInsightStats.totalYearlyExpense

                            // Arrow
                            withStyle(SpanStyle(color = yoyColor)) {
                                append(if (isIncrease) "▲ " else "▼ ")
                            }

                            // Smaller currency
                            withStyle(
                                SpanStyle(
                                    fontSize = (MaterialTheme.typography.titleLarge.fontSize.value - 4).sp,
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                append("$currencySymbol ")
                            }

                            // Bigger amount
                            withStyle(
                                SpanStyle(
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(CurrencyFormatter.format(amount, currencySymbol))
                            }

                    },
                    valueColor = yoyColor
                )

            }
        }

            if (selectedYear == Calendar.getInstance().get(Calendar.YEAR) && (canShowProjection)) {
                item {

                    LongInsightMetricCard(
                        icon = Icons.Default.ShowChart,
                        label = "Year Projection",
                        value = buildAnnotatedString {


                                val projected = yearlyInsightStats.projectedYearlyExpense ?: 0.0

                                withStyle(
                                    SpanStyle(
                                        fontSize = (MaterialTheme.typography.titleLarge.fontSize.value - 4).sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                ) {
                                    append("$currencySymbol ")
                                }

                                withStyle(
                                    SpanStyle(
                                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(CurrencyFormatter.format(projected, currencySymbol))
                                }


                        }
                    )
                }
            }

        if (hasYearData) {
            item {
                LongInsightMetricCard(
                    icon = Icons.Default.Autorenew,
                    label = "Avg. Daily Spend",
                    value = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = (MaterialTheme.typography.titleLarge.fontSize.value - 4).sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("$currencySymbol ")
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(
                                CurrencyFormatter.format(
                                    yearlyInsightStats.averageDailySpending,
                                    currencySymbol
                                )
                            )
                        }
                    }
                )
            }

            item {
                val balance = yearlyInsightStats.yearlyNetBalance
                val isPositive = balance >= 0

                LongInsightMetricCard(
                    icon = Icons.Default.AccountBalance,
                    label = if (isPositive) "Total Saved" else "Total Overspent",
                    value = buildAnnotatedString {

                        // Smaller currency symbol
                        withStyle(
                            SpanStyle(
                                fontSize = (MaterialTheme.typography.titleLarge.fontSize.value - 4).sp,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append("$currencySymbol ")
                        }

                        // Bigger amount
                        withStyle(
                            SpanStyle(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(
                                CurrencyFormatter.format(
                                    kotlin.math.abs(balance),
                                    currencySymbol
                                )
                            )
                        }
                    },
                    valueColor = if (isPositive)
                        Color(0xFF4CAF50)
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }


        if (!hasYearData) {
            item { YearEmptyState() }
        }

        if (monthlyBreakdown.isNotEmpty()) {
            item {
                MonthlyBreakdownHeader()
            }
            items(monthlyBreakdown, key = { it.monthName }) { summary ->
                MonthExpenseRow(summary = summary, currencySymbol = currencySymbol)
            }
        }
    }
}
