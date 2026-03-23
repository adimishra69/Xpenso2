package com.example.xpenso.InsightYearView

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter

@Composable
fun TotalYearlyExpenseCard(
    currencySymbol: String,
    totalExpense: Double,
    year: Int
) {
    // 🔹 Fade-in animation progress
    val animationProgress = remember { Animatable(20f) }

    val visible = totalExpense >= 0

    val animatedOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = ""
    )

    val animatedElevation by animateFloatAsState(
        targetValue = if (visible) 6f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = animatedOffset
                alpha = animatedAlpha
            },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Total Spent in $year",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            val baseStyle = MaterialTheme.typography.displayMedium

            Text(
                text = buildAnnotatedString {

                    // Currency symbol — smaller & lighter
                    withStyle(
                        SpanStyle(
                            fontSize = (baseStyle.fontSize.value - 14).sp,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(currencySymbol)
                        append(" ")
                    }

                    // Amount — full size & extra bold
                    withStyle(
                        SpanStyle(
                            fontSize = (baseStyle.fontSize.value - 6).sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.1.sp
                        )
                    ) {
                        append(
                            CurrencyFormatter.format(totalExpense, currencySymbol)
                        )
                    }
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
