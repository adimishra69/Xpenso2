package com.example.xpenso.Home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter

@Composable
fun DailyExpenseCard(
    currencySymbol: String,
    dailyTotal: Double
) {
    // Animate a 0..1 progress value, then interpolate using Double math.
    // This preserves full 9-digit precision — no Float cast on the amount itself.
    // Animation only triggers when dailyTotal actually changes.
    val progress = remember { Animatable(0f) }
    var previousTotal by remember { mutableDoubleStateOf(dailyTotal) }
    var startTotal by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(dailyTotal) {
        startTotal = previousTotal
        previousTotal = dailyTotal
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    // Interpolate between old and new value using Double — no Float precision loss
    val displayAmount = startTotal + (dailyTotal - startTotal) * progress.value

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 18.dp
            )
        ) {
            Text(
                text = "Spent Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                val baseStyle1 = MaterialTheme.typography.displaySmall

                Text(
                    text = currencySymbol,
                    fontSize = (baseStyle1.fontSize.value * 0.65f).sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = CurrencyFormatter.format(displayAmount, currencySymbol),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}