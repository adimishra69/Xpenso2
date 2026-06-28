package com.example.xpenso.AppStart

import androidx.compose.runtime.Composable

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.xpenso.SettingsStore
import com.example.xpenso.ui.theme.XpensoTheme


class MainActivity : ComponentActivity() {
        private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

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
//            LaunchedEffect(fromNotification) {
//                if (fromNotification) {
//                    settingsStore.clearNotificationCount()
//                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                    notificationManager.cancelAll()
//                }
//            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.cancelAll()

            val themeMode by settingsStore.themeMode.collectAsState(initial = 0)
            val colorStyle by settingsStore.colorStyle.collectAsState(initial = 0)

            val darkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            XpensoTheme(
                darkTheme = darkTheme,
//                dynamicColor = colorStyle == 1
                colorStyle = colorStyle
            ) {
                AppRoot(
                    settingsStore = settingsStore,
                )
//                FunScreen()
            }
        }
    }
}

//fun get(name:String) : String{
//    return "Hello $name , you are bulding something cool!"
//}

//@Composable
//fun FunScreen() {
//
//    var text by remember { mutableStateOf("Click button") }
//    var clicked by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Text(text = text)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            text = get("John")
//            clicked = true
//        }) {
//            Text("Show Message")
//        }
//    }
//
//    LaunchedEffect(clicked) {
//        if (clicked) {
//            kotlinx.coroutines.delay(3000)
//            text = "Click button"   // reset instead of empty
//            clicked = false
//        }
//    }
//}

