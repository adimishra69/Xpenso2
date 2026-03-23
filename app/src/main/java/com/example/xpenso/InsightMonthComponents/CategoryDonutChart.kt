package com.example.xpenso.InsightMonthComponents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.xpenso.InsightsModel.CategoryPieData


@Composable
fun CategoryDonutChart(
    data: List<CategoryPieData>,
    totalAmount: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp), // Increased height to accommodate larger donut
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(260.dp) // Increased size of the Canvas
                .padding(16.dp)
        ) {
            var startAngle = -90f
            val strokeWidth = 80f // Increased stroke width for a thicker donut
            val gapAngle = 1f // Degrees for the gap between segments

            val totalSegments = data.size
            val totalGapSpace = if (totalSegments > 1) totalSegments * gapAngle else 0f // Apply gaps only if there's more than one segment

            // Calculate the effective total angle available for all segments to be drawn
            val effectiveTotalAngle = 360f - totalGapSpace

            data.forEach { segment ->
                // Calculate the sweep angle for the current segment, proportional to the effective total angle
                val sweepAngleForSegment = segment.percentage * effectiveTotalAngle * animationProgress.value

                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngleForSegment,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                // Move the start angle for the next segment, accounting for its sweep angle and the gap
                startAngle += sweepAngleForSegment + gapAngle
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedAmount(
                amount = totalAmount,
                currencySymbol = currencySymbol,
            )
        }

    }
}