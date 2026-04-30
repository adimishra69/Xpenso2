package com.example.xpenso.AppStart

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.xpenso.BiometricWithScreen.LockScreen
import com.example.xpenso.OnBoarding.OnboardingScreen
import com.example.xpenso.SettingsStore
import kotlinx.coroutines.launch

@Composable
fun AppRoot(
    settingsStore: SettingsStore,
    startInNotificationScreen: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // 🔒 CONFIG
    val LOCK_DELAY = 5000L

    var skipNextLock by rememberSaveable { mutableStateOf(false) }
    val onboardingCompleted by settingsStore.onboardingCompleted.collectAsState(initial = null)
    val selectedCurrencySymbol by settingsStore.currencySymbol.collectAsState(initial = "₹")
    val isSecurityEnabled by settingsStore.isSecurityEnabled.collectAsState(initial = false)
    val savedPin by settingsStore.securityPin.collectAsState(initial = null)
    val useBiometrics by settingsStore.useBiometrics.collectAsState(initial = false)
    val screenMode by settingsStore.screenMode.collectAsState(initial = 0)

    // ✅ survives recomposition + rotation
    var isUnlocked by rememberSaveable { mutableStateOf(false) }

    // ✅ Clean lock condition
    val shouldLock = onboardingCompleted == true &&
            isSecurityEnabled &&
            savedPin != null &&
            !isUnlocked &&
            !skipNextLock

    // 🔁 Lifecycle handling
//    val lifecycleOwner = LocalLifecycleOwner.current
//    var lastBackgroundTime by remember { mutableStateOf(0L) }

//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {

//                Lifecycle.Event.ON_STOP -> {
//                    lastBackgroundTime = System.currentTimeMillis()
//                }

//                Lifecycle.Event.ON_RESUME -> {
//                    val now = System.currentTimeMillis()
//                    if (now - lastBackgroundTime > LOCK_DELAY) {
//                        isUnlocked = false
//                    }
//                }
//
//                else -> {}
//            }
//        }

//        lifecycleOwner.lifecycle.addObserver(observer)

//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }



    // 🎛 System UI control (unchanged)
    LaunchedEffect(screenMode, view) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        when (screenMode) {

            // 0 → Standard
            0 -> {
                WindowCompat.setDecorFitsSystemWindows(window, true)

                insetsController.show(
                    WindowInsetsCompat.Type.statusBars() or
                            WindowInsetsCompat.Type.navigationBars()
                )
            }

            // 1 → Fullscreen (edge-to-edge but bars visible)
            1 -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)

                insetsController.show(
                    WindowInsetsCompat.Type.statusBars() or
                            WindowInsetsCompat.Type.navigationBars()
                )
            }

            // 2 → Immersive (hide bars)
            2 -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)

                insetsController.hide(
                    WindowInsetsCompat.Type.statusBars() or
                            WindowInsetsCompat.Type.navigationBars()
                )

                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    // ⏳ Loading
    if (onboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // 🚀 Onboarding
    if (onboardingCompleted == false) {
        OnboardingScreen(
            settingsStore = settingsStore,
            onFinish = { symbol ->
                scope.launch {
                    settingsStore.saveOnboardingCompleted(true)
                    settingsStore.saveCurrencySymbol(symbol)
                }
            }
        )
        return
    }

    // 🔒 LOCK SCREEN
    if (shouldLock) {
        LockScreen(
            savedPin = savedPin!!,
            useBiometrics = useBiometrics,
            onUnlock = { isUnlocked = true }
        )
        return
    }

    MainScreen(
        currencySymbol = selectedCurrencySymbol,
        settingsStore = settingsStore,
        startInNotificationScreen = startInNotificationScreen,
        onPinCreated ={skipNextLock = true}  // ✅ FIX
    )
}