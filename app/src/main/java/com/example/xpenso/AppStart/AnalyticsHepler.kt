package com.example.xpenso.AppStart


import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {

    /**
     * Call this whenever the user flips the toggle in Settings or Onboarding.
     * It tells Firebase to stop or start collecting ALL data.
     */
    fun setEnabled(context: Context, enabled: Boolean) {
        Firebase.analytics.setAnalyticsCollectionEnabled(enabled)
    }

    /**
     * Logs a screen view, but only if collection is enabled.
     */
    fun trackScreen(screenName: String) {
        // Firebase handles the check automatically if setAnalyticsCollectionEnabled was called,
        // but it's good practice to keep this simple.
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
    }
}