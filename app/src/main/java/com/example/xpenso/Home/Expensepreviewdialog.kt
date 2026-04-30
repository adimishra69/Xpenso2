package com.example.xpenso.Home

import android.R.attr.category
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.xpenso.Transaction.CategoryIcon
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.CurrencyFormatter
import kotlinx.coroutines.launch


// ─────────────────────────────────────────────────────────────────────────────
// DATA MODEL  (unchanged)
// ─────────────────────────────────────────────────────────────────────────────

data class ExpensePreviewData(
    val id: String = "",
    val amount: String,
    val title: String,
    val category: String,
    val categoryEmoji: String,
    val paymentMode: String,
    val dateTime: String,
    val notes: String = "",
    val currencySymbol: String = "₹"
)


// ─────────────────────────────────────────────────────────────────────────────
// MAIN PREVIEW DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ExpensePreviewDialog(
    expense: ExpensePreviewData,
    onDismiss: () -> Unit,
    onEdit: (ExpensePreviewData) -> Unit,
    onDelete: (ExpensePreviewData) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ── Animation state ───────────────────────────────────────────────────────
    val offsetY       = remember { Animatable(600f) }
    val overlayAlpha  = remember { Animatable(0f) }
    val contentScale  = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness    = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            overlayAlpha.animateTo(1f, tween(260))
        }
        launch {
            contentScale.animateTo(
                1f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
            )
        }
    }

    fun animateDismiss() {
        scope.launch {
            launch {
                offsetY.animateTo(
                    targetValue  = 700f,
                    animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
                )
            }
            launch { overlayAlpha.animateTo(0f, tween(200)) }
        }.invokeOnCompletion { onDismiss() }
    }

    Dialog(
        onDismissRequest = ::animateDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth  = false,
            decorFitsSystemWindows   = false
        )
    ) {
        // ── Scrim ─────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayAlpha.value }
                .background(Color.Black.copy(alpha = 0.15f * overlayAlpha.value))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = ::animateDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // ── Sheet ─────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = offsetY.value
                        scaleX       = contentScale.value
                        scaleY       = contentScale.value
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (offsetY.value > 100f) {
                                    animateDismiss()
                                } else {
                                    scope.launch {
                                        offsetY.animateTo(
                                            0f,
                                            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    offsetY.animateTo(
                                        0f,
                                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                                    )
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                scope.launch {
                                    // Resist upward drag, allow free downward
                                    val next = (offsetY.value + dragAmount).coerceAtLeast(-10f)
                                    offsetY.snapTo(next)
                                }
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = {}
                    ),
                shape         = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color         = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 12.dp
            ) {
                SheetContent(
                    expense       = expense,
                    onClose       = ::animateDismiss,
                    onEdit        = { onEdit(expense) },
                    onDeleteClick = { showDeleteConfirm = true }
                )
            }
        }
    }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            expense   = expense,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete(expense)
                onDismiss()

                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )
                Toast.makeText(
                    context,
                    "Expense deleted",
                    Toast.LENGTH_SHORT
                ).show()

            }
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// SHEET CONTENT  — purely visual, zero logic
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SheetContent(
    expense: ExpensePreviewData,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Staggered section reveal
    val sectionAlpha  = remember { Animatable(0f) }
    val amountScale   = remember { Animatable(0.86f) }

    LaunchedEffect(Unit) {
        launch {
            sectionAlpha.animateTo(1f, tween(durationMillis = 340, delayMillis = 130))
        }
        launch {
            amountScale.animateTo(
                1f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
            )
        }
    }



    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {

        // ── Drag handle ───────────────────────────────────────────────────────
//        Box(
//
//            modifier = Modifier
//                .padding(top = 16.dp, bottom = 4.dp)
//                .width(36.dp)
//                .height(5.dp)
//                .clip(CircleShape)
//                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
//                .align(Alignment.CenterHorizontally)
//        )

        // ── Header row ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            PreviewIconButton(onClick = onClose, contentDescription = "Close") {
                Icon(
                    imageVector     = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier        = Modifier.size(18.dp),
                    tint            = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PreviewIconButton(onClick = onEdit, contentDescription = "Edit") {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier           = Modifier.size(18.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PreviewIconButton(
                    onClick          = onDeleteClick,
                    contentDescription = "Delete",
                    isDestructive    = true
                ) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier           = Modifier.size(18.dp),
                        tint               = MaterialTheme.colorScheme.error.copy(0.87f)
                    )
                }
            }
        }

        // ── Amount ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp)
                .graphicsLayer {
                    scaleX = amountScale.value
                    scaleY = amountScale.value
                },
            contentAlignment = Alignment.Center
        ) {
            val amountValue = expense.amount.toDoubleOrNull() ?: 0.0
            val formattedAmount = CurrencyFormatter.format(
                amountValue,
                expense.currencySymbol
            )

            val digitCount = formattedAmount.filter { it.isDigit() }.length


            val baseAmountSize = 42.sp
            val baseCurrencySize = MaterialTheme.typography.headlineMedium.fontSize

            val reduction = when {
                digitCount >= 8 -> 2.sp
                else -> 0.sp
            }
            val finalAmountSize = (baseAmountSize.value - reduction.value).sp
            val finalCurrencySize = (baseCurrencySize.value - reduction.value).sp

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text     = expense.currencySymbol,
                    style    = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = finalCurrencySize
                    ),
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, end = 3.dp)
                )
                Text(
                    text  = formattedAmount,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize      = finalAmountSize,
                        letterSpacing = (-1.5).sp,
                        fontWeight    = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ── Category + date ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .graphicsLayer { alpha = sectionAlpha.value },
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val detected = ExpenseCategory.detect(expense.title)

                val category = if (
                    expense.category.equals(detected.name, ignoreCase = true)
                ) {
                    detected
                } else {
                    ExpenseCategory.entries.find {
                        it.name.equals(expense.category, ignoreCase = true)
                    } ?: ExpenseCategory.OTHER
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
//                    border = BorderStroke(
//                        0.7.dp,
//                        MaterialTheme.colorScheme.outline   // ✅ use outline color
//                    ),
                    modifier = Modifier.size(30.dp)
                ) {

                    Box(contentAlignment = Alignment.Center) {

                        CategoryIcon(
                            category = category,
                            tint = category.color,
                            size = 18.dp
                        )

                    }
                }
                Text(
                    text = expense.title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text         = expense.dateTime,
                style        = MaterialTheme.typography.bodySmall,
                color        = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.2.sp
            )
        }

        // ── Divider ───────────────────────────────────────────────────────────
        HorizontalDivider(
            modifier  = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .graphicsLayer { alpha = sectionAlpha.value },
            thickness = 0.8.dp,
            color     = MaterialTheme.colorScheme.outlineVariant
        )

        // ── Chips ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            PreviewPaymentChip(
                paymentMode = expense.paymentMode
            )
        }

        // ── Notes ─────────────────────────────────────────────────────────────
        if (expense.notes.isNotBlank()) {
            // ── Notes ─────────────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .graphicsLayer { alpha = sectionAlpha.value }
            ) {

                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = if (expense.notes.trim().isEmpty()) "No notes" else expense.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewPaymentChip(
    paymentMode: String
) {
    val icon = when (paymentMode) {
        "Cash" -> Icons.Rounded.Payments
        "UPI" -> Icons.Rounded.QrCode
        "Debit Card", "Credit Card" -> Icons.Rounded.CreditCard
        else -> Icons.Rounded.CreditCard
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
                0.8.dp,
        MaterialTheme.colorScheme.outlineVariant
      )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = paymentMode,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DELETE CONFIRMATION DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DeleteConfirmDialog(
    expense: ExpensePreviewData,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scope      = rememberCoroutineScope()
    val dialScale  = remember { Animatable(0.88f) }
    val dialAlpha  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            dialScale.animateTo(
                1f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
            )
        }
        launch { dialAlpha.animateTo(1f, tween(200)) }
    }

    fun animateOut(then: () -> Unit) {
        scope.launch {
            launch { dialScale.animateTo(0.93f, tween(160)) }
            launch { dialAlpha.animateTo(0f, tween(160)) }
        }.invokeOnCompletion { then() }
    }

    Dialog(
        onDismissRequest = { animateOut(onDismiss) },
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = dialAlpha.value }
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = { animateOut(onDismiss) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .graphicsLayer {
                        scaleX = dialScale.value
                        scaleY = dialScale.value
                        alpha  = dialAlpha.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = {}
                    ),
                shape          = RoundedCornerShape(24.dp),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        text       = "Delete expense?",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "This will permanently remove the expense",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(18.dp))

                    // Expense preview pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier              = Modifier.padding(14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape    = RoundedCornerShape(13.dp),
                                color    = MaterialTheme.colorScheme.surfaceVariant,
//                                border = BorderStroke(
//                                    0.7.dp,
//                                    MaterialTheme.colorScheme.outline   // ✅ use outline color
//                                ),
                            ) {

                                val detected = ExpenseCategory.detect(expense.title)

                                val category = if (
                                    expense.category.equals(detected.name, ignoreCase = true)
                                ) {
                                    detected
                                } else {
                                    ExpenseCategory.entries.find {
                                        it.name.equals(expense.category, ignoreCase = true)
                                    } ?: ExpenseCategory.OTHER
                                }
                                Box(contentAlignment = Alignment.Center) {
                                    CategoryIcon(
                                        category = category,
                                        tint = category.color,
                                        size = 20.dp
                                    )
                                }
                            }
                            Column {
                                val formattedAmount = CurrencyFormatter.format(
                                    expense.amount.toDoubleOrNull() ?: 0.0,
                                    expense.currencySymbol
                                )

                                Row(verticalAlignment = Alignment.Top) {

                                    Text(
                                        text = expense.currencySymbol,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = MaterialTheme.typography.titleLarge.fontSize * 0.70
                                        ),
                                        color = MaterialTheme.colorScheme.error.copy(0.75f),
                                    )

                                    Text(
                                        text = formattedAmount,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.error.copy(0.75f)
                                    )
                                }
                                Text(
                                    text  = expense.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.72f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { animateOut(onDismiss) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                0.8.dp,
                                MaterialTheme.colorScheme.outline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text       = "Cancel",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick  = { animateOut(onConfirm) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(0.79f),
                                contentColor   = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Delete,
                                contentDescription = null,
                                modifier           = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = "Delete",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PRIVATE HELPERS
// ─────────────────────────────────────────────────────────────────────────────

/** 40dp circular button — surfaceVariant bg, ripple via Surface.onClick, optional error tint */
@Composable
private fun PreviewIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    isDestructive: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        onClick        = onClick,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        modifier       = Modifier
            .size(40.dp)
            .clip(CircleShape),
        shape          = CircleShape,
        color          = if (isDestructive)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.20f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { content() }
    }
}



