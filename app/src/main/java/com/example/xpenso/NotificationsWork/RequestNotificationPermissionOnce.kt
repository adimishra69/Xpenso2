//package com.example.xpenso.NotificationsWork
//
//import android.Manifest
//import android.app.Activity
//import android.content.pm.PackageManager
//import android.os.Build
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.*
//import androidx.compose.ui.platform.LocalContext
//import androidx.core.content.ContextCompat
//import com.example.xpenso.SettingsStore
//import kotlinx.coroutines.launch
//
//@Composable
//fun RequestNotificationPermissionOnce(
//    settingsStore: SettingsStore
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    val permissionAsked by
//    settingsStore.notificationPermissionAsked.collectAsState(initial = false)
//
//    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
//
//    val launcher =
//        rememberLauncherForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { granted ->
//            if (granted) {
//                ReminderScheduler.schedule(context)
//            }
//        }
//
////    LaunchedEffect(Unit) {
////
////        val granted =
////            ContextCompat.checkSelfPermission(
////                context,
////                Manifest.permission.POST_NOTIFICATIONS
////            ) == PackageManager.PERMISSION_GRANTED
////
////        if (granted) {
////            ReminderScheduler.schedule(context)
////            return@LaunchedEffect
////        }
////
////        if (!permissionAsked) {
////            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
////
////            scope.launch {
////                settingsStore.setNotificationPermissionAsked()
////            }
////        }
////    }
//}
