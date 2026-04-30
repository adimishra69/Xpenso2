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
    // Step 1: only draw slices >= 1%
    val visibleSlices = remember(data) {
        data.filter { (it.percentage * 100f) >= 1f }
    }

    // Step 2: rescale visible slices so they sum to exactly 1.0
    // This ensures they fill the full ring with no leftover gap,
    // regardless of how much the dropped slices contributed.
    // e.g. ELECTRONICS 90.0% + SHOPPING 9.0% = 99.0% visible
    // rescaled: ELECTRONICS 90.9% + SHOPPING 9.1% → fills 100% of ring
    val rescaledSlices = remember(visibleSlices) {
        val visibleTotal = visibleSlices.sumOf { it.percentage.toDouble() }.toFloat()
        if (visibleTotal <= 0f) emptyList()
        else visibleSlices.map { it.copy(percentage = it.percentage / visibleTotal) }
    }

    // Step 3: dominant = only 1 slice visible after filter
    val isDominant = rescaledSlices.size == 1
    val dominantSlice = if (isDominant) rescaledSlices.firstOrNull() else null

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(rescaledSlices.size) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .padding(16.dp)
        ) {
            val strokeWidth = 80f

            if (isDominant) {
                // Single slice — perfect unbroken ring, no gap possible
                drawArc(
                    color = dominantSlice!!.color,
                    startAngle = -90f,
                    sweepAngle = 360f * animationProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                val totalSegments = rescaledSlices.size
                val gapAngle = if (totalSegments > 1) 1f else 0f
                // Gap budget is based on visible slice count only
                val effectiveTotalAngle = 360f - (totalSegments * gapAngle)
                var startAngle = -90f

                rescaledSlices.forEach { segment ->
                    val sweepAngle =
                        segment.percentage * effectiveTotalAngle * animationProgress.value
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweepAngle + gapAngle
                }
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