package com.example.xpenso.AddExpenses

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


// ─────────────────────────────────────────────────────────────────────────────
// CALCULATOR GRID
// Layout: 7 8 9 +  |  4 5 6 −  |  1 2 3 ×  |  0 . = ÷
// ─────────────────────────────────────────────────────────────────────────────

private data class CalcKey(val label: String, val type: KeyType)
private enum class KeyType { NUMBER, OPERATOR, EQUALS }

private val CALC_KEYS = listOf(
    CalcKey("7", KeyType.NUMBER),  CalcKey("8", KeyType.NUMBER),  CalcKey("9", KeyType.NUMBER),  CalcKey("+", KeyType.OPERATOR),
    CalcKey("4", KeyType.NUMBER),  CalcKey("5", KeyType.NUMBER),  CalcKey("6", KeyType.NUMBER),  CalcKey("-", KeyType.OPERATOR),
    CalcKey("1", KeyType.NUMBER),  CalcKey("2", KeyType.NUMBER),  CalcKey("3", KeyType.NUMBER),  CalcKey("*", KeyType.OPERATOR),
    CalcKey("0", KeyType.NUMBER),  CalcKey(".", KeyType.NUMBER),  CalcKey("=", KeyType.EQUALS),  CalcKey("/", KeyType.OPERATOR),
)

@Composable
 fun CalcGrid(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,          // ← ADD
    verticalPadding: Dp = 6.dp,             // ← ADD
    rowSpacing: Dp = 7.dp,                  // ← ADD
    buttonHeight: Dp = 56.dp                // ← ADD
) {
    Column(
        modifier = modifier.padding(horizontal = 14.dp, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        for (row in 0..3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),                // ← ADD (rows share height equally)
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                for (col in 0..3) {
                    val key = CALC_KEYS[row * 4 + col]
                    CalcButton(
                        key = key,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),               // ← ADD
                        onClick = { onKey(key.label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    key: CalcKey,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    val containerColor = when (key.type) {
        KeyType.NUMBER   -> MaterialTheme.colorScheme.surfaceVariant
        KeyType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
        KeyType.EQUALS   -> MaterialTheme.colorScheme.primary.copy(.9f)
    }
    val contentColor = when (key.type) {
        KeyType.NUMBER   -> MaterialTheme.colorScheme.onSurface
        KeyType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
        KeyType.EQUALS   -> MaterialTheme.colorScheme.onPrimary
    }
    val displayLabel = when (key.label) {
        "*" -> "×"; "/" -> "÷"; "-" -> "−"; else -> key.label
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}