package com.example.xpenso.InsightMonthComponents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
    // Float max precision is ~7 digits — animating 255,000,025f rounds to
    // 255,000,032f. Fix: never animate the value itself. Animate opacity only,
    // always render the exact Double value with no precision loss.
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(amount) {
        alpha.snapTo(0f)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration)
        )
    }

    Text(
        modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("$currencySymbol ")
            }
            withStyle(
                SpanStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                // Exact Double — no float cast, no precision loss ever
                append(CurrencyFormatter.format(amount, currencySymbol))
            }
        },
        style = MaterialTheme.typography.titleMedium
    )
}