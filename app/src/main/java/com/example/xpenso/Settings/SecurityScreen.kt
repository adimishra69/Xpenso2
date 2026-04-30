package com.example.xpenso.Settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.BiometricWithScreen.BiometricActivity
import com.example.xpenso.SettingsStore
import com.example.xpenso.utils.AppHeaderBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// PIN Dialog Step Enum
// ─────────────────────────────────────────────
private enum class PinStep { ENTER, CONFIRM }

// ─────────────────────────────────────────────
// SecurityScreen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    settingsStore: SettingsStore,
    onBack: () -> Unit,
    onPinCreated: () -> Unit   // 👈 ADD THIS
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isSecurityEnabled by settingsStore.isSecurityEnabled.collectAsState(false)
    val useBiometrics by settingsStore.useBiometrics.collectAsState(false)

    var isSettingPin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { AnalyticsHelper.trackScreen("Security") }

    val biometricLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            scope.launch { settingsStore.saveUseBiometrics(true) }
        }
    }

    val toggleBiometrics: (Boolean) -> Unit = { enable ->
        if (!enable) {
            scope.launch { settingsStore.saveUseBiometrics(false) }
        } else {
            val intent = Intent(context, BiometricActivity::class.java).apply {
                putExtra("title", "Confirm Security")
                putExtra("subtitle", "Authenticate to enable biometric unlock")
            }
            biometricLauncher.launch(intent)
        }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = { AppHeaderBar(title = "Security", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(22.dp))

            // ── Security Toggle Card ──
            SecuritySettingCard(
                icon = Icons.Default.Lock,
                title = "App Lock",
                subtitle = "Protect app with a 6-digit PIN",
                checked = isSecurityEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        isSettingPin = true
                    } else {
                        scope.launch {
                            settingsStore.saveSecurityEnabled(false)
                            settingsStore.saveSecurityPin(null)
                            settingsStore.saveUseBiometrics(false)
                        }
                    }
                }
            )

            // ── Animated expandable options ──
            AnimatedVisibility(
                visible = isSecurityEnabled,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    SecuritySettingCard(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Unlock",
                        subtitle = "Use fingerprint or face to unlock",
                        checked = useBiometrics,
                        onCheckedChange = toggleBiometrics
                    )

                    Spacer(Modifier.height(16.dp))

                    // Change PIN row
                    SecuritySettingCard(
                        icon = Icons.Default.Security,
                        title = "Change PIN",
                        subtitle = "Update your 6-digit security code",
                        onClick = {
                            isSettingPin = true
                        }
                    )
                }
            }
        }
    }

    // ── Full-screen PIN Dialog ──
    if (isSettingPin) {
        PinSetupDialog(
            onDismiss = { isSettingPin = false },
            onPinSet = { pin ->
                scope.launch {
                    settingsStore.saveSecurityPin(pin)
                    settingsStore.saveSecurityEnabled(true)


                    Toast.makeText(
                        context,
                        if (isSecurityEnabled) "PIN updated successfully"
                        else "PIN set successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    onPinCreated()   // ✅ THIS WAS MISSING

                    isSettingPin = false
                }
            }
        )
    }
}

// ─────────────────────────────────────────────
// Reusable Security Setting Card
// ─────────────────────────────────────────────
@Composable
private fun SecuritySettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (checked != null) {
                Switch(
                    checked = checked,
                    onCheckedChange = { onCheckedChange?.invoke(it) },
                    colors = SwitchDefaults.colors(
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Production PIN Setup Dialog
// ─────────────────────────────────────────────
@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var step by remember { mutableStateOf(PinStep.ENTER) }
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var shakeError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Shake animation for mismatch
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeError) 1f else 0f,
        animationSpec = if (shakeError) {
            keyframes {
                durationMillis = 400
                0f at 0
                -12f at 50
                12f at 100
                -10f at 150
                10f at 200
                -6f at 250
                6f at 300
                0f at 400
            }
        } else spring(),
        finishedListener = { shakeError = false },
        label = "shake"
    )

    fun handleConfirmPin(pin: String) {
        if (pin.length == 6) {
            if (pin == firstPin) {
                showSuccess = true
                scope.launch {
                    delay(600)
                    onPinSet(firstPin)
                }
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                scope.launch {
                    shakeError = true
                    delay(400)
                    confirmPin = ""
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Icon with gradient ring ──
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AnimatedContent(
                                targetState = showSuccess,
                                transitionSpec = { scaleIn() togetherWith scaleOut() },
                                label = "icon"
                            ) { success ->
                                if (success) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Title + Subtitle ──
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { it / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { -it / 2 } + fadeOut()
                    },
                    label = "step_title"
                ) { currentStep ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (currentStep == PinStep.ENTER) "Create PIN" else "Confirm PIN",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (currentStep == PinStep.ENTER)
                                "Enter a 6-digit PIN to secure your app"
                            else
                                "Re-enter your PIN to confirm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Step Indicator ──
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    PinStep.values().forEachIndexed { index, s ->
                        val isActive = s == step || (step == PinStep.CONFIRM && index == 0)
                        val isDone = step == PinStep.CONFIRM && index == 0
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isActive) 24.dp else 16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                // ── Animated PIN Dots ──
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { it / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { -it / 2 } + fadeOut()
                    },
                    label = "pin_dots"
                ) { currentStep ->
                    val currentPin = if (currentStep == PinStep.ENTER) firstPin else confirmPin
                    val onPinChange: (String) -> Unit = if (currentStep == PinStep.ENTER) {
                        { v ->
                            firstPin = v
                            if (v.length == 6) {
                                scope.launch {
                                    delay(150)
                                    step = PinStep.CONFIRM
                                }
                            }
                        }
                    } else {
                        { v ->
                            confirmPin = v
//                            handleConfirmPin(v)
                        }
                    }

                    PinDotRow(
                        pin = currentPin,
                        onPinChange = onPinChange,
                        shakeOffset = if (currentStep == PinStep.CONFIRM) shakeOffset else 0f,
                        isSuccess = showSuccess,
                        isError = shakeError
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Inline Numpad ──
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "numpad"
                ) { currentStep ->
                    val currentPin = if (currentStep == PinStep.ENTER) firstPin else confirmPin
                    val onPinChange: (String) -> Unit = if (currentStep == PinStep.ENTER) {
                        { v ->
                            firstPin = v
                            if (v.length == 6) {
                                scope.launch {
                                    delay(150)
                                    step = PinStep.CONFIRM
                                }
                            }
                        }
                    } else {
                        { v ->
                            confirmPin = v
//                            handleConfirmPin(v)
                        }
                    }

                    InlineNumpad(
                        pin = currentPin,
                        maxLength = 6,
                        onPinChange = onPinChange,
                        enabled = !showSuccess
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Buttons row ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (step == PinStep.CONFIRM) {
                                step = PinStep.ENTER
                                confirmPin = ""
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (step == PinStep.CONFIRM) "Back" else "Cancel" ,  color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (step == PinStep.CONFIRM) {
                        Button(
                            onClick = { handleConfirmPin(confirmPin) },
                            enabled = confirmPin.length == 6 && !showSuccess,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Set PIN", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// PIN Dot Row (visual only, auto-advance)
// ─────────────────────────────────────────────
@Composable
private fun PinDotRow(
    pin: String,
    onPinChange: (String) -> Unit,
    shakeOffset: Float = 0f,
    isSuccess: Boolean = false,
    isError: Boolean = false,
    length: Int = 6
) {
    // Hidden text field for input
//    val focusRequester = remember { FocusRequester() }
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    LaunchedEffect(Unit) {
//        delay(100)
//        try { focusRequester.requestFocus() } catch (_: Exception) {}
//    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shakeOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        // Hidden real input
//        OutlinedTextField(
//            value = pin,
//            onValueChange = { v ->
//                val f = v.filter { it.isDigit() }.take(length)
//                onPinChange(f)
////                if (f.length == length) keyboardController?.hide()
//            },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier
//                .size(1.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Color.Transparent,
//                unfocusedBorderColor = Color.Transparent
//            )
//        )

        // Dot UI
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
            }
        ) {
            repeat(length) { index ->
                val isFilled = index < pin.length
                val scale by animateFloatAsState(
                    targetValue = if (isFilled) 1f else 0.9f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "dot_scale_$index"
                )
                val dotAlpha by animateFloatAsState(
                    targetValue = if (isFilled) 1f else 0f,
                    animationSpec = tween(150),
                    label = "dot_alpha_$index"
                )
                val dotScale by animateFloatAsState(
                    targetValue = if (isFilled) 1f else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "dot_fill_scale_$index"
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isSuccess -> MaterialTheme.colorScheme.primaryContainer
                                isError -> MaterialTheme.colorScheme.errorContainer
                                isFilled -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = when {
                                isSuccess -> MaterialTheme.colorScheme.primary
                                isError -> MaterialTheme.colorScheme.error
                                isFilled -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .scale(dotScale)
                            .graphicsLayer { alpha = dotAlpha }
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Inline Numpad (no system keyboard dependency)
// ─────────────────────────────────────────────
@Composable
private fun InlineNumpad(
    pin: String,
    maxLength: Int,
    onPinChange: (String) -> Unit,
    enabled: Boolean = true
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    val haptic = LocalHapticFeedback.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (key.isNotEmpty()) {
                            val isDelete = key == "⌫"
                            val interactionSource = remember { MutableInteractionSource() }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        enabled = enabled
                                    ) {

                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (isDelete) {
                                            if (pin.isNotEmpty()) onPinChange(pin.dropLast(1))
                                        } else {
                                            if (pin.length < maxLength) onPinChange(pin + key)
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDelete)
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = if (isDelete) 18.sp else 22.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDelete) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
