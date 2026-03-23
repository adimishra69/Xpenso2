package com.example.xpenso.InsightsModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.InsightMonthComponents.CategoryDonutChart
import com.example.xpenso.InsightMonthComponents.CategoryLegend
import com.example.xpenso.InsightMonthComponents.InsightSummaryCard
import com.example.xpenso.InsightMonthComponents.SimpleBarChart
import com.example.xpenso.InsightMonthComponents.SmartInsightAnimatedText
import com.example.xpenso.InsightMonthComponents.generateSmartInsight
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.ui.theme.LocalBudgetColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/* ---------------------------------------------------------
   MONTH VIEW CONTENT — PREMIUM REDESIGN
   Logic: 100% unchanged  |  Design: premium elevated
   Colors: MaterialTheme + LocalBudgetColors only (no hardcoding)
---------------------------------------------------------- */
@Composable
fun MonthViewContent(
    currencySymbol: String,
    expenseViewModel: ExpenseViewModel,
    settingsStore: SettingsStore,
    startOfMonth: Int,
    onCategoryClick: (String, Long, Long) -> Unit,
    onPremiumRequired: () -> Unit
) {
    // ── All original logic — untouched ───────────────────────────────────────
    val isPremium by settingsStore.isPremium.collectAsState(initial = false)
    val haptic = LocalHapticFeedback.current
    val allExpenses by expenseViewModel.allExpenses.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) { AnalyticsHelper.trackScreen("InsightsMonth") }

    val monthlyTrend = remember(allExpenses, startOfMonth) {
        val trend = mutableListOf<MonthlyTrendData>()
        val cal = Calendar.getInstance()
        for (i in 0 until 12) {
            val currentCal = cal.clone() as Calendar
            currentCal.add(Calendar.MONTH, -i)
            val month = currentCal.get(Calendar.MONTH)
            val year = currentCal.get(Calendar.YEAR)
            val startCal = Calendar.getInstance().apply {
                set(year, month, startOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = (startCal.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }
            val total = allExpenses
                .filter { it.timestamp in startCal.timeInMillis..endCal.timeInMillis }
                .sumOf { it.amount }
            val label = SimpleDateFormat("MMM", Locale.getDefault()).format(currentCal.time)
            trend.add(MonthlyTrendData(label = label, amount = total, month = month, year = year))
        }
        trend.reversed()
    }

    var selectedMonthIndex by rememberSaveable { mutableIntStateOf(monthlyTrend.lastIndex) }
    val selectedTrend = monthlyTrend.getOrNull(selectedMonthIndex)

    val currentMonthRange = remember(selectedTrend, startOfMonth) {
        if (selectedTrend == null) null
        else {
            val start = Calendar.getInstance().apply {
                set(selectedTrend.year, selectedTrend.month, startOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val end = (start.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }
            start.timeInMillis to end.timeInMillis
        }
    }

    val selectedMonthExpenses = remember(allExpenses, currentMonthRange) {
        val range = currentMonthRange ?: return@remember emptyList()
        allExpenses.filter { it.timestamp in range.first..range.second }
    }

    val smartInsight = remember(allExpenses, selectedTrend, startOfMonth) {
        if (selectedTrend == null) ""
        else generateSmartInsight(
            currencySymbol = currencySymbol,
            allExpenses = allExpenses,
            selectedMonth = selectedTrend.month,
            selectedYear = selectedTrend.year,
            startOfMonth = startOfMonth
        )
    }

    val insightStats = remember(selectedMonthExpenses, selectedTrend, startOfMonth) {
        if (selectedTrend == null)
            return@remember InsightStats(0.0, 0.0, 0.0, "N/A", "N/A")
        val month = selectedTrend.month
        val year = selectedTrend.year
        val monthExpenses = selectedMonthExpenses
        val total = monthExpenses.sumOf { it.amount }
        val startCalForDays = Calendar.getInstance().apply {
            set(year, month, startOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCalForDays = (startCalForDays.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        val daysInPeriod =
            ((endCalForDays.timeInMillis - startCalForDays.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
        val daysConsideredForAverage = if (startCalForDays.before(Calendar.getInstance())) {
            val today = Calendar.getInstance()
            if (endCalForDays.before(today)) daysInPeriod
            else ((today.timeInMillis - startCalForDays.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
        } else 0
        val avg = if (daysConsideredForAverage > 0) total / daysConsideredForAverage else 0.0
        val groupedDays = monthExpenses.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_MONTH)
        }
        val highestDay = groupedDays.maxByOrNull { it.value.sumOf { e -> e.amount } }
        val highestAmount = highestDay?.value?.sumOf { it.amount } ?: 0.0
        val highestLabel = highestDay?.value?.firstOrNull()?.let { firstExpenseOfDay ->
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(
                Calendar.getInstance().apply { timeInMillis = firstExpenseOfDay.timestamp }.time
            )
        } ?: "N/A"
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.MONTH, month)
                set(Calendar.YEAR, year)
            }.time
        )
        InsightStats(total, avg, highestAmount, highestLabel, monthName)
    }

    val pieChartData = remember(selectedMonthExpenses) {
        if (selectedMonthExpenses.isEmpty()) return@remember emptyList<CategoryPieData>()
        val totalExpensesAmount = selectedMonthExpenses.sumOf { it.amount }
        if (totalExpensesAmount == 0.0) return@remember emptyList()
        val MIN_PERCENT_FOR_SEPARATE_SLICE = 2f
        val rawCategoryAmounts = selectedMonthExpenses
            .groupBy { ExpenseCategory.detect(it.title) }
            .mapValues { it.value.sumOf { e -> e.amount } }
        val finalSliceAmounts = mutableMapOf<ExpenseCategory, Double>()
        var otherAggregatedAmount = 0.0
        rawCategoryAmounts.forEach { (category, amount) ->
            val percentage = (amount / totalExpensesAmount) * 100f
            when {
                category == ExpenseCategory.OTHER -> otherAggregatedAmount += amount
                percentage > MIN_PERCENT_FOR_SEPARATE_SLICE -> finalSliceAmounts[category] = amount
                else -> otherAggregatedAmount += amount
            }
        }
        if (otherAggregatedAmount > 0.0) {
            finalSliceAmounts[ExpenseCategory.OTHER] =
                (finalSliceAmounts[ExpenseCategory.OTHER] ?: 0.0) + otherAggregatedAmount
        }
        val finalPieData = mutableListOf<CategoryPieData>()
        var currentSumOfPercentages = 0
        finalSliceAmounts.forEach { (category, amount) ->
            val percentage = ((amount / totalExpensesAmount) * 100f).toInt()
            finalPieData.add(
                CategoryPieData(
                    categoryName = category.name.uppercase(),
                    amount = amount,
                    percentage = percentage / 100f,
                    color = category.color
                )
            )
            currentSumOfPercentages += percentage
        }
        val difference = 100 - currentSumOfPercentages
        if (difference != 0 && finalPieData.isNotEmpty()) {
            val largestSliceIndex = finalPieData.indices.maxByOrNull { finalPieData[it].percentage }
            if (largestSliceIndex != null) {
                val originalSlice = finalPieData[largestSliceIndex]
                finalPieData[largestSliceIndex] =
                    originalSlice.copy(percentage = originalSlice.percentage + (difference / 100f))
            }
        }
        finalPieData.filter { it.percentage > 0f }.sortedByDescending { it.amount }
    }

    // ── Design tokens ────────────────────────────────────────────────────────
    val colorScheme = MaterialTheme.colorScheme
    val budgetColors = LocalBudgetColors.current
    // Insight card uses budgetColors.blueBg / blueTitle / blueDesc
    // All other surfaces use MaterialTheme only — zero hardcoding

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {

        // ── Smart Insight card ───────────────────────────────────────────────
        // Uses LocalBudgetColors.blueBg/blueTitle/blueDesc — correct light/dark
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Emoji in a tinted circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(budgetColors.blueTitle.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 22.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Smart Insight",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    SmartInsightAnimatedText(
                        insight = smartInsight,
                        color = budgetColors.blueDesc
                    )
                }
            }
        }

        Spacer(Modifier.height(36.dp))

        // ── Section header: Monthly Trend ────────────────────────────────────
        PremiumSectionHeader(title = "Monthly Spending Trend")

        Spacer(Modifier.height(16.dp))

        // Bar chart wrapped in a surface card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                SimpleBarChart(
                    data = monthlyTrend,
                    selectedIndex = selectedMonthIndex,
                    currencySymbol = currencySymbol,
                    onMonthClick = { selectedMonthIndex = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        // ── Donut chart section ───────────────────────────────────────────────
        if (pieChartData.isNotEmpty()) {
            PremiumSectionHeader(title = "Spending by Category • ${insightStats.monthName}")

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    CategoryDonutChart(
                        data = pieChartData,
                        totalAmount = insightStats.total,
                        currencySymbol = currencySymbol
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Legend — no extra card, let items breathe
            CategoryLegend(
                data = pieChartData,
                currencySymbol = currencySymbol,
                onCategoryClick = { categoryName ->
                    if (!isPremium) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPremiumRequired()
                        return@CategoryLegend
                    }
                    currentMonthRange?.let { (start, end) ->
                        onCategoryClick(categoryName, start, end)
                    }
                }
            )

            Spacer(Modifier.height(40.dp))
        }

        // ── Summary section ──────────────────────────────────────────────────
        PremiumSectionHeader(title = "Spending Summary • ${insightStats.monthName}")

        Spacer(Modifier.height(16.dp))

        InsightSummaryCard(currencySymbol, insightStats)

        Spacer(Modifier.height(28.dp))
    }
}

/* ── Reusable premium section header ─────────────────────────────────────────
   Accent pip + bold label — same pattern as HomeScreen, fully theme-adaptive  */
@Composable
private fun PremiumSectionHeader(title: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp
            ),
            color = colorScheme.onSurface
        )
    }
}