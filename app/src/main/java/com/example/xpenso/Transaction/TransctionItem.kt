package com.example.xpenso.Transaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.xpenso.Database.ExpenseEntity

import com.example.xpenso.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ---------------- ITEM UI ---------------- */

@Composable
fun TransactionItem(
    expense: ExpenseEntity,
    currencySymbol: String
) {
    val category = ExpenseCategory.detect(expense.title)

    val animation = remember { Animatable(0f) }

    LaunchedEffect(expense.id) {
        animation.animateTo(1f,     tween(300))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animation.value
                // Reduce Y translation from 40f to 20f for a smoother "float-in" effect
                translationY = (1 - animation.value) * 20f
            },
        shape = RoundedCornerShape(24.dp), // Increase from default to 24dp for a modern feel
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // Add a very subtle border
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        // Add a tiny bit of elevation
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        category.color.copy(alpha = 0.12f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {

                Text(
                    expense.title,
                    style = MaterialTheme.typography.titleMedium, // Changed from bodyLarge
                    fontWeight = FontWeight.ExtraBold, // Use ExtraBold for better contrast
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    val paymentIcon = when (expense.paymentMode) {
                        "Cash" -> Icons.Default.Payments
                        "Card" -> Icons.Default.CreditCard
                        "UPI" -> Icons.Default.QrCode
                        else -> Icons.Default.Payments
                    }

                    Icon(
                        paymentIcon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "${expense.paymentMode} • ${
                            SimpleDateFormat(
                                "hh:mm a",
                                Locale.getDefault()
                            ).format(Date(expense.timestamp))
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                    )
                }
            }
            val baseStyle = MaterialTheme.typography.bodyLarge

            Text(
                text = buildAnnotatedString {

                    // Currency symbol — smaller & lighter
                    withStyle(
                        SpanStyle(
                            fontSize = (baseStyle.fontSize.value * 0.75f).sp,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(currencySymbol)
                        append(" ")
                    }

                    // Amount — full size & bold
                    withStyle(
                        SpanStyle(
                            fontSize = baseStyle.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            CurrencyFormatter.format(
                                expense.amount,
                                currencySymbol
                            )
                        )
                    }
                },
                color = MaterialTheme.colorScheme.onSurface
            )

        }
    }
}