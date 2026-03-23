package com.example.xpenso.Home

import androidx.compose.ui.graphics.Color
import java.util.Calendar

data class DailyInsightResult(
    val message: String,
    val color: Color
)

// UI-facing function that returns a result with a Compose Color
fun getDailyBudgetInsight(
    dailyTotal: Double,
    monthlyTotal: Double,
    monthlyBudget: Double,
    selectedDateMillis: Long,
    startOfMonth: Int,
    primary: Color,
    normal: Color,
    danger: Color
): DailyInsightResult {
    val message = getDailyBudgetInsightMessage(
        dailyTotal,
        monthlyTotal,
        monthlyBudget,
        selectedDateMillis,
        startOfMonth
    )

    val insightColor = when (message) {
        "Excellent control today" -> Color(0xFF4CAF50)
        "You\'re within today\'s safe limit" -> normal
        "High spending today" -> danger
        else -> primary
    }

    return DailyInsightResult(message, insightColor)
}

// Logic-only function that can be used anywhere, including background workers
fun getDailyBudgetInsightMessage(
    dailyTotal: Double,
    monthlyTotal: Double,
    monthlyBudget: Double,
    selectedDateMillis: Long,
    startOfMonth: Int
): String {

    if (monthlyBudget <= 0.0) {
        return "Set a monthly budget to get insights"
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    val startCal = Calendar.getInstance().apply {
        timeInMillis = selectedDateMillis
        set(Calendar.DAY_OF_MONTH, startOfMonth)
    }
    if (currentDayOfMonth < startOfMonth) {
        startCal.add(Calendar.MONTH, -1)
    }

    val endCal = (startCal.clone() as Calendar).apply {
        add(Calendar.MONTH, 1)
        add(Calendar.DAY_OF_MONTH, -1)
    }

    val daysInCycle = endCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysPassedInCycle = if (currentDayOfMonth >= startOfMonth) {
        currentDayOfMonth - startOfMonth
    } else {
        val prevMonth = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH) - startOfMonth + currentDayOfMonth
    }

    val daysLeftInCycle = (daysInCycle - daysPassedInCycle).coerceAtLeast(1)
    val remainingBudget = (monthlyBudget - monthlyTotal + dailyTotal).coerceAtLeast(0.0)
    val dailySafeToSpend = remainingBudget / daysLeftInCycle

    return when {
        dailyTotal == 0.0 -> "Great! No spending today"
        dailyTotal <= dailySafeToSpend * 0.7 -> "Excellent control today"
        dailyTotal <= dailySafeToSpend -> "You\'re within today\'s safe limit"
        else -> "High spending today"
    }
}