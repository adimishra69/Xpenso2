package com.example.xpenso.InsightMonthComponents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.InsightsModel.CategoryPieData
import com.example.xpenso.Transaction.CategoryIcon
import com.example.xpenso.Transaction.ExpenseCategory
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────
//  Premium Category Legend
// ─────────────────────────────────────────────────────────────
fun normalizePercentages(data: List<CategoryPieData>): Map<String, Int> {
    if (data.isEmpty()) return emptyMap()

    // 🔥 special case (2 categories, one dominates)


    val raw = data.map { it.categoryName to (it.percentage * 100f) }

    val floored = raw.map { it.first to kotlin.math.floor(it.second).toInt() }

    var total = floored.sumOf { it.second }
    var remaining = 100 - total

    val sorted = raw
        .filter { it.second >= 1f }
        .sortedByDescending { it.second }

    val result = floored.associate { it.first to it.second }.toMutableMap()

    var i = 0
    while (remaining > 0 && i < sorted.size) {
        val key = sorted[i].first
        result[key] = result.getOrDefault(key, 0) + 1
        remaining--
        i++
    }

    return result
}
@Composable
fun CategoryLegend(
    data: List<CategoryPieData>,
    currencySymbol: String,
    onCategoryClick: (String) -> Unit
) {
    // ── Data logic (unchanged) ────────────────────────────────
    val normalItems = remember(data) { data.filter { (it.percentage * 100f) >= 1f } }
    val smallItems  = remember(data) { data.filter { (it.percentage * 100f) < 1f } }

//    val mergedOther: CategoryPieData? = remember(smallItems) {
//        if (smallItems.isEmpty()) null
//        else {
//            val totalAmount     = smallItems.sumOf { it.amount }
//            val totalPercentage = smallItems.sumOf { it.percentage.toDouble() }.toFloat()
//            CategoryPieData(
//                categoryName = "OTHER",
//                amount       = totalAmount,
//                percentage   = totalPercentage,
//                color        = Color(0xFF9E9E9E)
//            )
//        }
//    }
//
//    val displayItems = remember(normalItems, mergedOther) {
//        val combined = if (mergedOther != null) {
//            normalItems + mergedOther
//        } else {
//            normalItems
//        }
//
//        combined.sortedByDescending { it.amount }
//    }

    val displayItems = remember(data) {
        data.sortedByDescending { it.amount }
    }

    val normalizedMap = remember(displayItems) {
        normalizePercentages(displayItems)
    }
    // ── Animation: stagger each row ───────────────────────────
    val rowAnimations = displayItems.indices.map { remember { Animatable(0f) } }

    LaunchedEffect(data.size) {
        rowAnimations.forEach { it.snapTo(0f) }
        rowAnimations.forEachIndexed { index, anim ->
            delay(index * 60L)                    // stagger offset
            anim.animateTo(
                targetValue  = 1f,
                animationSpec = tween(durationMillis = 150)
            )
        }
    }

    // ── Layout ────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        displayItems.forEachIndexed { index, item ->
            val anim   = rowAnimations.getOrNull(index)?.value ?: 1f
            val isLast = index == displayItems.lastIndex

            LegendRow(
                item           = item,
                currencySymbol = currencySymbol,
                animProgress   = anim,
                percentageMap = normalizedMap,
                onClick        = { onCategoryClick(item.categoryName) }
            )

            // Subtle divider between rows, skip after last
            if (!isLast) {
                HorizontalDivider(
                    modifier  = Modifier
                        .padding(start = 55.dp , end = 10.dp)
                        .graphicsLayer(alpha = anim * 0.35f),
                         thickness = 0.4.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Single Legend Row
// ─────────────────────────────────────────────────────────────

@Composable
private fun LegendRow(
    item: CategoryPieData,
    currencySymbol: String,
    animProgress: Float,
    percentageMap: Map<String, Int>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                alpha           = animProgress,
                translationX    = (1f - animProgress) * 24f   // slide-in from left
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ── Colour swatch with inner glow ─────────────────────
        Box(contentAlignment = Alignment.Center) {
            // soft outer glow
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                item.color.copy(alpha = 0.35f),
                                item.color.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // solid swatch

                val category = ExpenseCategory.entries
                    .find { it.name == item.categoryName }
                    ?: ExpenseCategory.OTHER

                CategoryIcon(
                    category = category,
                    tint = item.color,
                    size = 22.dp
                )

        }

        Spacer(Modifier.width(14.dp))

        // ── Category name ─────────────────────────────────────
        Text(
            text       = item.categoryName.toDisplayUpper(),
            style      = MaterialTheme.typography.bodyMedium.copy(
                fontWeight  = FontWeight.Medium,
                letterSpacing = 0.4.sp,
                fontSize    = 13.sp
            ),
            modifier   = Modifier.weight(1f),
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
        )

        // ── Percentage pill ───────────────────────────────────
//        val normalized = percentageMap[item.categoryName] ?: 0
        val realPct = item.percentage * 100f

        val textValue = when {

            kotlin.math.abs(realPct - 100f) < 0.0001f -> {
                "100%"
            }

            realPct >= 1f -> {
                String.format("%.2f%%", realPct)
            }

            realPct >= 0.01f -> {
                String.format("%.2f%%", realPct)
            }

            else -> {
                "<0.01%"
            }
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = item.color.copy(alpha = 0.15f),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = textValue,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.SemiBold,
                    fontSize      = 11.sp,
                    letterSpacing = 0.2.sp
                ),
                color    = item.color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // ── Amount ────────────────────────────────────────────
        AnimatedAmount(
            amount         = item.amount,
            currencySymbol = currencySymbol
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Extension (unchanged)
// ─────────────────────────────────────────────────────────────

fun String.toDisplayUpper(): String = this.replace("_", " ").uppercase()