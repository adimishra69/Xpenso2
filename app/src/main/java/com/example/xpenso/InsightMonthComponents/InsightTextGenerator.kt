package com.example.xpenso.InsightMonthComponents

import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.example.xpenso.utils.CurrencyFormatter
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import java.util.Calendar
import kotlin.math.abs

fun generateSmartInsight(
    currencySymbol: String,
    allExpenses: List<ExpenseEntity>,
    selectedMonth: Int,
    selectedYear: Int,
    startOfMonth: Int
): String {

    // ----------------------------
    // Current Period Calculation
    // ----------------------------
    val startCalThisPeriod = Calendar.getInstance().apply {
        set(selectedYear, selectedMonth, startOfMonth, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val endCalThisPeriod = (startCalThisPeriod.clone() as Calendar).apply {
        add(Calendar.MONTH, 1)
        add(Calendar.MILLISECOND, -1)
    }
    val thisMonthExpenses = allExpenses.filter { it.timestamp in startCalThisPeriod.timeInMillis..endCalThisPeriod.timeInMillis }

    if (thisMonthExpenses.isEmpty()) {
        return "No expenses recorded for this period"
    }

    // ----------------------------
    // Previous Period Calculation
    // ----------------------------
    val startCalLastPeriod = (startCalThisPeriod.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val endCalLastPeriod = (endCalThisPeriod.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val lastMonthExpenses = allExpenses.filter { it.timestamp in startCalLastPeriod.timeInMillis..endCalLastPeriod.timeInMillis }

    // ----------------------------
    // Totals
    // ----------------------------
    val thisTotal = thisMonthExpenses.sumOf { it.amount }
    val lastTotal = lastMonthExpenses.sumOf { it.amount }

    // ----------------------------
    // ZERO & NEAR-ZERO GUARDS
    // ----------------------------

    if (lastTotal == 0.0 && thisTotal > 0.0) {
        return "⚠️ You had no expenses last period, but spent $currencySymbol ${
            CurrencyFormatter.format(thisTotal, currencySymbol)
        } this period"
    }

    if (lastTotal < 100 && thisTotal > lastTotal) {
        return "⚠️ You had very little spending last period, so this period appears much higher"
    }
    if (lastTotal > 0) { // Avoid division by zero
        val percentChange = ((thisTotal - lastTotal) / lastTotal) * 100
        val absPercent = abs(percentChange)

        if (percentChange < -30) {
            return "Amazing! You reduced spending by ${absPercent.toInt()}% compared to last period"
        }

        if (percentChange > 200) {
            return "🚨 Your spending increased drastically compared to last period"
        }

        if (percentChange > 30) {
            return "Warning ⚠️ Your spending increased by ${absPercent.toInt()}% this period"
        }

        if (thisTotal > lastTotal && percentChange > 10) {
            return "You spent $currencySymbol${
                CurrencyFormatter.format(thisTotal - lastTotal, currencySymbol)
            } more than last period"
        }
    }

    // ----------------------------
    // Category fallback
    // ----------------------------
    val categoryTotals = thisMonthExpenses.groupBy {

        (ExpenseCategory.entries
            .find { cat -> cat.name == it.category }
            ?: ExpenseCategory.OTHER
                ).name

    }.mapValues { it.value.sumOf { e -> e.amount } }

    val topCategory = categoryTotals.maxByOrNull { it.value }

    return topCategory?.let {
        "You spent the most on ${it.key} this period"
    } ?: "Track your expenses regularly to get better insights"
}

@Composable
fun SmartInsightAnimatedText(
    insight: String,
    color: Color
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(insight) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
    }

    Text(
        text = insight,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.graphicsLayer(alpha = animationProgress.value)
    )
}
