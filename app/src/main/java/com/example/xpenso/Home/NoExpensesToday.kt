package com.example.xpenso.Home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoExpensesToday(
    onAddExpenseClick: () -> Unit
) {
    // -------- Fade + slight slide animation --------
    val alpha = remember { Animatable(0f) }
    val offsetY = remember {Animatable(40f) }

    LaunchedEffect(Unit) {
        // run together for smooth entrance
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(500)
        )
    }

    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(500)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp)
            .graphicsLayer {
                this.alpha = alpha.value
                translationY = offsetY.value
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // -------- Icon subtle scale-in --------
        val scale = remember {Animatable(0.92f) }

        LaunchedEffect(Unit) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec =tween(450)
            )
        }

        Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = null,
            modifier = Modifier
                .size(98.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            tint = MaterialTheme.colorScheme.primary.copy(.9f) // Changed tint to match primary theme color
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Start tracking your spending\nand stay in control",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(20.dp))

        // -------- Button fade-in delay (premium touch) --------
        val buttonAlpha = remember { androidx.compose.animation.core.Animatable(0f) }

        LaunchedEffect(Unit) {
            buttonAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis = 120
                )
            )
        }

        OutlinedButton(
            onClick = onAddExpenseClick,
            modifier = Modifier.graphicsLayer {
                this.alpha = buttonAlpha.value
            }
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Add Expense")
        }
    }
}