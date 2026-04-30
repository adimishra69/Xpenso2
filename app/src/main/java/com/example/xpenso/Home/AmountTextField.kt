package com.example.Home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─── Constants ───────────────────────────────────────────────────────────────

private const val MAX_INTEGER_DIGITS = 9          // e.g. 999,999,999
private const val MAX_DECIMAL_DIGITS = 2          // e.g. .99
private const val MAX_TOTAL_AMOUNT   = 999_999_999.99

// ─── Data model ──────────────────────────────────────────────────────────────

/**
 * Sealed class describing every validation state the field can be in.
 *
 * @property message    Human-readable feedback shown below the field. Null = no helper text.
 * @property isError    Whether to style the border in the error colour.
 */
sealed class AmountFieldState(
    open val message: String? = null,
    open val isError: Boolean = false
) {
    /** Field is idle / untouched. */
    object Idle : AmountFieldState()

    /** Value is valid and within limits. */
    object Valid : AmountFieldState()

    /** Integer part has hit MAX_INTEGER_DIGITS. */
    data class IntegerLimitReached(
        override val message: String = "Maximum $MAX_INTEGER_DIGITS integer digits reached",
        override val isError: Boolean = true
    ) : AmountFieldState()

    /** Decimal part has hit MAX_DECIMAL_DIGITS. */
    data class DecimalLimitReached(
        override val message: String = "Only $MAX_DECIMAL_DIGITS decimal places allowed",
        override val isError: Boolean = true
    ) : AmountFieldState()

    /** The full value exceeds the allowed ceiling. */
    data class AmountTooLarge(
        override val message: String = "Amount cannot exceed ${MAX_TOTAL_AMOUNT.toLong().formatWithCommas()}",
        override val isError: Boolean = true
    ) : AmountFieldState()

    /** Field is empty when it shouldn't be (e.g. on submit). */
    data class Empty(
        override val message: String = "Please enter an amount",
        override val isError: Boolean = true
    ) : AmountFieldState()

    /** Custom external error injected by the parent. */
    data class ExternalError(
        override val message: String,
        override val isError: Boolean = true
    ) : AmountFieldState()
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun Long.formatWithCommas(): String =
    "%,d".format(this)

/**
 * Validate a raw string and return the matching [AmountFieldState].
 */
fun validateAmount(raw: String): AmountFieldState {
    if (raw.isBlank()) return AmountFieldState.Idle

    val integerPart  = raw.substringBefore(".").trimStart('-', ' ')
    val decimalPart  = if (raw.contains(".")) raw.substringAfter(".") else ""

    if (integerPart.length > MAX_INTEGER_DIGITS)  return AmountFieldState.IntegerLimitReached()
    if (decimalPart.length > MAX_DECIMAL_DIGITS)  return AmountFieldState.DecimalLimitReached()

    val numeric = raw.toDoubleOrNull()
    if (numeric != null && numeric > MAX_TOTAL_AMOUNT) return AmountFieldState.AmountTooLarge()

    return AmountFieldState.Valid
}

// ─── Composable ──────────────────────────────────────────────────────────────

/**
 * A premium, fully-validated amount text field.
 *
 * Features
 * ─────────
 * • Shake + haptic feedback when a limit is reached
 * • Animated helper text that slides in/out
 * • Remaining-digit counter that pulses when close to the limit
 * • Currency prefix label
 * • Gradient focus ring
 * • External error injection via [externalError]
 *
 * @param value          Current raw text value
 * @param onValueChange  Called with the new raw text whenever it passes validation
 * @param modifier       Standard compose modifier
 * @param label          Field label shown above
 * @param currencySymbol Prefix symbol shown inside the field (default "₹")
 * @param externalError  Optional error message injected from outside (e.g. server-side validation)
 * @param showCharCounter Whether to show the remaining integer-digit counter
 */
@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Amount",
    currencySymbol: String, //= "₹"
    externalError: String? = null,
    showCharCounter: Boolean = true
) {
    val haptic       = LocalHapticFeedback.current
    val scope        = rememberCoroutineScope()
    val shakeOffset  = remember { Animatable(0f) }
    val borderScale  = remember { Animatable(1f) }

    // Track if field has been interacted with
    var isDirty by remember { mutableStateOf(false) }

    // Shake trigger — incrementing causes the LaunchedEffect to re-run
    var shakeTrigger by remember { mutableIntStateOf(0) }
    var isFirstLoad  by remember { mutableStateOf(true) }

    // Compute live validation state
    val validationState: AmountFieldState = when {
        externalError != null -> AmountFieldState.ExternalError(externalError)
        isDirty               -> validateAmount(value)
        else                  -> AmountFieldState.Idle
    }

    val isError = validationState.isError

    // Integer chars remaining (for counter)
    val integerPartLength = value.substringBefore(".").trimStart('-', ' ').length
    val integerRemaining  = MAX_INTEGER_DIGITS - integerPartLength
    val counterCritical   = integerRemaining <= 2

    // ── Shake animation ───────────────────────────────────────────────────────
    LaunchedEffect(shakeTrigger) {
        if (isFirstLoad) { isFirstLoad = false; return@LaunchedEffect }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        repeat(3) {
            shakeOffset.animateTo(-7f, tween(40))
            shakeOffset.animateTo(7f,  tween(40))
        }
        shakeOffset.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
    }

    // ── Border pulse on error ─────────────────────────────────────────────────
    LaunchedEffect(isError) {
        if (isError) {
            borderScale.animateTo(1.015f, tween(100))
            borderScale.animateTo(1f,     spring(Spring.DampingRatioMediumBouncy))
        }
    }

    // ── Border colour ─────────────────────────────────────────────────────────
    val borderColor by remember(isError, isDirty) {
        derivedStateOf {
            when {
                isError  -> Color(0xFFE53935)    // Material error red
                isDirty && validationState is AmountFieldState.Valid ->
                    Color(0xFF43A047)             // success green
                else     -> Color(0xFFBDBDBD)    // neutral grey
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    Column(modifier = modifier) {

        // ── Label + counter row ───────────────────────────────────────────────
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                ),
                color = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (showCharCounter && isDirty) {
                val counterScale = remember { Animatable(1f) }
                LaunchedEffect(integerRemaining) {
                    if (counterCritical) {
                        counterScale.animateTo(1.25f, tween(120))
                        counterScale.animateTo(1f,    spring(Spring.DampingRatioMediumBouncy))
                    }
                }

                Text(
                    text  = buildAnnotatedString {
                        withStyle(SpanStyle(
                            color      = if (counterCritical) Color(0xFFE53935) else Color(0xFF9E9E9E),
                            fontWeight = if (counterCritical) FontWeight.Bold else FontWeight.Normal
                        )) {
                            append("$integerRemaining")
                        }
                        withStyle(SpanStyle(color = Color(0xFF9E9E9E))) {
                            append("/$MAX_INTEGER_DIGITS")
                        }
                    },
                    style    = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.scale(counterScale.value)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Text field box ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(borderScale.value)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .border(
                    width = if (isError) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                // Currency symbol
                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 13.sp,   // 2–3sp smaller than amount (16sp)
                        fontWeight = FontWeight.Medium,  // less bold
                        color = if (isError)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(0.9f)
                    ),
                    modifier = Modifier
                        .padding(top = 1.dp) // optional: align baseline better
                )

                Spacer(Modifier.width(2.dp))

                BasicTextField(
                    value    = value,
                    onValueChange = { newValue ->
                        isDirty = true

                        val cleaned     = newValue.trim()
                        val integerPart = cleaned.substringBefore(".", cleaned).trimStart('-', ' ')
                        val decimalPart = if (cleaned.contains(".")) cleaned.substringAfter(".") else ""

                        // Block if integer part exceeds limit
                        if (integerPart.length > MAX_INTEGER_DIGITS) {
                            scope.launch { shakeTrigger++ }
                            return@BasicTextField
                        }

                        // Block if decimal part exceeds limit
                        if (decimalPart.length > MAX_DECIMAL_DIGITS) {
                            scope.launch { shakeTrigger++ }
                            return@BasicTextField
                        }

                        // Block if amount too large
                        val numeric = cleaned.toDoubleOrNull()
                        if (numeric != null && numeric > MAX_TOTAL_AMOUNT) {
                            scope.launch { shakeTrigger++ }
                            return@BasicTextField
                        }

                        onValueChange(newValue)
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeOffset.value.dp),

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),

                    singleLine   = true,
                    cursorBrush  = SolidColor(
                        if (isError) MaterialTheme.colorScheme.error
                        else         MaterialTheme.colorScheme.primary
                    ),
                    textStyle = TextStyle(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.2.sp
                    ),

                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text  = "0.00",
                                style = TextStyle(
                                    fontSize  = 16.sp,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // ── Animated helper / error text ──────────────────────────────────────
        AnimatedVisibility(
            visible = validationState.message != null,
            enter   = fadeIn(tween(180)) + expandVertically(tween(180)),
            exit    = fadeOut(tween(120)) + shrinkVertically(tween(120))
        ) {
            validationState.message?.let { msg ->
                Spacer(Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = if (isError) Icons.Filled.Warning else Icons.Outlined.Info,
                        contentDescription = null,
                        modifier           = Modifier.size(13.dp),
                        tint               = if (isError)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text  = msg,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight    = FontWeight.Medium,
                            letterSpacing = 0.1.sp
                        ),
                        color = if (isError)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}