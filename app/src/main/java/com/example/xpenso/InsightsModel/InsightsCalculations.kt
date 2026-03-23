package com.example.xpenso.InsightsModel

import androidx.compose.ui.graphics.Color
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.formatIndianCurrency
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class to perform complex insight calculations.
 */
object InsightsCalculations {

    /**
     * Calculates comprehensive insights for a specific month.
     */
    fun calculateMonthlyInsights(
        allExpenses: List<ExpenseEntity>,
        month: Int,
        year: Int,
        budget: Double
    ): MonthlyInsightData {
        val calendar = Calendar.getInstance()
        
        // Filter expenses for current month
        val currentMonthExpenses = allExpenses.filter {
            calendar.timeInMillis = it.timestamp
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }

        // Filter expenses for previous month for comparison
        val prevMonthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            add(Calendar.MONTH, -1)
        }
        val prevMonthExpenses = allExpenses.filter {
            calendar.timeInMillis = it.timestamp
            calendar.get(Calendar.MONTH) == prevMonthCal.get(Calendar.MONTH) && 
            calendar.get(Calendar.YEAR) == prevMonthCal.get(Calendar.YEAR)
        }

        val totalSpent = currentMonthExpenses.sumOf { it.amount }
        val prevTotal = prevMonthExpenses.sumOf { it.amount }
        
        val comparisonPercent = if (prevTotal > 0) {
            ((totalSpent - prevTotal) / prevTotal) * 100
        } else null

        // Calculate Daily Average
        val daysInMonth = if (month == Calendar.getInstance().get(Calendar.MONTH) && year == Calendar.getInstance().get(Calendar.YEAR)) {
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        } else {
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
            }.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        val dailyAvg = if (daysInMonth > 0) totalSpent / daysInMonth else 0.0

        // Process Pie Chart Data with "Other" grouping for < 2%
        val categoryGroups = currentMonthExpenses.groupBy { ExpenseCategory.detect(it.title) }
            .mapValues { it.value.sumOf { e -> e.amount } }
            .toList()
            .sortedByDescending { it.second }

        val pieData = mutableListOf<CategoryPieData>()
        var otherAmount = 0.0
        
        categoryGroups.forEach { (category, amount) ->
            val percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
            if (percentage < 0.02f) {
                otherAmount += amount
            } else {
                pieData.add(
                    CategoryPieData(
                        categoryName = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        amount = amount,
                        percentage = percentage,
                        color = category.color
                    )
                )
            }
        }

        if (otherAmount > 0) {
            pieData.add(
                CategoryPieData(
                    categoryName = "Other",
                    amount = otherAmount,
                    percentage = (otherAmount / totalSpent).toFloat(),
                    color = Color.Gray
                )
            )
        }

        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
            }.time
        )

        val overview = MonthlyOverview(
            totalSpent = totalSpent,
            totalTransactions = currentMonthExpenses.size,
            dailyAverage = dailyAvg,
            prevMonthComparisonPercent = comparisonPercent,
            monthName = monthName,
            month = month,
            year = year
        )

        val topCat = pieData.maxByOrNull { it.amount }
        val smartInsight = generateMonthlySmartInsight(overview, topCat)

        return MonthlyInsightData(
            overview = overview,
            pieChartData = pieData,
            smartInsight = smartInsight,
            budget = budget,
            spent = totalSpent
        )
    }

    /**
     * Calculates comprehensive insights for a specific year.
     */
    fun calculateYearlyInsights(
        allExpenses: List<ExpenseEntity>,
        year: Int,
        monthlyBudgets: Map<Int, Double> // Map of Month (0-11) to Budget amount
    ): YearlyInsightData {
        val calendar = Calendar.getInstance()
        
        // Filter expenses for current year
        val currentYearExpenses = allExpenses.filter {
            calendar.timeInMillis = it.timestamp
            calendar.get(Calendar.YEAR) == year
        }

        // Filter expenses for previous year
        val prevYearExpenses = allExpenses.filter {
            calendar.timeInMillis = it.timestamp
            calendar.get(Calendar.YEAR) == year - 1
        }

        val totalSpent = currentYearExpenses.sumOf { it.amount }
        val prevTotal = prevYearExpenses.sumOf { it.amount }
        val comparisonPercent = if (prevTotal > 0) {
            ((totalSpent - prevTotal) / prevTotal) * 100
        } else null

        // Monthly breakdown
        var monthsUnderBudget = 0
        var monthsOverBudget = 0
        
        val monthlyBreakdown = (0..11).map { m ->
            val mTotal = currentYearExpenses.filter {
                calendar.timeInMillis = it.timestamp
                calendar.get(Calendar.MONTH) == m
            }.sumOf { it.amount }
            
            val budget = monthlyBudgets[m] ?: 0.0
            if (budget > 0) {
                if (mTotal <= budget) monthsUnderBudget++ else monthsOverBudget++
            }

            val label = SimpleDateFormat("MMM", Locale.getDefault()).format(
                Calendar.getInstance().apply { set(Calendar.MONTH, m) }.time
            )
            
            YearlyMonthData(label, mTotal, m, year)
        }

        val highestMonth = monthlyBreakdown.maxByOrNull { it.amount }?.copy(isHighest = true) ?: YearlyMonthData("N/A", 0.0, 0, year)
        val lowestMonth = monthlyBreakdown.filter { it.amount > 0 }.minByOrNull { it.amount } ?: monthlyBreakdown.first()

        // Top Category for the year
        val topCategoryEntry = currentYearExpenses.groupBy { ExpenseCategory.detect(it.title) }
            .mapValues { it.value.sumOf { e -> e.amount } }
            .maxByOrNull { it.value }

        val topCategoryData = topCategoryEntry?.let { (cat, amount) ->
            CategoryPieData(
                categoryName = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                amount = amount,
                percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f,
                color = cat.color
            )
        }

        val overview = YearlyOverview(
            totalSpent = totalSpent,
            totalTransactions = currentYearExpenses.size,
            averageMonthlySpend = totalSpent / 12,
            prevYearComparisonPercent = comparisonPercent,
            year = year
        )

        val budgetPerf = BudgetPerformance(monthsUnderBudget, monthsOverBudget)
        val smartInsight = generateYearlySmartInsight(overview, highestMonth, topCategoryData)

        return YearlyInsightData(
            overview = overview,
            monthlyBreakdown = monthlyBreakdown,
            highestSpendingMonth = highestMonth,
            lowestSpendingMonth = lowestMonth,
            topCategory = topCategoryData,
            budgetPerformance = budgetPerf,
            smartInsight = smartInsight
        )
    }

    private fun generateMonthlySmartInsight(overview: MonthlyOverview, topCat: CategoryPieData?): String {
        if (overview.totalSpent == 0.0) return "No data for this month yet. Start tracking your expenses!"
        
        val comp = overview.prevMonthComparisonPercent
        if (comp != null) {
            if (comp <= -20) return "Great job! You spent ${Math.abs(comp).toInt()}% less than last month."
            if (comp >= 20) return "Alert: Your spending is up ${comp.toInt()}% from last month. Check your ${topCat?.categoryName ?: "top"} expenses."
        }

        return topCat?.let {
            "${it.categoryName} is your biggest expense this month, accounting for ${(it.percentage * 100).toInt()}% of your total spending."
        } ?: "You're doing well! Keep tracking your daily average of ₹${formatIndianCurrency(overview.dailyAverage)}."
    }

    private fun generateYearlySmartInsight(overview: YearlyOverview, highest: YearlyMonthData, topCat: CategoryPieData?): String {
        if (overview.totalSpent == 0.0) return "Start tracking your yearly journey to see deep insights here."

        val comp = overview.prevYearComparisonPercent
        if (comp != null) {
            if (comp <= -20) return "Excellent! You reduced your yearly spending by ${Math.abs(comp).toInt()}% compared to last year."
            if (comp >= 20) return "Your annual spending increased by ${comp.toInt()}% compared to last year. ${highest.label} was your peak month."
        }

        return topCat?.let {
            "${it.categoryName} was your dominant spending category this year (${(it.percentage * 100).toInt()}%)."
        } ?: "${highest.label} was your highest spending month this year at ₹${formatIndianCurrency(highest.amount)}."
    }
}
