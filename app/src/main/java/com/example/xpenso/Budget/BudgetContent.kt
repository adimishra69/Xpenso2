package com.example.xpenso.Budget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.Viewmodel.ExpenseViewModel
import com.example.xpenso.ui.components.AmountText
import com.example.xpenso.ui.components.AmountTextStyle
import com.example.xpenso.ui.components.BudgetStatusCard
import com.example.xpenso.ui.components.CommonNoBudgetView
import com.example.xpenso.utils.CurrencyFormatter
import java.util.Calendar


@Composable
fun BudgetContent(
    currencySymbol: String,
    viewMonth: Calendar,
    selectedMonthYearDisplay: String,
    budget: Double,
    spentInSelectedMonth: Double,
    remaining: Double,
    usagePercentage: Float,
    displayPercentage: Int,
    percentageText: String,
    budgetStatusText: String,
    budgetStatusColor: Color,
    progressColor: Color,
    showDialog: Boolean,
    isNext: Boolean,
    onMonthClick: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onEditBudget: () -> Unit,
    onSetBudget: () -> Unit,
    expenseViewModel: ExpenseViewModel
) {
    val scrollState = rememberScrollState()

    val visible = budget >= 0

    val animatedOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "amountOffset"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "amountAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        // ─────────────────────────────────────────────────────────────────────
        // Month Navigation  ★ Premium pill style
        // ─────────────────────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(
                0.7.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
            ),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Prev button — subtle tonal pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        0.6.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                    )
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.69f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            onMonthClick()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    AnimatedContent(
                        targetState = selectedMonthYearDisplay,
                        transitionSpec = {
                            if (isNext) {
                                slideInVertically { it } + fadeIn() togetherWith
                                        slideOutVertically { -it } + fadeOut()
                            } else {
                                slideInVertically { -it } + fadeIn() togetherWith
                                        slideOutVertically { it } + fadeOut()
                            }
                        }
                    ) { targetText ->

                        Text(
                            text = targetText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // Next button — subtle tonal pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        0.6.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                    )
                ) {
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────────────────────
        // Budget Main Card  ★ Completely redesigned — two premium states
        // ─────────────────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = budget > 0.0,
            transitionSpec = {
                val enter = fadeIn(tween(350, delayMillis = 60)) +
                        slideInVertically(tween(380, delayMillis = 60)) { it / 14 }
                val exit = fadeOut(tween(200)) +
                        slideOutVertically(tween(220)) { -it / 14 }
                enter.togetherWith(exit).using(SizeTransform(clip = false))
            },
            label = "BudgetCardState"
        ) { isBudgetSet ->

            if (isBudgetSet) {
                // ══════════════════════════════════════════════════════════════
                // STATE A — Budget IS set  ★ Rich data card
                // ══════════════════════════════════════════════════════════════
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 22.dp, end = 22.dp, top = 20.dp, bottom = 24.dp
                        )
                    ) {

                        // ── Header row: month pill + edit button ──────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Month badge with status dot
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                border = BorderStroke(
                                    0.7.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                color = progressColor.copy(alpha = 0.85f),
                                                shape = CircleShape
                                            )
                                    )
                                    AnimatedContent(
                                        targetState = selectedMonthYearDisplay,
                                        transitionSpec = {
                                            if (isNext) {
                                                slideInVertically { it } + fadeIn() togetherWith
                                                        slideOutVertically { -it } + fadeOut()
                                            } else {
                                                slideInVertically { -it } + fadeIn() togetherWith
                                                        slideOutVertically { it } + fadeOut()
                                            }
                                        }
                                    ) { targetText ->

                                        Text(
                                            text = targetText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.4.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Edit button
                            OutlinedButton(
                                onClick = onEditBudget,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.2.sp,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Budget headline amount ────────────────────────────
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(3.dp))
                        AmountText(
                            amount = budget,
                            currencySymbol = currencySymbol,
                            style = AmountTextStyle.BUDGET
                        )

                        Spacer(Modifier.height(22.dp))

                        // ── Divider line (subtle) ─────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.7.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.54f)
                                )
                        )

                        Spacer(Modifier.height(18.dp))

                        // ── Spent / Remaining rows ────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    translationY = animatedOffset
                                    alpha = animatedAlpha
                                },
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Spent row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Color accent dot
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = progressColor.copy(alpha = 0.75f),
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = "Spent",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AmountText(
                                    amount = spentInSelectedMonth,
                                    currencySymbol = currencySymbol,
                                    style = AmountTextStyle.SPENT
                                )
                            }

                            // Remaining row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.69f),
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = "Remaining",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                BudgetAmountText(
                                    remaining = remaining,
                                    budget = budget,
                                    currencySymbol = currencySymbol
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Progress bar ──────────────────────────────────────
                        LinearProgressIndicator(
                            progress = { usagePercentage.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {}
                        )

                        Spacer(Modifier.height(10.dp))

                        // ── Percentage + status row ───────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = budgetStatusText,
                                color = budgetStatusColor,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = percentageText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

            } else {
                // ══════════════════════════════════════════════════════════════
                // STATE B — Budget NOT set  ★ Elegant empty-state card
                // ══════════════════════════════════════════════════════════════
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 22.dp, end = 22.dp, top = 20.dp, bottom = 26.dp
                            )
                    ) {

                        // ── Header: month pill only ───────────────────────────
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = selectedMonthYearDisplay,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.4.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(26.dp))

                        // ── Big wallet icon in a subtle tonal circle ──────────
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape = CircleShape
                                )
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        // ── Title + subtitle centered ─────────────────────────
                        Text(
                            text = "No budget set",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Set a monthly budget to track your\nspending and stay in control",
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        // ── Spent row (even without budget, show what was spent) ─
                        AnimatedVisibility(
                            visible = spentInSelectedMonth > 0.0,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.7.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                        )
                                )
                                Spacer(Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Spent this month",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AmountText(
                                        amount = spentInSelectedMonth,
                                        currencySymbol = currencySymbol,
                                        style = AmountTextStyle.SPENT
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // ── CTA button — primary tonal style ──────────────────
                        FilledTonalButton(
                            onClick = onSetBudget,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Set Budget",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.3.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // Status Area  (unchanged logic — kept exactly as original)
        // ─────────────────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = budget to spentInSelectedMonth,
            transitionSpec = {
                val enter = fadeIn(
                    animationSpec = tween(durationMillis = 420, delayMillis = 90)
                ) + slideInVertically(
                    animationSpec = tween(durationMillis = 420, delayMillis = 90)
                ) { it / 12 }

                val exit = fadeOut(
                    animationSpec = tween(durationMillis = 180)
                ) + slideOutVertically(
                    animationSpec = tween(durationMillis = 220)
                ) { -it / 12 }

                enter.togetherWith(exit).using(SizeTransform(clip = false))
            },
            label = "BudgetStatus"
        ) { (budgetState, spentState) ->

            Column {
                Spacer(modifier = Modifier.height(20.dp))

                when {
//                    budgetState <= 0 && spentState == 0.0 -> {
//                        CommonNoBudgetView(
//                            icon = Icons.Default.BarChart,
//                            title = "No spending yet",
//                            description = "You haven't used your budget this month\nA great start!",
//                        )
//                    }

                    budgetState <= 0 && spentState > 0.0 -> {
                        CommonNoBudgetView(
                            icon = Icons.Default.Warning,
                            title = "No budget set",
                            description =
                                "You have already spent $currencySymbol ${CurrencyFormatter.format(spentState, currencySymbol)}\nSet a budget to track your spending properly",
                        )
                    }

                    else -> {
                        BudgetStatusCard(
                            budget = budgetState,
                            usagePercentage = usagePercentage,
                            displayPercentage = displayPercentage
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}