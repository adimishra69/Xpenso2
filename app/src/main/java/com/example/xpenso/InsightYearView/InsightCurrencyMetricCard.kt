package com.example.xpenso.InsightYearView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.utils.CurrencyFormatter

@Composable
fun InsightCurrencyMetricCard(
    icon: ImageVector,
    label: String,
    amount: Double,
    currencySymbol: String,
    subtitle: AnnotatedString? = null,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary // ✅ non-null default
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)

        ) {

            // Top row → icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ===== Styled currency value =====
            val baseStyle = MaterialTheme.typography.titleLarge

            Text(
                text = buildAnnotatedString {

                    // Currency symbol → 2sp smaller, lighter weight
                    withStyle(
                        SpanStyle(
                            fontSize = (baseStyle.fontSize.value - 2).sp,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(currencySymbol)
                        append(" ")
                    }

                    // Amount → normal size, semi-bold
                    withStyle(
                        SpanStyle(
                            fontSize = baseStyle.fontSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(CurrencyFormatter.format(amount, currencySymbol))
                    }
                },
                color = valueColor
            )

            // Optional subtitle
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
