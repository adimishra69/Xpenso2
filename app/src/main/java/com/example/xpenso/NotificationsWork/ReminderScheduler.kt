package com.example.xpenso.NotificationsWork

import android.content.Context

/**
 * This object now acts as a wrapper around the new NotificationScheduler.
 * It preserves the original call sites but delegates all functionality
 * to the new, more advanced scheduling system.
 */
object ReminderScheduler {

    /**
     * Schedules all required notifications (daily insights and add-expense reminders).
     */
    fun schedule(context: Context) {
        NotificationScheduler.schedule(context)
    }

    /**
     * Cancels all scheduled notifications.
     */
    fun cancel(context: Context) {
        NotificationScheduler.cancel(context)
    }
}