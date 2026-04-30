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
    const val REMINDER_NOTIFICATION_ID = 102

    private const val REMINDER_WORK_NAME_5PM = "add_expense_reminder_5pm"
    private const val REMINDER_WORK_NAME_8PM = "add_expense_reminder_8pm"

    fun schedule(context: Context) {
        createNotificationChannel(context)
        scheduleReminder(context, 17, REMINDER_WORK_NAME_5PM) // 5 PM
        scheduleReminder(context, 20, REMINDER_WORK_NAME_8PM) // 8 PM
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME_5PM)
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME_8PM)
    }

    private fun scheduleReminder(
        context: Context,
        hour: Int,
        workName: String
    ) {

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

        val workRequest =
            PeriodicWorkRequestBuilder<AddExpenseReminderWorker>(
                1,
                TimeUnit.DAYS
            )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("type" to workName))
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    private fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "Daily Reminders"
            val descriptionText =
                "Daily reminders to set budget and add expenses."

            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    name,
                    importance
                ).apply {
                    description = descriptionText
                    setShowBadge(true)
                }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}