package com.example.xpenso.Home

import androidx.compose.ui.graphics.Color
import java.util.Calendar

data class DailyInsight(
    val message: String,
    val color: Color
)

fun getDailySpendingInsight(
    dailyTotal: Double,
    monthlyBudget: Double,
    month: Int,
    year: Int,
    primaryColor: Color,
    surfaceVariantColor: Color,
    errorColor: Color
): DailyInsight {

    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, month)
        set(Calendar.YEAR, year)
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val dailyAverage =
        if (monthlyBudget > 0)
            monthlyBudget / daysInMonth
        else
            0.0

    return when {
        dailyTotal == 0.0 ->
            DailyInsight(
                "Great! No spending today",
                primaryColor
            )

        dailyAverage > 0 && dailyTotal < dailyAverage * 0.5 ->
            DailyInsight(
                "Nice control today",
                Color(0xFF4CAF50)
            )

        dailyAverage > 0 && dailyTotal <= dailyAverage ->
            DailyInsight(
                "You're within your daily limit",
                surfaceVariantColor
            )

        else ->
            DailyInsight(
                "High spending today",
                errorColor
            )
    }
}
