package com.example.xpenso.Home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.CurrencyFormatter
import com.example.xpenso.utils.CurrencyText

@Composable
fun DailyTopSpendingCard(
    filteredExpenses: List<ExpenseEntity>,
    currencySymbol: String
) {
    // 1. Logic to find the most spent category today
    val topCategoryData = remember(filteredExpenses) {
        if (filteredExpenses.isEmpty()) return@remember null

        // Group expenses by category (using your detect logic) and sum them up
        filteredExpenses.groupBy { ExpenseCategory.Companion.detect(it.title) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .maxByOrNull { it.value } // Find the one with the highest sum
    }

    // 2. Only show the card if there is spending today
    topCategoryData?.let { (category, totalAmount) ->
        if (category != ExpenseCategory.OTHER) {

            var showCard by remember { mutableStateOf(false) }

            LaunchedEffect(topCategoryData) {
                showCard = false
                if (topCategoryData != null) {
                    showCard = true
                }
            }

            AnimatedVisibility(
                visible = showCard,
                enter = fadeIn(animationSpec = tween(350)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(350)
                        ) +
                        scaleIn(
                            initialScale = 0.96f,
                            animationSpec = tween(350)
                        )
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = category.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = category.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LARGEST EXPENSE TODAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = category.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        CurrencyText(
                            amount = totalAmount,
                            currencySymbol = currencySymbol,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}