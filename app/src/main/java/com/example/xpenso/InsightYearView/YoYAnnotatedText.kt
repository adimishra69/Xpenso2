package com.example.xpenso.InsightYearView

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter

@Composable
fun YoYAnnotatedText(
    yoy: Double,
    totalPreviousYearExpense: Double,
    totalYearlyExpense: Double,
    currencySymbol: String,
    selectedYear: Int
) {
    val yoyColor: Color = when {
        totalPreviousYearExpense == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        yoy > 0 -> MaterialTheme.colorScheme.error
        else -> Color(0xFF4CAF50)
    }

    val yoyText = buildAnnotatedString {
        when {
            totalPreviousYearExpense == 0.0 -> {
                append("No data for ${selectedYear - 1}")
            }
            yoy > 150 -> {
                append("Spending increased significantly")
            }
            yoy > 0 -> {
                append("▲ ${String.format("%.1f", yoy)}% more than last year")
            }
            else -> {
                val decrease = totalPreviousYearExpense - totalYearlyExpense
                append("▼ Spending decreased by ")
                withStyle(
                    style = SpanStyle(
                        fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value - 2).sp,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    append(currencySymbol)
                }
                append(CurrencyFormatter.format(decrease, currencySymbol))
            }
        }
    }

    Text(
        text = yoyText,
        color = yoyColor,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
    )
}
