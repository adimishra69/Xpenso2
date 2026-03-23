package com.example.xpenso.NotificationsWork

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.xpenso.NotificationsWork.ReminderScheduler

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.schedule(context)
        }
    }
}