package com.example.xpenso.InsightMonthComponents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.Transaction.CategoryIcon
import kotlinx.coroutines.delay

data class OtherCategoryItem(
    val name: ExpenseCategory,
    val amount: Double,
    val color: Color
)

@Composable
fun OtherCategoryDialog(
    categories: List<OtherCategoryItem>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    // ── Percentages relative to this sub-group total ──────────
    val totalAmount = categories.sumOf { it.amount }.coerceAtLeast(0.0001)

    val sortedCategories = remember(categories) {
        categories.sortedByDescending { it.amount }
    }

    // ── Staggered row entrance animations ─────────────────────
    val rowAnimations = categories.indices.map { remember { Animatable(0f) } }
    LaunchedEffect(Unit) {
        rowAnimations.forEachIndexed { i, anim ->
            delay(i * 55L)
            anim.animateTo(1f, animationSpec = tween(250))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
        modifier         = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape            = RoundedCornerShape(24.dp),
        containerColor   = MaterialTheme.colorScheme.surface,
        tonalElevation   = 8.dp,

        // ── Title ─────────────────────────────────────────────
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = "Other Categories",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                )
                Text(
                    text  = "${categories.size} small categories · each < 1% of total",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        },

        // ── Body ──────────────────────────────────────────────
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Proportional colour strip across the top
                ProportionBar(categories = sortedCategories, totalAmount = totalAmount)

                Spacer(Modifier.height(16.dp))

                sortedCategories.forEachIndexed { index, item ->
                    val anim              = rowAnimations.getOrNull(index)?.value ?: 1f
                    val itemPct           = (item.amount / totalAmount * 100.0).toFloat()
                    val interactionSource = remember { MutableInteractionSource() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                alpha        = anim,
                                translationX = (1f - anim) * 20f
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = interactionSource,
                            ) {
                                onDismiss()
                                onCategoryClick(item.name.name)
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {

                        // ── Left: icon + name + mini bar ─────
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Tinted icon circle — same as main legend
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(42.dp)
                            ) {

                                // 🔥 outer glow
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    item.color.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                )

                                // 🔥 inner icon

                                CategoryIcon(
                                    category = item.name,   // 🔥 directly use enum,
                                    tint = item.color,
                                    size = 22.dp   // slightly bigger for dialog
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    text = item.name.name.toDisplayUpper(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight    = FontWeight.SemiBold,
                                        letterSpacing = 0.4.sp,
                                        fontSize      = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                                // Inline mini percentage bar
                                PercentageBar(
                                    percent = itemPct / 100f,
                                    color   = item.color
                                )
                            }
                        }

                        // ── Right: percentage pill + amount ──
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Percentage pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = item.color.copy(alpha = 0.13f)
                            ) {
                                Text(
                                    text     = "${"%.1f".format(itemPct)}%",
                                    style    = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight    = FontWeight.Bold,
                                        fontSize      = 10.sp,
                                        letterSpacing = 0.2.sp
                                    ),
                                    color    = item.color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Amount
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontSize   = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) { append("$currencySymbol ") }
                                    withStyle(
                                        SpanStyle(
                                            fontSize   = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) { append("%.0f".format(item.amount)) }
                                }
                            )
                        }
                    }

                    if (index != categories.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 58.dp , end = 10.dp),
                            color     = MaterialTheme.colorScheme.outline,
                            thickness = 0.42.dp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Close",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },

        confirmButton = {}
    )
}

// ─────────────────────────────────────────────────────────────
//  Proportional colour strip across the top of the dialog
//  Each segment width = that category's share of the sub-total
// ─────────────────────────────────────────────────────────────

@Composable
private fun ProportionBar(
    categories: List<OtherCategoryItem>,
    totalAmount: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(50))
    ) {
        categories.forEach { item ->
            val fraction = (item.amount / totalAmount).toFloat()
            Box(
                modifier = Modifier
                    .weight(fraction.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(item.color)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Tiny gradient bar under category name
//  Represents this category's share relative to sub-group total
// ─────────────────────────────────────────────────────────────

@Composable
private fun PercentageBar(
    percent: Float,   // 0f–1f
    color: Color
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(percent.coerceIn(0f, 1f))
                .background(
                    Brush.horizontalGradient(
                        listOf(color, color.copy(alpha = 0.55f))
                    )
                )
        )
    }
}