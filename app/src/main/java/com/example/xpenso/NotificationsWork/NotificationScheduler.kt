package com.example.xpenso.NotificationsWork

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

object NotificationScheduler {
    const val CHANNEL_ID = "MY_PROJECT_REMINDER_CHANNEL"
    const val INSIGHT_NOTIFICATION_ID = 101
    const val REMINDER_NOTIFICATION_ID = 102

    private const val INSIGHT_WORK_NAME = "daily_insight_notification"
    private const val REMINDER_WORK_NAME = "add_expense_reminder"

    fun schedule(context: Context) {
        createNotificationChannel(context)
        scheduleInsightNotifications(context)
        scheduleAddExpenseReminder(context)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(INSIGHT_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    private fun scheduleInsightNotifications(context: Context) {
        val insightTimes = listOf(9, 14, 19) // 9 AM, 2 PM, 7 PM

        for (hour in insightTimes) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val delay = target.timeInMillis - now.timeInMillis
            val workRequest = PeriodicWorkRequestBuilder<SmartInsightWorker>(24, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "SmartInsightWork", // A Unique Name stops duplicates!
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    private fun scheduleAddExpenseReminder(context: Context) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = target.timeInMillis - now.timeInMillis
        val workRequest = PeriodicWorkRequestBuilder<AddExpenseReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders & Insights"
            val descriptionText = "Get daily spending insights and reminders to add expenses."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}