package com.example.xpenso.Settings


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium destructive-action confirmation dialog.
 *
 * Usage:
 *
 *   if (showClearDataDialog) {
 *       ClearDataDialog(
 *           onConfirm = {
 *               scope.launch {
 *                   haptic.performHapticFeedback(HapticFeedbackType.LongPress)
 *                   settingsStore.clearAll()
 *                   expenseViewModel.deleteAllData()
 *                   Toast.makeText(context, "Data deleted successfully!", Toast.LENGTH_LONG).show()
 *               }
 *           },
 *           onDismiss = { showClearDataDialog = false }
 *       )
 *   }
 */
@Composable
fun ClearDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // ── Entry animations ─────────────────────────────────────────────────────
    val overlayAlpha  = remember { Animatable(0f) }
    val cardScale     = remember { Animatable(0.88f) }
    val cardAlpha     = remember { Animatable(0f) }
    val cardOffsetY   = remember { Animatable(40f) }
    val iconScale     = remember { Animatable(0f) }

    // Pulse on the warning icon
    val iconPulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by iconPulse.animateFloat(
        initialValue = 1f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )

    // Shimmer on the confirm button
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmer.animateFloat(
        initialValue = -300f,
        targetValue  = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    var isDismissing by remember { mutableStateOf(false) }

    fun animateDismiss(then: () -> Unit) {
        if (isDismissing) return
        isDismissing = true
        scope.launch {
            launch { overlayAlpha.animateTo(0f, tween(220)) }
            launch { cardScale.animateTo(0.92f, tween(200, easing = FastOutLinearInEasing)) }
            launch { cardAlpha.animateTo(0f, tween(200)) }
            delay(220)
            then()
        }
    }

    LaunchedEffect(Unit) {
        launch { overlayAlpha.animateTo(1f, tween(280)) }
        launch {
            delay(60)
            cardAlpha.animateTo(1f, tween(300))
        }
        launch {
            delay(60)
            cardScale.animateTo(
                1f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
            )
        }
        launch {
            delay(60)
            cardOffsetY.animateTo(
                0f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
            )
        }
        launch {
            delay(250)
            iconScale.animateTo(
                1f,
                spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
            )
        }
    }

    val errorColor   = MaterialTheme.colorScheme.error
    val errorAlpha15 = errorColor.copy(alpha = 0.12f)
    val errorAlpha30 = errorColor.copy(alpha = 0.28f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface    = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val outline      = MaterialTheme.colorScheme.outlineVariant

    Dialog(
        onDismissRequest = { animateDismiss(onDismiss) },
        properties = DialogProperties(
            dismissOnBackPress      = true,
            dismissOnClickOutside   = true,   // no accidental outside tap
            usePlatformDefaultWidth = false
        )
    ) {
        // ── Scrim ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayAlpha.value }
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {  animateDismiss(onDismiss)},
            contentAlignment = Alignment.Center
        ) {

            // ── Card ─────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .wrapContentHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* consume — block scrim bleed-through */ }
                    .graphicsLayer {
                        scaleX        = cardScale.value
                        scaleY        = cardScale.value
                        alpha         = cardAlpha.value
                        translationY  = cardOffsetY.value
                    },
                shape         = RoundedCornerShape(28.dp),
                color         = surfaceColor,
                tonalElevation  = 3.dp,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    // ── Warning icon with glow ring ───────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(iconScale.value * pulseScale)
                            .drawBehind {
                                // soft glow halo
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(errorAlpha30, Color.Transparent),
                                        center = Offset(size.width / 2, size.height / 2),
                                        radius = size.minDimension * 0.75f
                                    )
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(errorAlpha15),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text     = "⚠️",
                                fontSize = 28.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Title ─────────────────────────────────────────────────
                    Text(
                        text       = "Clear All Data?",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = onSurface,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    // ── Body ──────────────────────────────────────────────────
                    Text(
                        text      = "This will permanently delete all your expenses and reset your settings",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = onSurfaceVar,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    // ── Warning pill ──────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(errorAlpha15)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text      = "This action cannot be undone",
                            style     = MaterialTheme.typography.labelMedium,
                            color     = errorColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    HorizontalDivider(color = outline ,
                        thickness = 1.dp
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── Confirm button (shimmer) ──────────────────────────────
                    Button(
                        onClick = {
                            scope.launch {
                                // tiny press-scale before executing
                                animateDismiss {
                                    onConfirm()
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .drawBehind {
                                // shimmer sweep over the button
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.18f),
                                            Color.Transparent
                                        ),
                                        start = Offset(shimmerOffset, 0f),
                                        end   = Offset(shimmerOffset + 200f, size.height)
                                    )
                                )
                            },
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = errorColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            pressedElevation = 0.dp,
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text       = "Clear Everything",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // ── Cancel button ─────────────────────────────────────────
                    TextButton(
                        onClick  = { animateDismiss(onDismiss) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text   = "Cancel",
                            style  = MaterialTheme.typography.labelLarge,
                            color  = onSurfaceVar,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}