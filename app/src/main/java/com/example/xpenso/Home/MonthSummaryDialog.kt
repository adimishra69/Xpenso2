package com.example.xpenso.Home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthSummaryDialog(
    currentMonthName: String,
    currencySymbol: String,
    monthlyTotal: Double,
    monthlyExpenses: List<ExpenseEntity>,
    onDismiss: () -> Unit
) {
    val containerAlpha = remember { Animatable(0f) }
    val containerTranslate = remember { Animatable(24f) }
    val containerScale = remember { Animatable(0.96f) }

    LaunchedEffect(Unit) {
        containerAlpha.animateTo(1f, animationSpec = tween(300))
        containerTranslate.animateTo(0f, animationSpec = tween(300))
        containerScale.animateTo(1f, animationSpec = tween(300))
    }
    val baseCurrencySize = 20.sp
    val baseAmountSize = 28.sp

    val formattedAmount = CurrencyFormatter.format(monthlyTotal, currencySymbol)

// count only digits (ignore commas etc)
    val digitCount = formattedAmount.filter { it.isDigit() }.length

    val reduction = when {
        digitCount <= 7 -> 0.sp
        digitCount <= 10 -> 2.sp
        else -> 3.5.sp
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                // "MONTHLY SUMMARY" eyebrow label
                Text(
                    text = "MONTHLY SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                // Month name as title
                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                // Total amount row
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Total spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            // Currency symbol — smaller, colored
                            withStyle(
                                SpanStyle(
                                    fontSize = (baseCurrencySize.value - reduction.value).sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    baselineShift = BaselineShift(0.1f)
                                )
                            ) { append("$currencySymbol ") }

                            // Amount — large, prominent
                            withStyle(
                                SpanStyle(
                                    fontSize = (baseAmountSize.value - reduction.value).sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.5).sp
                                )
                            ) {
                                append(formattedAmount)
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .graphicsLayer {
                        alpha = containerAlpha.value
                        scaleX = containerScale.value
                        scaleY = containerScale.value
                        translationY = containerTranslate.value
                    },
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(monthlyExpenses) { index, expense ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Leading dot + info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = expense.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM", Locale.getDefault())
                                        .format(Date(expense.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Amount
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) { append("$currencySymbol ") }
                                withStyle(
                                    SpanStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = (-0.3).sp
                                    )
                                ) {
                                    append(CurrencyFormatter.format(expense.amount, currencySymbol))
                                }
                            },
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (index != monthlyExpenses.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            thickness = 0.7.dp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                )
            }
        }
    )
}