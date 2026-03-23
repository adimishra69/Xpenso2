package com.example.xpenso.InsightsModel


// --- Monthly Insights Data Structures ---

/**
 * Represents the overall summary data for a selected month.
 */
data class MonthlyOverview(
    val totalSpent: Double,
    val totalTransactions: Int,
    val dailyAverage: Double,
    val prevMonthComparisonPercent: Double?, // Percentage change from the previous month
    val monthName: String, // e.g., "January 2026"
    val month: Int, // 0-11
    val year: Int
)


/**
 * Comprehensive data holder for the Monthly Insights View.
 */
data class MonthlyInsightData(
    val overview: MonthlyOverview,
    val pieChartData: List<CategoryPieData>, // For the donut chart and legend
    val smartInsight: String, // The generated smart insight text
    val budget: Double, // The effective budget for the month
    val spent: Double // Total spent for the month (redundant with overview.totalSpent, but kept for clarity with budget context)
)

// --- Yearly Insights Data Structures ---

/**
 * Represents the overall summary data for a selected year.
 */
data class YearlyOverview(
    val totalSpent: Double,
    val totalTransactions: Int,
    val averageMonthlySpend: Double,
    val prevYearComparisonPercent: Double?, // Percentage change from the previous year
    val year: Int
)

/**
 * Represents spending for a single month within a year breakdown.
 */
data class YearlyMonthData(
    val label: String, // e.g., "Jan"
    val amount: Double,
    val month: Int, // 0-11
    val year: Int,
    val isHighest: Boolean = false // True if this is the highest spending month in the year
)

/**
 * Represents the budget performance (months under/over budget) for a year.
 */
data class BudgetPerformance(
    val monthsUnderBudget: Int,
    val monthsOverBudget: Int
)

/**
 * Comprehensive data holder for the Yearly Insights View.
 */
data class YearlyInsightData(
    val overview: YearlyOverview,
    val monthlyBreakdown: List<YearlyMonthData>, // For the yearly bar chart
    val highestSpendingMonth: YearlyMonthData,
    val lowestSpendingMonth: YearlyMonthData,
    val topCategory: CategoryPieData?, // Top category for the *entire year*
    val budgetPerformance: BudgetPerformance,
    val smartInsight: String, // The generated smart yearly insight text

)
