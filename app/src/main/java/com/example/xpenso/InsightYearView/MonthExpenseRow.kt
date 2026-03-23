
package com.example.xpenso.InsightYearView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.InsightsModel.MonthlySummary
import com.example.xpenso.utils.CurrencyFormatter
@Composable
fun MonthExpenseRow(
    summary: MonthlySummary,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = summary.monthName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            val baseStyle = MaterialTheme.typography.bodyLarge

            Text(
                text = buildAnnotatedString {

                    // ₹ smaller −2sp
                    withStyle(
                        SpanStyle(
                            fontSize = (baseStyle.fontSize.value - 2).sp,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(currencySymbol)
                        append(" ")
                    }

                    // amount bold
                    withStyle(
                        SpanStyle(
                            fontSize = baseStyle.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            CurrencyFormatter.format(
                                summary.totalExpense,
                                currencySymbol
                            )
                        )
                    }
                },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
