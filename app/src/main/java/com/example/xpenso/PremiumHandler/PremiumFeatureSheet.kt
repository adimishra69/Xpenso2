package com.example.xpenso.PremiumHandler

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ─── Features ────────────────────────────────────────────────────────────────
private val premiumFeatures = listOf(
    "Custom date filters",
    "Cloud backup",
    "Restore across devices",
    "Advanced spending insights",
    "Export transactions"
)

// ─── Entry point — same public signature as before ───────────────────────────
@Composable
fun PremiumFeatureSheet(
    message: String,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    CustomBottomSheet(onDismiss = onDismiss) {
        SheetBody(
            message = message,
            onDismiss = onDismiss,
            onUpgradeClick = onUpgradeClick
        )
    }
}

// ─── Custom bottom sheet (no ModalBottomSheet) ────────────────────────────────
@Composable
private fun CustomBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    var sheetVisible by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) { sheetVisible = true }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows   = false
        )
    )  {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Scrim ────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = sheetVisible,
                enter = fadeIn(tween(300)),
                exit  = fadeOut(tween(220))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.50f))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val ev = awaitPointerEvent()
                                    if (ev.changes.all { !it.pressed }) onDismiss()
                                }
                            }
                        }
                )
            }

            // ── Sheet panel ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = sheetVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    tween(440, easing = EaseOutQuart),
                    initialOffsetY = { it }
                ) + fadeIn(tween(280)),
                exit  = slideOutVertically(
                    tween(280, easing = EaseInCubic),
                    targetOffsetY = { it }
                ) + fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, dragOffsetY.coerceAtLeast(0f).roundToInt()) }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (dragOffsetY > 160f) onDismiss() else dragOffsetY = 0f
                                }
                            ) { _, delta -> dragOffsetY += delta }
                        }
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            spotColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                        )
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    content = content
                )
            }
        }
    }
}

// ─── Sheet body ───────────────────────────────────────────────────────────────
@Composable
private fun ColumnScope.SheetBody(
    message: String,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    var stagger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); stagger = true }

    // Drag handle

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {

        // ❌ Close button (LEFT TOP)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp)
                .size(40.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Drag handle (CENTER)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .width(42.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
                )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 9.dp, bottom = 25

                .dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        // ── Icon badge ───────────────────────────────────────────────────
        StaggerItem() {
            Box(contentAlignment = Alignment.Center) {
                // Soft glow ring
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Filled circle
                val iconScale = remember { Animatable(0.4f) }
                LaunchedEffect(Unit) {
                    iconScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMediumLow
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .scale(iconScale.value)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Title ────────────────────────────────────────────────────────
        StaggerItem() {
            Text(
                text = "Premium Feature",
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.4).sp
            )
        }

        Spacer(Modifier.height(6.dp))

        // ── Message ──────────────────────────────────────────────────────
        StaggerItem() {
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Section label ────────────────────────────────────────────────
        StaggerItem() {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "  What's included  ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Feature rows ─────────────────────────────────────────────────
        premiumFeatures.forEachIndexed { i, feature ->
            StaggerItem() {
                PremiumFeatureRow(feature)
            }
            if (i < premiumFeatures.lastIndex) Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(24.dp))

        // ── Upgrade button ───────────────────────────────────────────────
        StaggerItem() {
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Upgrade to Premium",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    letterSpacing = 0.2.sp
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Dismiss ──────────────────────────────────────────────────────
    }
}

// ─── Feature row ─────────────────────────────────────────────────────────────
@Composable
fun PremiumFeatureRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector    = Icons.Default.CheckCircle,
            contentDescription = null,
            tint           = MaterialTheme.colorScheme.primary,
            modifier       = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text        = text,
            fontSize    = 13.sp,
            color       = MaterialTheme.colorScheme.onSurface,
            fontWeight  = FontWeight.Medium
        )
    }
}

// ─── Stagger animation helper ─────────────────────────────────────────────────
@Composable
private fun StaggerItem(
    content  : @Composable () -> Unit
) {
    content()
}

// ─── Easing helpers ───────────────────────────────────────────────────────────
private val EaseOutQuart = Easing { t -> val x = 1 - t; 1 - x * x * x * x }
private val EaseInCubic  = Easing { t -> t * t * t }