package com.example.xpenso.Home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$currentMonthName Summary") },
        text = {
            val containerAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
            val containerScale = remember { androidx.compose.animation.core.Animatable(0.96f) }

            LaunchedEffect(Unit) {
                containerAlpha.animateTo(1f, animationSpec = tween(250))
                containerScale.animateTo(1f, animationSpec = tween(250))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = containerAlpha.value
                        scaleX = containerScale.value
                        scaleY = containerScale.value
                    }
            ) {

            val titleStyle = MaterialTheme.typography.titleLarge

                Text(
                    text = buildAnnotatedString {

                        // "Total: "
                        withStyle(
                            SpanStyle(
                                fontSize = titleStyle.fontSize,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append("Total: ")
                        }

                        // Currency symbol (smaller + lighter)
                        withStyle(
                            SpanStyle(
                                fontSize = (titleStyle.fontSize.value * 0.75f).sp,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(currencySymbol)
                            append(" ")
                        }

                        // Amount (full size + bold)
                        withStyle(
                            SpanStyle(
                                fontSize = titleStyle.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(CurrencyFormatter.format(monthlyTotal, currencySymbol))
                        }
                    },
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(monthlyExpenses) { expense ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(expense.title, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(expense.timestamp)),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                text = buildAnnotatedString {

                                    // currency symbol (smaller)
                                    withStyle(
                                        SpanStyle(
                                            fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value - 2).sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        append("$currencySymbol ")
                                    }

                                    // amount (normal size)
                                    withStyle(
                                        SpanStyle(
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    ) {
                                        append(
                                            CurrencyFormatter.format(expense.amount, currencySymbol)
                                        )
                                    }
                                }
                            )


                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { // --- THE FIX IS HERE ---
                Text(
                    "Close",
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Explicitly set the color for Cancel
                ) }
        }
    )
}
