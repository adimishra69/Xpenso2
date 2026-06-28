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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.DateRangeUtils
import java.util.Calendar
import kotlin.math.abs

fun generateSmartInsight(
    currencySymbol: String,
    allExpenses: List<ExpenseEntity>,
    selectedMonth: Int,
    selectedYear: Int,
    startOfMonth: Int
): AnnotatedString {

    val thisRange = DateRangeUtils.getCycleRange(
        selectedYear,
        selectedMonth,
        startOfMonth
    )

    val lastRange = DateRangeUtils.getPreviousCycleRange(
        selectedYear,
        selectedMonth,
        startOfMonth
    )

    val thisMonthExpenses = allExpenses.filter { it.timestamp in thisRange }
    val lastMonthExpenses = allExpenses.filter { it.timestamp in lastRange }

    val thisTotal = thisMonthExpenses.sumOf { it.amount }
    val lastTotal = lastMonthExpenses.sumOf { it.amount }

    val totalExpense = thisMonthExpenses.sumOf { it.amount }

    val otherExpense = thisMonthExpenses
        .filter { it.category == ExpenseCategory.OTHER.name }
        .sumOf { it.amount }

    val otherPercent = if (totalExpense > 0) {
        (otherExpense / totalExpense) * 100
    } else 0.0


    if (thisMonthExpenses.isEmpty()) {
        return AnnotatedString("No expenses this period")
    }



    if (otherPercent >= 85) {
        return AnnotatedString("Most spending is uncategorized")
    }

    if (lastMonthExpenses.isEmpty() && thisTotal > 0.0) {
        return buildAnnotatedString {
            append("⚠️ No spending last period, ")

            withStyle(
                SpanStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("$currencySymbol")
            }

            withStyle(
                SpanStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(CurrencyFormatter.format(thisTotal, currencySymbol))
            }

            append(" this period")
        }
    }

    if (lastTotal < 100 && thisTotal > lastTotal) {
        return AnnotatedString("⚠️ Last period was low, comparison may be misleading")
    }

    if (lastTotal > 0) {
        val percentChange = ((thisTotal - lastTotal) / lastTotal) * 100
        val absPercent = abs(percentChange)

        if (percentChange < -30) {
            return AnnotatedString("Nice — spending down ${absPercent.toInt()}% compared to last period")
        }

        if (percentChange > 200) {
            return AnnotatedString("🚨 Spending spiked sharply")
        }

        if (percentChange > 30) {
            return AnnotatedString("⚠️ spending up ${absPercent.toInt()}%")
        }

        if (thisTotal > lastTotal && percentChange > 10) {
            return buildAnnotatedString {

                withStyle(
                    SpanStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("$currencySymbol")
                }

                withStyle(
                    SpanStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(
                        CurrencyFormatter.format(thisTotal - lastTotal, currencySymbol)
                    )
                }

                append(" more than last period")
            }
        }
    }

    val categoryTotals = thisMonthExpenses.groupBy {
        (ExpenseCategory.entries
            .find { cat -> cat.name == it.category }
            ?: ExpenseCategory.OTHER
                ).name
    }.mapValues { it.value.sumOf { e -> e.amount } }

    val topCategory = categoryTotals.maxByOrNull { it.value }

    return topCategory?.let { (categoryName, _) ->

        val displayName = if (categoryName == ExpenseCategory.OTHER.name) {
            "Uncategorized (Other)"
        } else {
            categoryName
                .replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        }

        AnnotatedString("Top spending: $displayName")

    } ?: AnnotatedString("Add more expenses for better insights")
}

@Composable
fun SmartInsightAnimatedText(
    insight: AnnotatedString,
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
