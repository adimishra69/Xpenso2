package com.example.xpenso.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color

@Composable
fun CurrencyText(
    amount: Double,
    currencySymbol: String,
    color: Color,
) {

    Text(
        text = buildAnnotatedString {

            // currency symbol (-2sp)
            withStyle(
                SpanStyle(
                    fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value - 2).sp
                )
            ) {
                append("$currencySymbol ")
            }

            // amount
            withStyle(
                SpanStyle(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            ) {
                append(
                    CurrencyFormatter.format(
                        amount,
                        currencySymbol
                    )
                )
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = color
    )
}