package com.example.xpenso.Home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun DailySmartHeader(
    message: String,
    messageColor: Color,
    transactionCount: Int,
    monthlyBudget: Double,
    selectedDateMillis: Long,
    startOfMonth: Int // Added startOfMonth parameter
) {

    // ---------- Month progress text ----------
    val progressMessage = remember(selectedDateMillis, startOfMonth) {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Calculate the start of the current custom cycle
        val startCal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, startOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If selected date is before the start day of this calendar month, the cycle started last month
        if (calendar.before(startCal)) {
            startCal.add(Calendar.MONTH, -1)
        }

        // Calculate the end of the current custom cycle
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        // Calculate total days in the custom cycle
        val daysInCycle = ((endCal.timeInMillis - startCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
        
        // Calculate days passed in the cycle
        val daysPassed = ((selectedDateMillis - startCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        
        val daysLeft = (daysInCycle - daysPassed -1).coerceAtLeast(0) // -1 because current day is included in daysPassed

        when {
            daysPassed == 0 -> "Start of the month cycle"
            daysLeft == 0 -> "Last day of the month cycle"
            daysLeft == 1 -> "1 day left in the month cycle"
            else -> "$daysLeft days left in the month cycle"
        }
    }

    // ---------- Smart message override ----------
    val finalMessage =
        if (monthlyBudget <= 0) "Please set a monthly budget" else message

    val finalColor =
        if (monthlyBudget <= 0) MaterialTheme.colorScheme.onSurfaceVariant else messageColor


    // ---------- PREMIUM animation ONLY for main message ----------
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(8f) }

    LaunchedEffect(finalMessage) {
        alpha.snapTo(0f)
        offsetY.snapTo(8f)

        alpha.animateTo(1f, tween(350))
        offsetY.animateTo(0f, tween(350))
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {

        // ⭐ Animated main insight text
        Text(
            text = finalMessage.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = finalColor,
            letterSpacing = 1.6.sp,
            modifier = Modifier.graphicsLayer { 
                this.alpha = alpha.value
                translationY = offsetY.value
            }
        )

        // ---------- STATIC secondary line (NO animation) ----------
        val countText = when (transactionCount) {
            0 -> "No transactions"
            1 -> "1 transaction"
            else -> "$transactionCount transactions"
        }

        Text(
            text = "$countText  •  $progressMessage",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
