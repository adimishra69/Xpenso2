package com.example.xpenso

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore by preferencesDataStore(name = "settings")

data class NotificationMessage(
    val id: String,
    val message: String,
    val timestamp: Long
)

class SettingsStore(private val context: Context) {

    private val gson = Gson()

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val THEME_MODE = intPreferencesKey("theme_mode")

        val IS_PREMIUM = booleanPreferencesKey("is_premium")   // ⭐ ADD THIS

        private val COLOR_STYLE_KEY = intPreferencesKey("color_style_key")
//        val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
        val SCREEN_MODE = intPreferencesKey("screen_mode")
        val IS_SECURITY_ENABLED = booleanPreferencesKey("is_security_enabled")
        val SECURITY_PIN = stringPreferencesKey("security_pin")
        val USE_BIOMETRICS = booleanPreferencesKey("use_biometrics")
        val START_OF_MONTH = intPreferencesKey("start_of_month")
        val NOTIFICATION_COUNT = intPreferencesKey("notification_count")
        val NOTIFICATION_HISTORY = stringPreferencesKey("notification_history")

        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")

    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val currencySymbol: Flow<String> = context.dataStore.data.map { it[CURRENCY_SYMBOL] ?: "₹" }
    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[ANALYTICS_ENABLED] ?: true }
    val themeMode: Flow<Int> = context.dataStore.data.map { it[THEME_MODE] ?: 0 }
    val colorStyle: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[COLOR_STYLE_KEY] ?: 1 // Default to 0 (Material You)
    }
    val screenMode: Flow<Int> = context.dataStore.data.map { it[SCREEN_MODE] ?: 1 }
    val startOfMonth: Flow<Int> = context.dataStore.data.map { it[START_OF_MONTH] ?: 1 }
    val isSecurityEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_SECURITY_ENABLED] ?: false }
    val securityPin: Flow<String?> = context.dataStore.data.map { it[SECURITY_PIN] }
    val useBiometrics: Flow<Boolean> = context.dataStore.data.map { it[USE_BIOMETRICS] ?: false }
//    val notificationCount: Flow<Int> = context.dataStore.data.map { it[NOTIFICATION_COUNT] ?: 0 }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[REMINDER_ENABLED] ?: false
    }

    val isPremium: Flow<Boolean> =
        context.dataStore.data.map { it[IS_PREMIUM] ?: false }
//    val notificationPermissionAsked: Flow<Boolean> = context.dataStore.data.map {
//        it[NOTIFICATION_PERMISSION_ASKED] ?: false
//
//    }

    val notificationHistory: Flow<List<NotificationMessage>> = context.dataStore.data.map { preferences ->
        val json = preferences[NOTIFICATION_HISTORY] ?: "[]"
        val type = object : TypeToken<List<NotificationMessage>>() {}.type
        gson.fromJson(json, type)
    }

    suspend fun saveNotification(message: String) {
        val currentHistory = notificationHistory.first().toMutableList()

        val today = Calendar.getInstance()
        val isAlreadySentToday = currentHistory.any { msg ->
            val msgCal = Calendar.getInstance().apply { timeInMillis = msg.timestamp }
            msg.message == message &&
                    today.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
        }
        if (isAlreadySentToday) return

        val newMessage = NotificationMessage(
            id = java.util.UUID.randomUUID().toString(),
            message = message,
            timestamp = System.currentTimeMillis()
        )

        currentHistory.add(0, newMessage) // Add to top
        if (currentHistory.size > 25) currentHistory.removeLast() // Keep last 25

        context.dataStore.edit { it[NOTIFICATION_HISTORY] = gson.toJson(currentHistory) }
    }

//    suspend fun incrementNotificationCount() {
//        context.dataStore.edit { preferences ->
//            val currentCount = preferences[NOTIFICATION_COUNT] ?: 0
//            preferences[NOTIFICATION_COUNT] = currentCount + 1
//        }
//    }


    suspend fun savePremium(enabled: Boolean) {
        context.dataStore.edit {
            it[IS_PREMIUM] = enabled
        }
    }

    // --- Color Style Setting ---
    suspend fun saveColorStyle(styleIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_STYLE_KEY] = styleIndex
        }
    }


    suspend fun clearNotificationCount() {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_COUNT] = 0
        }
    }

    fun getMonthlyBudget(month: Int, year: Int): Flow<Double> {
        val key = doublePreferencesKey("budget_${month}_${year}")
        return context.dataStore.data.map { it[key] ?: 0.0 }
    }

    suspend fun removeMonthlyBudget(month: Int, year: Int) {
        val key = doublePreferencesKey("budget_${month}_${year}")
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

//    suspend fun setNotificationPermissionAsked() {
//        context.dataStore.edit { it[NOTIFICATION_PERMISSION_ASKED] = true }
//    }
//
//    suspend fun saveReminderEnabled(enabled: Boolean) {
//        context.dataStore.edit {
//            it[REMINDER_ENABLED] = enabled
//        }
//    }

    suspend fun saveOnboardingCompleted(completed: Boolean) = context.dataStore.edit {
        it[ONBOARDING_COMPLETED] = completed
    }
    suspend fun saveCurrencySymbol(symbol: String) = context.dataStore.edit {
        it[CURRENCY_SYMBOL] = symbol
    }
    suspend fun saveAnalyticsEnabled(enabled: Boolean) = context.dataStore.edit {
        it[ANALYTICS_ENABLED] = enabled
    }
    suspend fun saveThemeMode(mode: Int) = context.dataStore.edit {
        it[THEME_MODE] = mode
    }
    suspend fun saveScreenMode(mode: Int) = context.dataStore.edit {
        it[SCREEN_MODE] = mode
    }
    suspend fun saveSecurityEnabled(enabled: Boolean) = context.dataStore.edit {
        it[IS_SECURITY_ENABLED] = enabled
    }
    suspend fun saveSecurityPin(pin: String?) = context.dataStore.edit {
        if (pin == null) it.remove(SECURITY_PIN) else it[SECURITY_PIN] = pin
    }
    suspend fun saveUseBiometrics(enabled: Boolean) = context.dataStore.edit {
        it[USE_BIOMETRICS] = enabled
    }
    suspend fun saveMonthlyBudget(month: Int, year: Int, budget: Double) = context.dataStore.edit {
        it[doublePreferencesKey("budget_${month}_${year}")] = budget
    }
    suspend fun saveStartOfMonth(day: Int) = context.dataStore.edit {
        it[START_OF_MONTH] = day
    }
    suspend fun clearAll() = context.dataStore.edit {
        it.clear()
    }
}