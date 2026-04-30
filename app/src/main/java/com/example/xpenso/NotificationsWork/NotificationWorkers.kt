package com.example.xpenso.NotificationsWork

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.xpenso.R
import com.example.xpenso.AppStart.MainActivity
import com.example.xpenso.Database.ExpenseDatabase
import com.example.xpenso.SettingsStore
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AddExpenseReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {

            val db = ExpenseDatabase.getDatabase(context)
            val settingsStore = SettingsStore(context)
            val allExpenses = db.expenseDao().getAllExpenses().first()

            val now = Calendar.getInstance()

            val monthlyBudget = settingsStore
                .getMonthlyBudget(
                    now.get(Calendar.MONTH),
                    now.get(Calendar.YEAR)
                ).first()

            val dailyTotal = allExpenses.filter { expense ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = expense.timestamp
                }

                cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)

            }.sumOf { it.amount }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            // Read worker type
            val type = inputData.getString("type")

            // 5 PM — Budget Check
            if (type == "add_expense_reminder_5pm" && monthlyBudget <= 0) {

                val notification =
                    NotificationCompat.Builder(
                        context,
                        NotificationScheduler.CHANNEL_ID
                    )
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Set Monthly Budget")
                        .setContentText("Set your budget to track spending")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

                notificationManager.notify(100, notification)
            }


            // 8 PM — Expense Reminder
            if (type == "add_expense_reminder_8pm" && monthlyBudget > 0 && dailyTotal == 0.0) {

                val notification =
                    NotificationCompat.Builder(
                        context,
                        NotificationScheduler.CHANNEL_ID
                    )
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Add Today's Expenses")
                        .setContentText("Don't forget to add today's expenses")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

                notificationManager.notify(101, notification)
            }

            Result.success()

        } catch (e: Exception) {
            Result.failure()
        }
    }
}