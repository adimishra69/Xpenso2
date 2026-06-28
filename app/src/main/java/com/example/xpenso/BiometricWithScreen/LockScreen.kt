package com.example.xpenso.BiometricWithScreen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// Design tokens
// ─────────────────────────────────────────────────────────────────────────────
private val BgDeep      = Color(0xFF0A0D14)   // near-black navy
private val BgMid       = Color(0xFF111827)   // dark slate
private val AccentCyan  = Color(0xFF22D3EE)   // vivid cyan
private val AccentViolet= Color(0xFF818CF8)   // soft indigo

private val AccentGlow  = Color(0xFF0EA5E9)   // sky glow
private val SurfaceCard = Color(0xFF1E2433)   // card surface
private val SurfaceLine = Color(0xFF2D3448)   // subtle divider
private val TextPrimary = Color(0xFFF1F5F9)   // bright white-ish
private val TextMuted   = Color(0xFF94A3B8)   // slate-400
private val ErrorRed    = Color(0xFFFC8181)   // soft red

/**
 * A full-screen composable that handles user authentication via PIN or Biometrics.
 *
 * ### Component Breakdown:
 * - **`inputPin`**: A state variable holding the PIN entered by the user.
 * - **`isError`**: A state variable that tracks if the entered PIN is incorrect to show an error.
 * - **`biometricLauncher`**: Uses `rememberLauncherForActivityResult` to safely launch the `BiometricActivity`
 *   and receive a callback. It calls `onUnlock()` only if the authentication is successful.
 * - **`launchBiometricPrompt`**: A reusable lambda that creates the `Intent` for `BiometricActivity` and launches it.
 *   This keeps the `onClick` logic clean.
 * - **`LaunchedEffect`**: Triggers the biometric prompt automatically when the screen first appears, if the user
 *   has enabled biometrics. This is for convenience.
 * - **`OutlinedTextField`**: The input field for the PIN. Its `onValueChange` handler contains the logic for
 *   checking the PIN length and verifying it against the `savedPin`.
 * - **`IconButton` (Fingerprint)**: A manual trigger for the biometric prompt, in case the user cancels the initial one.
 * - **`onUnlock: () -> Unit`**: The critical callback function that notifies the parent composable that authentication
 *   has succeeded. This is the primary output of the `LockScreen`.
 *
 * @param savedPin The correct 4-digit PIN stored in settings.
 * @param useBiometrics A boolean flag indicating if biometric authentication is enabled.
 * @param onUnlock A lambda function to be invoked upon successful authentication.
 */
@Composable
fun LockScreen(
    savedPin: String,
    useBiometrics: Boolean,
    onUnlock: () -> Unit
) {
    var inputPin by rememberSaveable { mutableStateOf("") }
    var isError  by remember { mutableStateOf(false) }
    val context  = LocalContext.current

    // ── entrance animation ────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // ── shake animation on error ──────────────────────────────────────────────
    val shakeOffset by animateFloatAsState(
        targetValue = if (isError) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "shake"
    )

    // ── lock icon pulse ───────────────────────────────────────────────────────
    val lockScale by animateFloatAsState(
        targetValue = if (isError) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "lockScale"
    )

    // ── biometric launcher ────────────────────────────────────────────────────
    val biometricLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onUnlock()
        }
    }

    val launchBiometricPrompt = {
        val intent = Intent(context, BiometricActivity::class.java)
        biometricLauncher.launch(intent)
    }

    LaunchedEffect(Unit) {
        if (useBiometrics) {
            launchBiometricPrompt()
        }
    }

    val haptic = LocalHapticFeedback.current

    // ─────────────────────────────────────────────────────────────────────────
    // Root surface with mesh-gradient background
    // ─────────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF162032), BgDeep),
                    center = Offset(0.5f, 0.3f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Ambient glow blobs (decorative, non-interactive)
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-180).dp)
                .alpha(0.18f)
                .blur(80.dp)
                .background(AccentViolet, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 100.dp, y = 200.dp)
                .alpha(0.14f)
                .blur(80.dp)
                .background(AccentCyan, CircleShape)
        )

        // ── animated entrance ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(
                tween(600, easing = EaseOutCubic),
                initialOffsetY = { it / 3 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── Lock icon badge ────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(88.dp)
                        .scale(lockScale)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AccentViolet.copy(alpha = 0.35f),
                                    AccentCyan.copy(alpha = 0.20f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(AccentCyan.copy(alpha = 0.55f), AccentViolet.copy(alpha = 0.35f))
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isError) ErrorRed else AccentCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── App name ───────────────────────────────────────────────
                Text(
                    text = "Xpenso",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        brush = Brush.horizontalGradient(
                            listOf(AccentCyan, AccentViolet)
                        ),
                        shadow = Shadow(
                            color = AccentCyan.copy(alpha = 0.40f),
                            offset = Offset(0f, 4f),
                            blurRadius = 16f
                        )
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Your account is locked",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = TextMuted,
                        letterSpacing = 0.3.sp
                    )
                )

                Spacer(Modifier.height(40.dp))

                // ── PIN card ───────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceCard)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(SurfaceLine, Color.Transparent)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            text = "ENTER YOUR PIN",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.5.sp,
                                color = TextMuted
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        // ── PIN dot indicators ─────────────────────────────
                        PinDotRow(filledCount = inputPin.length, isError = isError)

                        Spacer(Modifier.height(20.dp))

                        // ── Hidden OutlinedTextField (keeps original logic) ─
                        // We keep the OutlinedTextField for functional correctness
                        // but style it to look premium.
                        OutlinedTextField(
                            value = inputPin,

                            onValueChange = { newPin ->
                                val digitsOnly = newPin.filter { it.isDigit() }
                                handlePinInput(
                                    newPin = digitsOnly,
                                    savedPin = savedPin,
                                    onUpdatePin = { inputPin = it },
                                    onErrorChange = { isError = it },
                                    onUnlock = onUnlock,
                                    haptic = haptic
                                )
                            },
                            label = {
                                Text(
                                    "6-Digit PIN",
                                    style = TextStyle(
                                        color = if (isError) ErrorRed else AccentCyan.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = isError,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = AccentCyan,
                                unfocusedBorderColor    = SurfaceLine,
                                errorBorderColor        = ErrorRed,
                                cursorColor             = AccentCyan,
                                focusedTextColor        = TextPrimary,
                                unfocusedTextColor      = TextPrimary,
                                errorTextColor          = ErrorRed,
                                focusedContainerColor   = BgMid,
                                unfocusedContainerColor = BgMid,
                                errorContainerColor     = Color(0xFF2A1515)
                            ),
                            textStyle = TextStyle(
                                fontSize = 22.sp,
                                letterSpacing = 12.sp,
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = if (isError) (shakeOffset * 6 - 3).dp else 0.dp)
                        )

                        // ── Error message ──────────────────────────────────
                        AnimatedVisibility(
                            visible = isError,
                            enter = fadeIn() + expandVertically(),
                            exit  = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(ErrorRed, CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Incorrect PIN. Please try again.",
                                    color = ErrorRed,
                                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }

                // ── Biometrics section ─────────────────────────────────────
                if (useBiometrics) {
                    Spacer(Modifier.height(28.dp))

                    HorizontalDividerWithLabel(label = "OR")

                    Spacer(Modifier.height(24.dp))

                    BiometricButton(onClick = { launchBiometricPrompt() })
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pin dot row – visual indicator of filled/empty slots
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PinDotRow(filledCount: Int, isError: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val filled = index < filledCount
            val dotColor = when {
                isError && filled -> ErrorRed
                filled            -> AccentCyan
                else              -> SurfaceLine
            }
            val dotScale by animateFloatAsState(
                targetValue = if (filled) 1f else 0.7f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(dotScale)
                    .background(dotColor, CircleShape)
                    .then(
                        if (!filled) Modifier.border(1.5.dp, SurfaceLine, CircleShape)
                        else Modifier
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Horizontal divider with centred label
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HorizontalDividerWithLabel(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SurfaceLine
        )
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = TextMuted
            )
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SurfaceLine
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Premium biometric button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BiometricButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.20f),
                            AccentViolet.copy(alpha = 0.10f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(AccentCyan.copy(alpha = 0.6f), AccentViolet.copy(alpha = 0.4f))
                    ),
                    shape = CircleShape
                )
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Unlock with Biometrics",
                    tint = AccentCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Use Fingerprint",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AccentCyan.copy(alpha = 0.85f),
                letterSpacing = 0.4.sp
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// handlePinInput — UNCHANGED (exact same logic and signature)
// ─────────────────────────────────────────────────────────────────────────────

fun handlePinInput(
    newPin: String,
    savedPin: String,
    onUpdatePin: (String) -> Unit,
    onErrorChange: (Boolean) -> Unit,
    onUnlock: () -> Unit,
    haptic: HapticFeedback
) {

    if (newPin.length > 6) return

    onUpdatePin(newPin)

    when {
        newPin == savedPin -> {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onErrorChange(false)
            onUnlock()
        }

        newPin.length == 6 -> {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onErrorChange(true)
        }

        else -> {
            onErrorChange(false)
        }
    }
}
