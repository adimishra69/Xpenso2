package com.example.xpenso.OnBoarding

import com.example.xpenso.OnBoarding.CurrencyItem
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.xpenso.OnBoarding.AnalyticsScreen
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.Settings.TermsScreen
import com.example.xpenso.SettingsStore
import kotlinx.coroutines.launch
import java.util.Currency as JavaCurrency
import java.util.Locale

// ---------------- DATA MODEL ----------------
data class CurrencyItem(
    val name: String,
    val code: String,
    val symbol: String
)

// ---------------- LOAD ALL WORLD CURRENCIES ----------------
    fun getAllCurrencies(): List<CurrencyItem> {
    val validCurrencies = setOf(

        // 🌍 Major
        "INR","USD","EUR","GBP","JPY","AUD","CAD","CHF","CNY","HKD",
        "SGD","NZD","SEK","NOK","DKK",

        // 🌏 Asia
        "PKR","LKR","KZR","ILS","NPR","IDR","MYR","THB","PHP","VND","KRW",

        // 🌍 Middle East
        "AED","SAR","QAR","KWD","OMR","IRR","IQD","JOD","BHD",

        // 🌍 Europe
        "PLN","CZK","HUF","RON","BGN","UAH",

        // 🌎 Americas
        "BRL","MXN","ARS","CLP","COP","PEN","UYU","PYG","BOB","JMD",

        // 🌍 Africa
        "ZAR","NGN","KES","GHS","EGP","MAD","TND", "ZWG",

        // 🌍 Others
        "TRY","RUB","XCD"
    )
        return JavaCurrency.getAvailableCurrencies()
            .filter { it.currencyCode in validCurrencies }
            .map {
                CurrencyItem(
                    name = it.getDisplayName(Locale.ENGLISH),
                    code = it.currencyCode,
                    symbol = it.symbol
                )
            }
            .sortedBy { it.name }
    }

// ---------------- ROOT ----------------
@Composable
fun OnboardingScreen(
    onlyCurrency: Boolean = false,
    onFinish: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    settingsStore: SettingsStore // Added settingsStore parameter
) {
    val analyticsEnabled by settingsStore.analyticsEnabled.collectAsState(initial = true)
    val scope = rememberCoroutineScope() // Added for launching suspend functions

    val context = LocalContext.current

    val currentCurrencySymbol by settingsStore.currencySymbol.collectAsState(initial = "₹")

    var screen by remember { mutableIntStateOf(0) }

    // 2. Initialize selectedCurrency by finding it in the full list
    var selectedCurrency by remember(currentCurrencySymbol) {
        mutableStateOf(
            getAllCurrencies().find { it.symbol == currentCurrencySymbol }
        )
    }
    var showTerms by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

            AnalyticsHelper.trackScreen("Onboarding")
    }


//    if (onlyCurrency && onBack != null) {
//        BackHandler {
//            onBack()
//        }
//    }

    BackHandler(enabled = showTerms) {
        showTerms = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            showTerms -> {
                TermsScreen(
                    onBack = { showTerms = false }
                )
            }

            screen == 0 -> {
                CurrencyScreen(
                    selectedCurrency = selectedCurrency,
                    onSelect = { selectedCurrency = it },
                    onNext = {
                        if (onlyCurrency) {
                            val symbol = selectedCurrency?.symbol ?: "₹"
                            onFinish(symbol)
                        } else if (selectedCurrency != null) {
                            screen = 1
                        }
                    },
                    isChange = onlyCurrency,
                    onBack = if (onlyCurrency) onBack else null
                )
            }

            screen == 1 -> {
                AnalyticsScreen(
                    crashReportsEnabled = analyticsEnabled,
                    onToggle = { enabled ->
                        scope.launch { settingsStore.saveAnalyticsEnabled(enabled) }
                    },
                    onBack = { screen = 0 },
                    onStart = {
                        val symbol = selectedCurrency?.symbol ?: "₹"
                        onFinish(symbol)
                    },
                    onOpenTerms = {
                        showTerms = true // 🔥 THIS YOU FORGOT
                    }
                )
            }
        }
    }
}