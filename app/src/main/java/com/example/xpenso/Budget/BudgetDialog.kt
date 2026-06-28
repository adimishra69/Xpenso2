package com.example.xpenso.Budget

import android.R.attr.shape
import com.example.Home.AmountTextField
import com.example.Home.AmountFieldState
import com.example.Home.validateAmount
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Home.AmountTextField
import java.util.Locale

@Composable
fun BudgetDialog(
    initialValue: String,
    isEditMode: Boolean,
    currencySymbol : String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
    onRemove: () -> Unit,
    previousMonthAmount: Double? = null,
    previousMonthIndex: Int? = null,
) {
    var textValue by remember {
        mutableStateOf(
            initialValue
                .toDoubleOrNull()
                ?.let { String.format(Locale.getDefault(), "%.2f", it) }
                ?: ""
        )
    }

    val monthName = previousMonthIndex?.let {
        java.text.DateFormatSymbols().months[it]
    } ?: ""

    var shakeTrigger by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    var showError by remember { mutableStateOf(false) }

    val cleanedValue = textValue.trimEnd('.')
    val budgetValue = cleanedValue.toDoubleOrNull() ?: 0.0
    val isValid = budgetValue > 0

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,

        // ── Header ──────────────────────────────────────────────────────────
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                // Month badge
                Surface(
                    shape = CircleShape,
                    color = if (isEditMode)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = if (isEditMode) "● Editing" else "● New",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.6.sp
                        ),
                        color = if (isEditMode)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },

        // ── Body ────────────────────────────────────────────────────────────
        text = {
            Column {
                Text(
                    text = if (isEditMode)
                        "Edit your spending limit for this specific month"
                    else
                        "Enter your spending limit for this specific month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))
                val shape = RoundedCornerShape(12.dp)

                if (!isEditMode && previousMonthAmount != null && previousMonthAmount > 0) {

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),

                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(vertical = 6.dp) // ✅ OUTSIDE spacing
                        .clip(shape)              // ✅ clip AFTER padding
                        .clickable {
                            textValue = String.format(
                                Locale.getDefault(),
                                "%.2f",
                                previousMonthAmount
                            )
                        }
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            text = buildAnnotatedString {

                                append("Use last budget (")

                                // 🔥 Currency (smaller + lighter)
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = (MaterialTheme.typography.labelLarge.fontSize.value - 2).sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                ) {
                                    append(currencySymbol)
                                }

                                // 🔥 Amount (normal)
                                append(previousMonthAmount.toInt().toString())

                                append(" • ")

                                // 🔥 Month (slightly faded)
                                withStyle(
                                    style = SpanStyle(
                                    )
                                ) {
                                    append(monthName)
                                }

                                append(")")
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Amount input
                AmountTextField(
                    value          = textValue,
                    onValueChange  = { textValue = it },
                    label          = "Budget Amount",
                    currencySymbol = currencySymbol,
                    externalError  = if (showError && !isValid) "Budget must be greater than 0" else null
                )

                // Anima
                }
        },

        // ── Confirm ─────────────────────────────────────────────────────────
        confirmButton = {

            Button(
                onClick = {
                    if (!isValid) {
                        showError = true
                        return@Button
                    }

                    Toast.makeText(
                        context,
                        if (isEditMode) "Budget updated" else "Budget added",
                        Toast.LENGTH_SHORT
                    ).show()

                    onSave(budgetValue)
                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                },
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                        }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = if (isEditMode) "Save" else "Set Budget",
                    fontWeight = FontWeight.Medium
                )
            }
        },

        // ── Dismiss ─────────────────────────────────────────────────────────
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isEditMode) {

                    TextButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Budget removed",
                                Toast.LENGTH_SHORT
                            ).show()

                            onRemove()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Remove limit")
                    }
                }
            }
        }
    )

    @Composable
    fun BudgetAmountSection(
        displayText: String,
        shakeTrigger: Int
    ) {

        val shakeOffset = remember { Animatable(0f) }
        var firstLoad by remember { mutableStateOf(true) }

        LaunchedEffect(shakeTrigger) {

            if (firstLoad) {
                firstLoad = false
                return@LaunchedEffect
            }

            shakeOffset.animateTo(-6f, tween(35))
            shakeOffset.animateTo(6f, tween(35))
            shakeOffset.animateTo(-4f, tween(30))
            shakeOffset.animateTo(4f, tween(30))
            shakeOffset.animateTo(0f, tween(25))
        }

        Row(
            verticalAlignment = Alignment.Bottom
        ) {

            Text(
                text = "₹",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(Modifier.width(4.dp))

            val fontSize = when (displayText.length) {
                in 0..3 -> 52.sp
                in 4..6 -> 50.sp
                in 7..8 -> 48.sp
                in 9..10 -> 45.sp
                else -> 42.sp
            }

            Text(
                text = if (displayText.isEmpty()) "0" else displayText,
                modifier = Modifier.offset(x = shakeOffset.value.dp),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = fontSize,
                    letterSpacing = (-2).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}