package com.example.xpenso.Budget


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter

@Composable
fun BudgetAmountText(
    remaining: Double,
    budget: Double,
    currencySymbol: String
) {
    val isOverBudget = budget > 0 && remaining < 0

    Text(
        text = buildAnnotatedString {

            // currency symbol → smaller & lighter
            withStyle(
                SpanStyle(
                    fontSize = (MaterialTheme.typography.titleMedium.fontSize.value - 2).sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverBudget)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                append(currencySymbol)
                append(" ")
            }

            // amount → normal size & bold
            withStyle(
                SpanStyle(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverBudget)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            ) {
                append(
                    CurrencyFormatter.format(
                        amount = remaining,
                        currencySymbol = currencySymbol
                    )
                )
            }
        }
    )
}
