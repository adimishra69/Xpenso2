package com.example.myproject.Insights

import androidx.compose.ui.graphics.Color
import com.example.xpenso.Transaction.ExpenseCategory

data class YearlyInsightStats(
    val totalYearlyExpense: Double,
    val highestSpendingMonth: MonthlySummary?,
    val lowestSpendingMonth: MonthlySummary?,
    val averageDailySpending: Double,
    val yearOverYearChange: Double?,
    val mostExpensiveCategory: Pair<ExpenseCategory, Double>?,
    val highestSpendingQuarter: Pair<String, Double>?,
    val totalPreviousYearExpense: Double,
    val projectedYearlyExpense: Double?   // ⭐ NEW
)



data class MonthlySummary(
    val monthName: String,
    val totalExpense: Double
)
