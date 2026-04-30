package com.example.xpenso.OnBoarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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


fun getCurrencySymbol(currency: JavaCurrency): String {
    return when (currency.currencyCode) {

        "JPY" -> "¥"
        "CNY" -> "¥"
        "IDR" -> "Rp"
        "PEN" -> "S/"
        "USD","CAD","AUD","NZD","SGD","HKD","XCD","JMD" -> "$"
        // Peso
        "PHP","MXN","ARS","CLP","COP","UYU" -> "₱"

        "INR","PKR","LKR","NPR" -> "₹"

        "RUB" -> "₽"

        "TRY" -> "₺"

        "KRW" -> "₩"
        "THB" -> "฿"
        "VND" -> "₫"
        "UAH" -> "₴"

        "DKK" , "SEK" , "NOK" -> "kr"

        // Hungarian Forint
        "HUF" -> "Ft"


        // Africa
        "NGN" -> "₦"
        "GHS" -> "₵"
        "ZAR" -> "R"
        "BDT" -> "৳"
        "ALL" -> "L"
        "CZK" -> "Kč"
        "EGP" , "GBP" -> "£"

        "MYR" -> "RM"
        "UZS" -> "so'm"

        "BGN" -> "лв"
        "BOB" -> "Bs"
        "GEL" -> "₾"
        "ISK" -> "kr"
        "KZT" -> "₸"
        "MOP" -> "MOP$"
        "MKD" -> "ден"
        "MMK" -> "K"
        "PYG" -> "₲"
        "PLN" -> "zł"
        "RON" -> "lei"
        "RSD" -> "дин"
        else -> currency.symbol
    }
}

// ---------------- LOAD ALL WORLD CURRENCIES ----------------
    fun getAllCurrencies(): List<CurrencyItem> {
    val validCurrencies = setOf(

        // 🌍 Major
        "INR","USD","EUR","GBP","JPY","AUD","CAD","CHF","CNY","HKD",
        "SGD","NZD","SEK","NOK","DKK",

        // 🌏 Asia
        "PKR","LKR","KZT","ILS","NPR","IDR","MYR","THB","PHP","VND","KRW",
        "BDT","AFN","KZT","UZS","TWD","MMK","MOP", "TWD",

        // 🌍 Middle East
        "AED","SAR","QAR","KWD","OMR","IRR","IQD","JOD","BHD",

        // 🌍 Europe
        "PLN","CZK","HUF","RON","BGN","UAH","RSD", "ALL",
        "MKD", "ISK", "GEL",

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
                    symbol = getCurrencySymbol(it)
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Main Screens
            when {
                showTerms -> {
                    TermsScreen(onBack = { showTerms = false })
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
                        onOpenTerms = { showTerms = true }
                    )
                }
            }

            // Fixed Bottom Dots
            if (!onlyCurrency && !showTerms) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 41.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StepProgressBar(currentStep = screen)
                }
            }
        }
    }
}

@Composable
fun OnboardingStepHeader(
    step: Int,
    total: Int = 2
) {

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "stepFade"
    )

    Text(
        text = "STEP ${step + 1} OF $total",
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = alpha),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
}