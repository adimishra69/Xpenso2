package com.example.xpenso.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter

enum class AmountTextStyle {
    SPENT,
    BUDGET
}

@Composable
fun AmountText(
    amount: Double,
    currencySymbol: String,
    style: AmountTextStyle
) {
    val typography = MaterialTheme.typography

    // choose sizes based on style
    val amountFontSize: TextUnit
    val symbolFontSize: TextUnit

    when (style) {
        AmountTextStyle.SPENT -> {
            amountFontSize = typography.titleMedium.fontSize
            symbolFontSize = (typography.titleMedium.fontSize.value - 2).sp
        }

        AmountTextStyle.BUDGET -> {
            amountFontSize = typography.headlineSmall.fontSize
            symbolFontSize = (typography.headlineSmall.fontSize.value - 4).sp
        }
    }

    Text(
        text = buildAnnotatedString {

            // currency symbol (smaller + lighter)
            withStyle(
                SpanStyle(
                    fontSize = symbolFontSize,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                append(currencySymbol)
                append(" ")
            }

            // amount (bold)
            withStyle(
                SpanStyle(
                    fontSize = amountFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append(
                    CurrencyFormatter.format(
                        amount = amount,
                        currencySymbol = currencySymbol
                    )
                )
            }
        }
    )
}
