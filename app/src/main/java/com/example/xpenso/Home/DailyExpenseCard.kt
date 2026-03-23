package com.example.xpenso.Home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter
@Composable
fun DailyExpenseCard(
    currencySymbol: String,
    dailyTotal: Double
) {

    // -------- Smooth number counting --------
    val animatedAmount by androidx.compose.animation.core.animateFloatAsState(
        targetValue = dailyTotal.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
        label = "DailyAmountAnim"
    )

    // -------- Subtle scale pulse when value changes --------
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(dailyTotal) {
        scale.animateTo(
            1.05f,
            animationSpec = tween(150)
        )
        scale.animateTo(
            1f,
            animationSpec = tween(350)
        )
    }


    Card(
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                alpha = 0.98f
            },
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

                val baseStyle = MaterialTheme.typography.displaySmall

                // Currency symbol
                Text(
                    text = currencySymbol,
                    fontSize = (baseStyle.fontSize.value * 0.65f).sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.width(8.dp))

                // -------- Animated amount --------
                Text(
                    text = CurrencyFormatter.format(animatedAmount.toDouble(), currencySymbol),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
