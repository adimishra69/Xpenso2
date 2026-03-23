package com.example.xpenso.AppStart

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.compose.ui.platform.LocalContext
import com.example.xpenso.NotificationsWork.RequestNotificationPermissionOnce
import com.example.xpenso.SettingsStore
import com.example.xpenso.ui.theme.XpensoTheme


class MainActivity : ComponentActivity() {
        private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val fromNotification = intent.getBooleanExtra("fromNotification", false)

        enableEdgeToEdge()

        setContent {
            // Inside MainActivity.kt -> setContent { ... }
            val context = LocalContext.current
            val settingsStore = remember { SettingsStore(context) }

            // --- ADD THIS BLOCK ---
            val analyticsEnabled by settingsStore.analyticsEnabled.collectAsState(initial = true)

            // ✅ This correctly enables/disables Firebase collection based on user preference
            LaunchedEffect(analyticsEnabled) {
                AnalyticsHelper.setEnabled(context, analyticsEnabled)
            }

            // ✅ Clear BOTH in-app and OS badges when opening from a notification
            LaunchedEffect(fromNotification) {
                if (fromNotification) {
                    settingsStore.clearNotificationCount()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelAll()
                }
            }

            RequestNotificationPermissionOnce(settingsStore)

            val themeMode by settingsStore.themeMode.collectAsState(initial = 0)
            val colorStyle by settingsStore.colorStyle.collectAsState(initial = 1)

            val darkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            XpensoTheme(
                darkTheme = darkTheme,
                dynamicColor = colorStyle == 1
            ) {
                AppRoot(
                    settingsStore = settingsStore,
                    startInNotificationScreen = fromNotification
                )
            }
        }
    }
}
