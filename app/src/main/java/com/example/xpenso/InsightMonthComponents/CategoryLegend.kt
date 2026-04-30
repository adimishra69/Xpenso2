package com.example.xpenso.InsightMonthComponents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.xpenso.InsightsModel.CategoryPieData

@Composable
fun CategoryLegend(
    data: List<CategoryPieData>,
    currencySymbol: String,
    onCategoryClick: (String) -> Unit
) {
    // Split into visible (>= 1%) and small (< 1%)
    val normalItems = remember(data) { data.filter { (it.percentage * 100f) >= 1f } }
    val smallItems  = remember(data) { data.filter { (it.percentage * 100f) < 1f } }

    // Merge all small items into one OTHER row
    val mergedOther: CategoryPieData? = remember(smallItems) {
        if (smallItems.isEmpty()) null
        else {
            val totalAmount     = smallItems.sumOf { it.amount }
            val totalPercentage = smallItems.sumOf { it.percentage.toDouble() }.toFloat()
            CategoryPieData(
                categoryName = "OTHER",
                amount       = totalAmount,
                percentage   = totalPercentage,
                color        = Color(0xFF9E9E9E) // neutral grey for merged OTHER
            )
        }
    }

    // Final display list: normal items + merged OTHER at the bottom (if any)
    val displayItems = remember(normalItems, mergedOther) {
        if (mergedOther != null) normalItems + mergedOther else normalItems
    }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data.size) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        displayItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(alpha = animationProgress.value)
                    .clickable { onCategoryClick(item.categoryName) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(item.color, RoundedCornerShape(2.dp))
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = item.categoryName.toDisplayUpper(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                // Exact percentage always — no misleading rounding


                Spacer(Modifier.width(16.dp))

                AnimatedAmount(
                    amount = item.amount,
                    currencySymbol = currencySymbol
                )
            }
        }
    }
}

fun String.toDisplayUpper(): String {
    return this.replace("_", " ").uppercase()
}