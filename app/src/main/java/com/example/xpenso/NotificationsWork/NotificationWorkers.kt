package com.example.xpenso.NotificationsWork

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import com.example.xpenso.R
import androidx.work.WorkerParameters
import com.example.xpenso.AppStart.MainActivity
import com.example.xpenso.Database.ExpenseDatabase
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.SettingsStore

import kotlinx.coroutines.flow.first
import java.util.Calendar

// Main worker for sending smart, contextual insights
class SmartInsightWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val settingsStore = SettingsStore(context)
            val db = ExpenseDatabase.getDatabase(context)
            val allExpenses = db.expenseDao().getAllExpenses().first()

            val message = generateAdvancedInsightMessage(context, allExpenses, settingsStore)

            if (message.isNotBlank()) {
                settingsStore.saveNotification(message)
                settingsStore.incrementNotificationCount()
                val currentCount = settingsStore.notificationCount.first()

                val intent = Intent(context, MainActivity::class.java).apply {
                    action = "com.example.myproject.ACTION_CLEAR_NOTIFICATIONS" // <-- ADD THIS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("fromNotification", true)
                }

                val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Smart Insight")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setNumber(currentCount)
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NotificationScheduler.INSIGHT_NOTIFICATION_ID, notification)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

// Main worker for the simple 8:00 PM reminder
class AddExpenseReminderWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = ExpenseDatabase.getDatabase(context)
            val settingsStore = SettingsStore(context)
            val allExpenses = db.expenseDao().getAllExpenses().first()
            val todayCal = Calendar.getInstance()

            // Calculate daily total
            val dailyTotal = allExpenses.filter { expense ->
                val cal = Calendar.getInstance().apply { timeInMillis = expense.timestamp }
                cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)
            }.sumOf { it.amount }

            // Get budget and cycle info
            val startOfMonth = settingsStore.startOfMonth.first()
            val cycleStartCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, startOfMonth)
                if (todayCal.get(Calendar.DAY_OF_MONTH) < startOfMonth) add(Calendar.MONTH, -1)
            }
            val cycleEndCal = (cycleStartCal.clone() as Calendar).apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_MONTH, -1) }
            val monthlyBudget = settingsStore.getMonthlyBudget(cycleStartCal.get(Calendar.MONTH), cycleStartCal.get(Calendar.YEAR)).first()
            val monthlyTotal = allExpenses.filter { it.timestamp in cycleStartCal.timeInMillis..cycleEndCal.timeInMillis }.sumOf { it.amount }

            // Only remind if no expenses today, a budget is set, AND the budget has not been exceeded
            if (dailyTotal == 0.0 && monthlyBudget > 0 && monthlyTotal < monthlyBudget) {
                settingsStore.incrementNotificationCount()
                val currentCount = settingsStore.notificationCount.first()


                val intent = Intent(context, MainActivity::class.java).apply {
                    action = "com.example.myproject.ACTION_CLEAR_NOTIFICATIONS" // <-- ADD THIS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("fromNotification", true) // <-- ADD THIS for consistency
                }

                val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Daily Reminder")
                    .setContentText("Don't forget to add today's expenses!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setNumber(currentCount)
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NotificationScheduler.REMINDER_NOTIFICATION_ID, notification)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

/**
 * The new, advanced logic for generating contextual spending insights.
 * This function is kept separate from the UI and can be safely run from a background worker.
 */
suspend fun generateAdvancedInsightMessage(context: Context, allExpenses: List<ExpenseEntity>, settingsStore: SettingsStore): String {
    val monthlyBudget = settingsStore.getMonthlyBudget(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR)).first()
    if (monthlyBudget <= 0.0) return "Set a monthly budget to get smart insights."

    val todayCal = Calendar.getInstance()
    val startOfMonth = settingsStore.startOfMonth.first()

    // Calculate cycle details
    val cycleStartCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, startOfMonth)
        if (todayCal.get(Calendar.DAY_OF_MONTH) < startOfMonth) add(Calendar.MONTH, -1)
    }
    val cycleEndCal = (cycleStartCal.clone() as Calendar).apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_MONTH, -1) }
    val daysInCycle = cycleEndCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysPassedInCycle = if (todayCal.get(Calendar.DAY_OF_MONTH) >= startOfMonth) {
        todayCal.get(Calendar.DAY_OF_MONTH) - startOfMonth
    } else {
        val prevMonth = (todayCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH) - startOfMonth + todayCal.get(Calendar.DAY_OF_MONTH)
    }
    val daysLeftInCycle = (daysInCycle - daysPassedInCycle).coerceAtLeast(0)

    // Calculate spending totals
    val dailyTotal = allExpenses.filter { val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }; cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) }.sumOf { it.amount }
    val yesterdayCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterdayTotal = allExpenses.filter { val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }; cal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) }.sumOf { it.amount }
    val monthlyTotal = allExpenses.filter { it.timestamp in cycleStartCal.timeInMillis..cycleEndCal.timeInMillis }.sumOf { it.amount }
    val dailySafeToSpend = (monthlyBudget - monthlyTotal + dailyTotal).coerceAtLeast(0.0) / (daysLeftInCycle.takeIf { it > 0 } ?: 1)

    // --- INSIGHT LOGIC (PRIORITIZED) ---

    // 1. Top Priority: Budget Exceeded
    if (monthlyTotal > monthlyBudget) {
        return "You\'ve exceeded your monthly budget. Be careful with your spending."
    }

    // 2. Critical Alert: Near end of cycle
    if (daysLeftInCycle <= 3) {
        if (monthlyTotal / monthlyBudget > 0.9) return "Heads up: You\'re at ${(monthlyTotal / monthlyBudget * 100).toInt()}% of your budget with $daysLeftInCycle days left."
    }

    // 3. Early-Month Overspending
    if (daysPassedInCycle in 1..7) {
        val expectedSpending = (monthlyBudget / daysInCycle) * daysPassedInCycle
        if (monthlyTotal > expectedSpending * 1.5) return "Early month check-in: Your spending is higher than average for this point in your cycle."
    }

    // 4. Recovery Feedback
    if (yesterdayTotal > dailySafeToSpend && dailyTotal < dailySafeToSpend && dailyTotal > 0) {
        return "Nice recovery! You\'ve pulled back on your spending today."
    }

    // 5. Weekend Spending
    val dayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK)
    if ((dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) && dailyTotal > dailySafeToSpend * 1.2) {
        return "Weekend spending alert: Today\'s total is a bit high."
    }

    // 6. Achievement Praise
    if (daysPassedInCycle >= 7) {
        val expectedSpending = (monthlyBudget / daysInCycle) * daysPassedInCycle
        if (monthlyTotal < expectedSpending * 0.7) return "Great work! Your spending is well under budget for this far into the month."
    }

    // 7. Fallback to standard daily insight
    return when {
        dailyTotal == 0.0 -> ""
        dailyTotal <= dailySafeToSpend * 0.7 -> "Excellent control today. Your spending is low."
        dailyTotal <= dailySafeToSpend -> "You\'re within today\'s safe spending limit."
        else -> "High spending detected for today."
    }
}