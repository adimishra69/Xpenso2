package com.example.xpenso.InsightMonthComponents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.InsightsModel.MonthlyTrendData
import com.example.xpenso.utils.CurrencyCompactFormatter


@Composable
fun SimpleBarChart(
    data: List<MonthlyTrendData>,
    selectedIndex: Int,
    currencySymbol: String,
    onMonthClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        // Handle scroll
        if (data.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        }

        // Handle animation - 900ms to match pie chart
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
    }

    val maxAmount =
        (data.maxByOrNull { it.amount }?.amount ?: 1.0)
            .coerceAtLeast(1.0)

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, item ->

            val barHeightFactor =
                (item.amount / maxAmount).toFloat()

            val isSelected = index == selectedIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(60.dp)
                    .graphicsLayer(alpha = animationProgress.value) // Smooth fade in

            ) {

                if (item.amount > 0) {
                    Text(
                        text = CurrencyCompactFormatter.format(
                            item.amount,
                            currencySymbol
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color =
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text("", fontSize = 10.sp)
                }

                Spacer(Modifier.height(4.dp))

                val maxBarHeight = 160.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(
                            (maxBarHeight * barHeightFactor * animationProgress.value) // Animate height
                                .coerceAtLeast(4.dp)
                        )
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(.9f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                        .clickable { onMonthClick(index) }
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight =
                        if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

//@Preview(showBackground = true , showSystemUi = true)
//@Composable
//fun SimpleBarChartPreview() {
//
//    val dummyData = listOf(
//        MonthlyTrendData("Jan", 2000.0, 1, 2026),
//        MonthlyTrendData("Feb", 3500.0, 2, 2026),
//        MonthlyTrendData("Mar", 1800.0, 3, 2026),
//        MonthlyTrendData("Apr", 4200.0, 4, 2026),
//        MonthlyTrendData("May", 1500.0, 5, 2026)
//    )
//
//    SimpleBarChart(
//        data = dummyData,
//        selectedIndex = 0,
//        currencySymbol = "₹",
//        onMonthClick = {}
//    )
//}