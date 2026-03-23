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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.xpenso.InsightsModel.CategoryPieData

@Composable
fun CategoryLegend(
    data: List<CategoryPieData>,
    currencySymbol: String,
    onCategoryClick: (String) -> Unit // New callback parameter
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
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
        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(alpha = animationProgress.value)
                    .clickable { onCategoryClick(item.categoryName) } // Added clickable modifier
                    .padding(vertical = 4.dp), // Added padding for better hit target
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Square
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(item.color, RoundedCornerShape(2.dp))
                )

                Spacer(Modifier.width(12.dp))

                // Category Name
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                // Percentage
                Text(
                    text = "${(item.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.End
                )

                Spacer(Modifier.width(16.dp))

                // Amount
                AnimatedAmount(
                    amount = item.amount,
                    currencySymbol = currencySymbol
                )
            }
        }
    }
}
