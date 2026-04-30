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

/**
 * Renders a currency amount with a smaller, lighter symbol prefix.
 *
 * [AmountTextStyle.SPENT]  — medium size, used for secondary/inline values
 * [AmountTextStyle.BUDGET] — large size, used as the primary headline figure
 */
@Composable
fun AmountText(
    amount: Double,
    currencySymbol: String,
    style: AmountTextStyle
) {
    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme

    // ── Size scale ───────────────────────────────────────────────────────────
    val amountFontSize: TextUnit
    val symbolFontSize: TextUnit
    val amountFontWeight: FontWeight
    val symbolColor = colorScheme.onSurfaceVariant
    val amountColor = colorScheme.onSurface

    when (style) {
        AmountTextStyle.SPENT -> {
            amountFontSize = typography.titleMedium.fontSize
            symbolFontSize = (typography.titleMedium.fontSize.value - 3).sp
            amountFontWeight = FontWeight.SemiBold
        }
        AmountTextStyle.BUDGET -> {

            val formattedAmount = CurrencyFormatter.format(
                amount = amount,
                currencySymbol = currencySymbol
            )

            val digitCount = formattedAmount
                .replace("[^0-9]".toRegex(), "")
                .length

            val baseSize = typography.headlineSmall.fontSize

            amountFontSize =
                if (digitCount > 9)
                    (baseSize.value - 3).sp
                else
                    baseSize

            symbolFontSize =
                if (digitCount > 9)
                    (baseSize.value - 9).sp
                else
                    (baseSize.value - 6).sp

            amountFontWeight = FontWeight.Bold
        }
    }

    // ── Rendered text ────────────────────────────────────────────────────────
    Text(
        text = buildAnnotatedString {

            // Currency symbol — smaller + muted, with a narrow gap after
            withStyle(
                SpanStyle(
                    fontSize = symbolFontSize,
                    fontWeight = FontWeight.Medium,
                    color = symbolColor,
                    letterSpacing = 0.sp
                )
            ) {
                append(currencySymbol)
                append("\u2009") // thin space for tighter symbol–number gap
            }

            // Amount — bold, primary colour, tight letter-spacing
            withStyle(
                SpanStyle(
                    fontSize = amountFontSize,
                    fontWeight = amountFontWeight,
                    color = amountColor,
                    letterSpacing = (-0.5).sp
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