package com.example.xpenso.Budget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.Transaction.CategoryIcon
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────────────────────

data class MonthlyBudgetData(
    val month: Int,        // 0-based: 0 = January
    val year: Int,
    val budget: Double,
    val spent: Double
) {
    val remaining: Double get() = budget - spent
    val isOverBudget: Boolean get() = budget > 0 && spent > budget
    val isUnderBudget: Boolean get() = budget > 0 && spent <= budget
    val savings: Double get() = if (isUnderBudget) remaining else 0.0
}

data class YearlyInsights(
    val year: Int,
    val monthlyData: List<MonthlyBudgetData>,
    // category spending map: ExpenseCategory.name -> total spent
    val categorySpending: Map<String, Double> = emptyMap()
) {
    val totalYearlyBudget: Double get() = monthlyData.sumOf { it.budget }
    val totalYearlySpending: Double get() = monthlyData.sumOf { it.spent }
    val remainingBudget: Double get() = totalYearlyBudget - totalYearlySpending
    val budgetUsagePercentage: Float
        get() = if (totalYearlyBudget > 0)
            (totalYearlySpending / totalYearlyBudget * 100f).toFloat().coerceIn(0f, 999f)
        else 0f
    val averageMonthlySpending: Double
        get() {
            val active = monthlyData.filter { it.spent > 0 }
            return if (active.isEmpty()) 0.0 else active.sumOf { it.spent } / active.size
        }
    val highestSpendingMonth: MonthlyBudgetData?
        get() = monthlyData.filter { it.spent > 0 }.maxByOrNull { it.spent }
    val lowestSpendingMonth: MonthlyBudgetData?
        get() = monthlyData.filter { it.spent > 0 }.minByOrNull { it.spent }
    val overBudgetMonths: List<MonthlyBudgetData> get() = monthlyData.filter { it.isOverBudget }
    val underBudgetMonths: List<MonthlyBudgetData> get() = monthlyData.filter { it.isUnderBudget }
    val bestBudgetControlMonth: MonthlyBudgetData?
        get() = monthlyData.filter { it.isUnderBudget && it.budget > 0 }.maxByOrNull { it.savings }
    val averageDailySpending: Double get() = totalYearlySpending / 365.0

    // Top spending category using ExpenseCategory enum
    val topSpendingCategory: ExpenseCategory?
        get() {
            val entry = categorySpending.maxByOrNull { it.value } ?: return null
            return ExpenseCategory.entries.find { it.name == entry.key } ?: ExpenseCategory.OTHER
        }

    // Most overspent category — highest spend relative to its "slot"
    val mostOverspentCategory: ExpenseCategory?
        get() {
            val entry = categorySpending.maxByOrNull { it.value } ?: return null
            return ExpenseCategory.entries.find { it.name == entry.key } ?: ExpenseCategory.OTHER
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private val MONTH_ABBR = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private fun monthName(index: Int) = MONTH_ABBR.getOrElse(index) { "—" }

private fun formatAmountShort(amount: Double, symbol: String): String = when {
    amount >= 10_00_000 -> "$symbol${String.format("%.1f", amount / 10_00_000)}L"
    amount >= 1_000 -> "$symbol${String.format("%.1f", amount / 1_000)}K"
    else -> "$symbol${amount.roundToInt()}"
}

private fun formatAmountFull(amount: Double, symbol: String): String {
    val abs = kotlin.math.abs(amount)
    val formatted = String.format("%,.0f", abs)
    return if (amount < 0) "-$symbol$formatted" else "$symbol$formatted"
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun YearlyBudgetInsightsScreen(
    insights: YearlyInsights,
    currencySymbol: String,
    selectedYear: Int,
    onPrevYear: () -> Unit,
    onNextYear: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Year navigation
        YearNavigationBar(
            year = selectedYear,
            onPrev = onPrevYear,
            onNext = onNextYear
        )

        // 2. Hero summary card
        YearlySummaryHeroCard(insights = insights, currencySymbol = currencySymbol)

        // 3. Bar chart
        MonthlyBarChartCard(insights = insights, currencySymbol = currencySymbol)

        // 4. Stats 2x2 grid
        StatsGrid(insights = insights, currencySymbol = currencySymbol)

        // 5. Highest / Lowest month highlights
        MonthHighlightsRow(insights = insights, currencySymbol = currencySymbol)

        // 6. Over-budget / Under-budget control card
        BudgetControlSummaryCard(insights = insights, currencySymbol = currencySymbol)

        // 7. Daily spending + top category row
        BottomInsightsRow(insights = insights, currencySymbol = currencySymbol)

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Year Navigation Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun YearNavigationBar(year: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrev, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous Year",
                    modifier = Modifier.size(23.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Yearly Overview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next Year",
                    modifier = Modifier.size(23.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Hero Summary Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun YearlySummaryHeroCard(insights: YearlyInsights, currencySymbol: String) {
    val usagePct = insights.budgetUsagePercentage
    val animatedProgress by animateFloatAsState(
        targetValue = (usagePct / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "yearlyProgress"
    )

    val progressColor = when {
        usagePct >= 100f -> MaterialTheme.colorScheme.error
        usagePct >= 80f -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusText = when {
        insights.totalYearlyBudget <= 0 -> "No budget set"
        usagePct >= 100f -> "Over budget"
        usagePct >= 80f -> "Approaching limit"
        else -> "On track"
    }

    val statusColor = when {
        insights.totalYearlyBudget <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        usagePct >= 100f -> MaterialTheme.colorScheme.error
        usagePct >= 80f -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 24.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${insights.year}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Total budget headline
            Text(
                text = "Total Yearly Budget",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatAmountFull(insights.totalYearlyBudget, currencySymbol),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(20.dp))

            // Spent / Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Spent
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatAmountFull(insights.totalYearlySpending, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (usagePct >= 100f) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                // Remaining
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatAmountFull(insights.remainingBudget, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (insights.remainingBudget < 0) MaterialTheme.colorScheme.error
                        else Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = statusColor
                )
                Text(
                    text = "${usagePct.roundToInt()}% used",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Monthly Bar Chart Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthlyBarChartCard(insights: YearlyInsights, currencySymbol: String) {
    val animatedBars by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "barAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget vs Spent",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), label = "Budget")
                    LegendDot(color = MaterialTheme.colorScheme.primary, label = "Spent")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Chart
            val data = insights.monthlyData
            val maxVal = data.maxOf { maxOf(it.budget, it.spent) }.takeIf { it > 0 } ?: 1.0

            val budgetColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            val spentColor = MaterialTheme.colorScheme.primary
            val overColor = MaterialTheme.colorScheme.error
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                drawBarChart(
                    data = data,
                    maxVal = maxVal,
                    animationProgress = animatedBars,
                    budgetColor = budgetColor,
                    spentColor = spentColor,
                    overColor = overColor,
                    surfaceVariantColor = surfaceVariantColor
                )
            }

            Spacer(Modifier.height(8.dp))

            // Month labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                insights.monthlyData.forEach { m ->
                    Text(
                        text = monthName(m.month).take(1),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBarChart(
    data: List<MonthlyBudgetData>,
    maxVal: Double,
    animationProgress: Float,
    budgetColor: Color,
    spentColor: Color,
    overColor: Color,
    surfaceVariantColor: Color
) {
    if (data.isEmpty()) return

    val chartHeight = size.height
    val chartWidth = size.width
    val barGroupWidth = chartWidth / data.size
    val barWidth = barGroupWidth * 0.35f
    val gap = barWidth * 0.15f
    val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

    data.forEachIndexed { index, month ->
        val groupLeft = index * barGroupWidth
        val centerX = groupLeft + barGroupWidth / 2f

        // Budget bar (left)
        val budgetHeight = if (maxVal > 0)
            ((month.budget / maxVal) * chartHeight * animationProgress).toFloat()
        else 0f

        val budgetLeft = centerX - barWidth - gap / 2f
        if (budgetHeight > 0) {
            drawRoundRect(
                color = budgetColor,
                topLeft = Offset(budgetLeft, chartHeight - budgetHeight),
                size = Size(barWidth, budgetHeight),
                cornerRadius = cornerRadius
            )
        }

        // Spent bar (right)
        val spentHeight = if (maxVal > 0)
            ((month.spent / maxVal) * chartHeight * animationProgress).toFloat()
        else 0f

        val spentLeft = centerX + gap / 2f
        val color = if (month.isOverBudget) overColor else spentColor
        if (spentHeight > 0) {
            drawRoundRect(
                color = color,
                topLeft = Offset(spentLeft, chartHeight - spentHeight),
                size = Size(barWidth, spentHeight),
                cornerRadius = cornerRadius
            )
        }

        // Baseline
        drawLine(
            color = surfaceVariantColor,
            start = Offset(groupLeft + 4.dp.toPx(), chartHeight),
            end = Offset(groupLeft + barGroupWidth - 4.dp.toPx(), chartHeight),
            strokeWidth = 0.8.dp.toPx()
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Stats Grid  (2 × 2)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsGrid(insights: YearlyInsights, currencySymbol: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.BarChart,
                label = "Avg Monthly Spend",
                value = formatAmountShort(insights.averageMonthlySpending, currencySymbol),
                iconColor = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Today,
                label = "Avg Daily Spend",
                value = formatAmountShort(insights.averageDailySpending, currencySymbol),
                iconColor = Color(0xFF9C27B0)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.TrendingUp,
                label = "Budget Usage",
                value = "${insights.budgetUsagePercentage.roundToInt()}%",
                iconColor = when {
                    insights.budgetUsagePercentage >= 100f -> MaterialTheme.colorScheme.error
                    insights.budgetUsagePercentage >= 80f -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Savings,
                label = "Total Savings",
                value = formatAmountShort(
                    insights.underBudgetMonths.sumOf { it.savings },
                    currencySymbol
                ),
                iconColor = Color(0xFF009688)
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Month Highlights Row  (Highest / Lowest)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthHighlightsRow(insights: YearlyInsights, currencySymbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MonthHighlightCard(
            modifier = Modifier.weight(1f),
            title = "Highest Spend",
            month = insights.highestSpendingMonth,
            currencySymbol = currencySymbol,
            icon = Icons.Outlined.ArrowUpward,
            iconBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
            iconColor = MaterialTheme.colorScheme.error,
            amountColor = MaterialTheme.colorScheme.error
        )
        MonthHighlightCard(
            modifier = Modifier.weight(1f),
            title = "Lowest Spend",
            month = insights.lowestSpendingMonth,
            currencySymbol = currencySymbol,
            icon = Icons.Outlined.ArrowDownward,
            iconBg = Color(0xFF4CAF50).copy(alpha = 0.12f),
            iconColor = Color(0xFF4CAF50),
            amountColor = Color(0xFF4CAF50)
        )
    }
}

@Composable
private fun MonthHighlightCard(
    modifier: Modifier = Modifier,
    title: String,
    month: MonthlyBudgetData?,
    currencySymbol: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconColor: Color,
    amountColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (month != null) {
                Text(
                    text = monthName(month.month),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatAmountFull(month.spent, currencySymbol),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
            } else {
                Text(
                    text = "No data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. Budget Control Summary Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BudgetControlSummaryCard(insights: YearlyInsights, currencySymbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Budget Control",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Over vs Under row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BudgetControlChip(
                    modifier = Modifier.weight(1f),
                    count = insights.overBudgetMonths.size,
                    label = "Over-budget\nmonths",
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    textColor = MaterialTheme.colorScheme.error,
                    countColor = MaterialTheme.colorScheme.error,
                    icon = Icons.Outlined.Warning
                )
                BudgetControlChip(
                    modifier = Modifier.weight(1f),
                    count = insights.underBudgetMonths.size,
                    label = "Under-budget\nmonths",
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.10f),
                    textColor = Color(0xFF4CAF50),
                    countColor = Color(0xFF4CAF50),
                    icon = Icons.Outlined.CheckCircle
                )
            }

            // Best budget control month
            val best = insights.bestBudgetControlMonth
            if (best != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.8.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFFFFD700).copy(alpha = 0.15f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Best Budget Control",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = monthName(best.month),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatAmountFull(best.savings, currencySymbol),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Over-budget months pills
            if (insights.overBudgetMonths.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.8.dp
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Overspent months",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        insights.overBudgetMonths.forEach { m ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = monthName(m.month),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetControlChip(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    containerColor: Color,
    textColor: Color,
    countColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = countColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. Bottom Insights Row  (Daily spend + Top Category)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomInsightsRow(insights: YearlyInsights, currencySymbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Daily spending card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Text(
                    text = "Daily Avg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatAmountFull(insights.averageDailySpending, currencySymbol),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "per day this year",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Top category card
        val topCat = insights.topSpendingCategory
        val topCatSpent = if (topCat != null)
            insights.categorySpending[topCat.name] ?: 0.0
        else 0.0

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (topCat != null) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                topCat.color.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // icon is nullable — INVESTMENT uses a drawable instead
                        CategoryIcon(
                            category = topCat,
                            tint = topCat.color,
                            size = 17.dp
                        )
                    }
                    Text(
                        text = "Top Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        // ExpenseCategory has no displayName — derive from enum .name
                        text = topCat.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatAmountFull(topCatSpent, currencySymbol),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = topCat.color
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Text(
                        text = "Top Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}