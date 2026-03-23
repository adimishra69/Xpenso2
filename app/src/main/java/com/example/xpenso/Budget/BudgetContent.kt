package com.example.xpenso.Budget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.xpenso.ui.components.AmountText
import com.example.xpenso.ui.components.AmountTextStyle
import com.example.xpenso.utils.CurrencyFormatter
import java.util.Calendar
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.ui.components.BudgetStatusCard
import com.example.xpenso.ui.components.CommonNoBudgetView


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
            .padding(20.dp)
    ){

        // ---------- Month Navigation ----------
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        modifier = Modifier.size(23.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = selectedMonthYearDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        modifier = Modifier.size(23.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------- Budget Info Card ----------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), // Increased roundness
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            // ⭐ PREMIUM TOUCH: Subtle border and elevation
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 21.dp,
                    end = 21.dp,
                    top = 36.dp,
                    bottom = 36.dp
                )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Column(
                        modifier = Modifier.weight(1f).graphicsLayer {
                            translationY = animatedOffset
                            alpha = animatedAlpha
                        }
                    ){
                        Text("Monthly Budget", style = MaterialTheme.typography.labelLarge)
                        AmountText(
                            amount = budget,
                            currencySymbol = currencySymbol,
                            style = AmountTextStyle.BUDGET
                        )
                    }

                    if (budget > 0.0) {
                        OutlinedButton(
                            onClick = onEditBudget,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Edit Budget",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight    = FontWeight.Medium,
                                    letterSpacing = 0.3.sp
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = animatedOffset
                            alpha = animatedAlpha
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text("Spent", style = MaterialTheme.typography.labelMedium)

                        AmountText(
                            amount = spentInSelectedMonth,
                            currencySymbol = currencySymbol,
                            style = AmountTextStyle.SPENT
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("Remaining", style = MaterialTheme.typography.labelMedium)

                        BudgetAmountText(
                            remaining = remaining,
                            budget = budget,
                            currencySymbol = currencySymbol
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { usagePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)), // Rounds the background track
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    // ⭐ ROUNDED ENDS:
                    strokeCap = StrokeCap.Round,      // Rounds the progress line ends
                    gapSize = 0.dp,                   // Removes gaps
                    drawStopIndicator = {}            // Removes the square/dot at the end
                )


                Spacer(Modifier.height(8.dp))

                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = budgetStatusText,
                    color = budgetStatusColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ================= STATUS AREA =================
        AnimatedContent(
            targetState = budget to spentInSelectedMonth,
            transitionSpec = {

                val enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 420,
                        delayMillis = 90
                    )
                ) + slideInVertically(
                    animationSpec = tween(
                        durationMillis = 420,
                        delayMillis = 90
                    )
                ) { it / 12 }   // smaller motion → calmer feel

                val exit = fadeOut(
                    animationSpec = tween(durationMillis = 180)
                ) + slideOutVertically(
                    animationSpec = tween(durationMillis = 220)
                ) { -it / 12 }

                enter.togetherWith(exit)
                    .using(SizeTransform(clip = false))
            },
            label = "BudgetStatus"
        ) { (budgetState, spentState) ->

            Column {
                Spacer(modifier = Modifier.height(20.dp))

                when {
                    // 🆕 New month
                    budgetState <= 0 && spentState == 0.0 -> {
                        CommonNoBudgetView(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "No spending yet",
                            description = "You haven’t used your budget this month\nA great start!",
                            buttonText = "Set Budget",
                            onSetBudgetClick = onSetBudget
                        )
                    }

                    // ⚠️ Spent but no budget
                    budgetState <= 0 && spentState > 0.0 -> {
                        CommonNoBudgetView(
                            icon = Icons.Default.Warning,
                            title = "No budget set",
                            description =
                                "You have already spent $currencySymbol ${CurrencyFormatter.format(spentState, currencySymbol)}\nSet a budget to track your spending properly",
                            buttonText = "Set Budget",
                            onSetBudgetClick = onSetBudget
                        )
                    }

                    // ✅ Normal tracking
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

        Spacer(Modifier.height(40.dp)) // Extra space at bottom

    }
}