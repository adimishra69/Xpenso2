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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.InsightMonthComponents.toDisplayUpper
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
    val category =
        ExpenseCategory.entries
            .find { it.name == expense.category }
            ?: ExpenseCategory.OTHER

    val animation = remember { Animatable(0f) }
    LaunchedEffect(expense.id) {
        animation.animateTo(1f, tween(300))
    }

    val categoryColor  = category.color
    val onSurface      = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar   = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha       = animation.value
                translationY = (1 - animation.value) * 20f
            },
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.7.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ── Category icon — flat soft circle, no glow ─────────
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                CategoryIcon(
                    category = category,
                    tint = category.color
                )
            }

            Spacer(Modifier.width(14.dp))

            // ── Title + meta ──────────────────────────────────────
            Column(Modifier.weight(1f)) {

                Text(
                    text     = expense.title,
                    style    = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    color    = onSurface,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    val paymentIcon = when (expense.paymentMode) {
                        "Cash" -> Icons.Default.Payments
                        "Card" -> Icons.Default.CreditCard
                        "UPI"  -> Icons.Default.QrCode
                        else   -> Icons.Default.CreditCard
                    }

                    Icon(
                        imageVector    = paymentIcon,
                        contentDescription = null,
                        modifier       = Modifier.size(11.dp),
                        tint           = onSurfaceVar.copy(alpha = 0.40f)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text  = expense.paymentMode,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = onSurfaceVar.copy(alpha = 0.45f),
                        letterSpacing = 0.sp
                    )

                    Text(
                        text  = "  ·  ",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = onSurfaceVar.copy(alpha = 0.22f)
                    )

                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(Date(expense.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = onSurfaceVar.copy(alpha = 0.38f),
                        letterSpacing = 0.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // ── Amount + category tag ─────────────────────────────
            Column(horizontalAlignment = Alignment.End) {

                val baseStyle = MaterialTheme.typography.bodyLarge
                Text(
                    text = buildAnnotatedString {
                        // Currency symbol — ghost weight
                        withStyle(
                            SpanStyle(
                                fontSize   = (baseStyle.fontSize.value * 0.69f).sp,
                                fontWeight = FontWeight.Normal,
                                color      = onSurface.copy(alpha = 0.30f),
                                baselineShift = BaselineShift(0.1f)
                            )
                        ) {
                            append("$currencySymbol ")
                        }
                        // Amount — reads first, bold and confident
                        withStyle(
                            SpanStyle(
                                fontSize      = (baseStyle.fontSize.value * 1.0f).sp,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = (-0.4).sp,
                                color         = onSurface.copy(alpha = 0.90f)
                            )
                        ) {
                            append(CurrencyFormatter.format(expense.amount, currencySymbol))
                        }
                    }
                )

                Spacer(Modifier.height(3.dp))

                // Barely-there category label — the whisper detail
                Text(
                    text          = category.name.toDisplayUpper(),
                    style         = MaterialTheme.typography.labelSmall,
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 0.6.sp,
                    color         = categoryColor.copy(alpha = 0.50f)
                )
            }
        }
    }
}