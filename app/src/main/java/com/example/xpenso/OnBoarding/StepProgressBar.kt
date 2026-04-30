package com.example.xpenso.OnBoarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StepProgressBar(currentStep: Int) {
    val steps = listOf("Currency", "Analytics")
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        steps.forEachIndexed { index, _ ->
            val isActive = index == currentStep
            val isDone = index < currentStep

            val dotColor by animateColorAsState(
                targetValue = if (isDone || isActive) primary else surfaceVariant,
                animationSpec = tween(400),
                label = "dot_$index"
            )

            val dotSize by animateDpAsState(
                targetValue = if (isActive) 10.dp else 7.dp,
                animationSpec = tween(300),
                label = "dotSize_$index"
            )

            // Dot
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            // Line
            if (index < steps.size - 1) {

                val lineProgress by animateFloatAsState(
                    targetValue = if (currentStep > index) 1f else 0f,
                    animationSpec = tween(500),
                    label = "line_$index"
                )

                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(lineProgress)
                            .background(primary)
                    )
                }
            }
        }
    }
}