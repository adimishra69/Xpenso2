package com.example.xpenso.AddExpenses

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AmountSection(
    currencySymbol: String,
    displayText: String,
    isZeroPlaceholder: Boolean,
    showBackspace: Boolean,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    shakeTrigger: Int,
    onAmountClick: () -> Unit,
    verticalPadding: Dp = 4.dp
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val shakeOffset = remember { Animatable(0f) }
    var isFirstLoad by remember { mutableStateOf(true) }

    LaunchedEffect(shakeTrigger) {

        if (isFirstLoad) {
            isFirstLoad = false
            return@LaunchedEffect
        }

        shakeOffset.animateTo(
            -6f,
            animationSpec = tween(35)
        )

        shakeOffset.animateTo(
            6f,
            animationSpec = tween(35)
        )

        shakeOffset.animateTo(
            0f,
            animationSpec = tween(35)
        )
    }

    val fontSize = when (displayText.length) {
        in 0..3 -> 52.sp
        in 4..6 -> 50.sp
        in 7..8 -> 48.sp
        in 9..10 -> 45.sp
        else -> 42.sp   // 11 digits max
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
                onAmountClick()
            }
            .padding(horizontal = 16.dp, vertical = verticalPadding)
    ) {

        Text(
            text = "TOTAL AMOUNT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom
            ) {

                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = fontSize * 0.60f // 🔥 ratio control
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.5.dp)
                )

                Spacer(Modifier.width(4.dp))

                val fontSize = when (displayText.length) {
                    in 0..3 -> 52.sp
                    in 4..6 -> 50.sp
                    in 7..8 -> 48.sp
                    in 9..10 -> 45.sp
                    else -> 42.sp   // 11 digits max
                }

                Text(
                    text = displayText,
                    Modifier
                        .offset(x = shakeOffset.value.dp),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = fontSize,
                        letterSpacing = (-2).sp
                    ),
                    maxLines = 1,
                    color =
                        if (isZeroPlaceholder)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                )
            }

            if (showBackspace) {
                BackspaceButton(
                    onSinglePress = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onBackspace()
                    },
                    onLongPress = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onClear()
                    }
                )
            }
        }
    }
}