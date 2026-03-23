package com.example.xpenso.InsightMonthComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xpenso.InsightsModel.InsightStats


@Composable
fun InsightSummaryCard(
    currencySymbol: String,
    stats: InsightStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(24.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, null)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Highest expense recorded",
                        style = MaterialTheme.typography.labelMedium)
                    if (stats.highestDayAmount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            AnimatedAmount(
                                amount = stats.highestDayAmount,
                                currencySymbol = currencySymbol
                            )

                            Text(
                                text = " spent on ${stats.highestDayLabel}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "No expenses recorded for this month",
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, null)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Daily average spend",
                        style = MaterialTheme.typography.labelMedium)
                    Row {
                        AnimatedAmount(
                            amount = stats.avgDailySpend,
                            currencySymbol = currencySymbol
                        )
                        Text(" per day", fontWeight = FontWeight.Bold)
                    }

                }
            }
        }
    }
}
