package com.example.xpenso.InsightMonthComponents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter

@Composable
fun AnimatedAmount(
    amount: Double,
    currencySymbol: String,
    duration: Int = 900
) {
    val animatedValue by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = duration),
        label = ""
    )

    Text(
        text = buildAnnotatedString {

            // currency (smaller)
            withStyle(
                SpanStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("$currencySymbol ")
            }

            // amount (reduced size)
            withStyle(
                SpanStyle(
                    fontSize = 16.sp,   // 🔽 donut center size
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(
                    CurrencyFormatter.format(
                        animatedValue.toDouble(),
                        currencySymbol
                    )
                )
            }
        },
        style = MaterialTheme.typography.titleMedium
    )
}
