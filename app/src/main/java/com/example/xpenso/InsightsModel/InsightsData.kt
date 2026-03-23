package com.example.xpenso.InsightsModel

import com.example.xpenso.Transaction.ExpenseCategory

// Data class for monthly spending summary
data class MonthlySummary(
    val monthName: String,
    val totalExpense: Double
)

// Data class for aggregated yearly statistics
data class YearlyInsightStats(
    val totalYearlyExpense: Double,
    val highestSpendingMonth: MonthlySummary?,
    val lowestSpendingMonth: MonthlySummary?,
    val averageDailySpending: Double,
    val yearOverYearChange: Double?,
    val mostExpensiveCategory: Pair<ExpenseCategory, Double>?,
    val highestSpendingQuarter: Pair<String, Double>?,
    val totalPreviousYearExpense: Double,
    val projectedYearlyExpense: Double?,
    val yearlyNetBalance: Double
)
