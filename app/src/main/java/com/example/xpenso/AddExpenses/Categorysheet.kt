package com.example.xpenso.AddExpenses

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.xpenso.Transaction.CategoryIcon
import com.example.xpenso.Transaction.ExpenseCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.chunked
import kotlin.collections.forEach

@Composable
fun CategorySection(
    selectedCategory: ExpenseCategory?,
    isSuggested: Boolean = false,
    onCategoryChange: (ExpenseCategory ) -> Unit,
    onClick: () -> Unit
) {
    var sheetVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isSuggested) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val  bgColor by animateColorAsState(
        targetValue = if (isSuggested)
            selectedCategory?.color?.copy(alpha = 0.18f)
                ?: MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(250),
        label = "bgColor"
    )

    Column(
        modifier = Modifier.padding( vertical = 8.dp)
    ) {

        Text(
            text = "CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.6.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                    sheetVisible = true
                },
            color = bgColor,
//            shadowElevation = elevation
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (selectedCategory != null) {
                    CategoryIcon(
                        category = selectedCategory,
                        tint = selectedCategory.color,
                        size = 20.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                val categoryText = selectedCategory?.displayName() ?: "Select category"

                Text(
                    text = categoryText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (sheetVisible) {
        CategoryBottomSheet(
            currentSelection = selectedCategory,
            onSelect = {
                onCategoryChange(it)
                sheetVisible = false
            },
            onDismiss = { sheetVisible = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    currentSelection: ExpenseCategory?,
    onSelect: (ExpenseCategory) -> Unit,
    isVertical: Boolean = false,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Animation state
    val offsetY = remember { Animatable(600f) }
    val overlayAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.95f) }

    // Track whether dismiss was intentionally triggered
    var isDismissing by remember { mutableStateOf(false) }

    fun animateDismiss(then: () -> Unit) {
        if (isDismissing) return
        isDismissing = true
        scope.launch {
            launch { offsetY.animateTo(600f, tween(280, easing = FastOutLinearInEasing)) }
            launch { overlayAlpha.animateTo(0f, tween(220)) }
            launch { contentScale.animateTo(0.95f, tween(250)) }
            delay(300)
            then()
        }
    }

    LaunchedEffect(Unit) {
        launch {
            offsetY.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch { overlayAlpha.animateTo(1f, tween(220)) }
        launch {
            contentScale.animateTo(
                1f,
                spring(
                    Spring.DampingRatioMediumBouncy,
                    Spring.StiffnessMedium
                )
            )
        }
    }
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val density = LocalDensity.current

    val screenHeightPx = with(density) { screenHeightDp.dp.toPx() }
    // Full-screen overlay using Popup to sit above everything
    Dialog(
        onDismissRequest = { animateDismiss(onDismiss) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false // 🔥 MOST IMPORTANT
        )
    )  {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.15f * overlayAlpha.value)
                )
                // Tap on scrim = intentional dismiss
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { animateDismiss(onDismiss) },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                val newOffset = (offsetY.value + dragAmount)
                                    .coerceAtLeast(0f) // no upward drag

                                scope.launch {
                                    offsetY.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                val threshold = screenHeightPx * 0.25f   // 35% drag needed // 🔥 control here

                                scope.launch {
                                    if (offsetY.value > threshold) {
                                        animateDismiss(onDismiss) // ✅ close
                                    } else {
                                        offsetY.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        ) // ❌ bounce back
                                    }
                                }
                            }
                        )
                    }
                    // Block scrim click from passing through the sheet
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* consume, do nothing */ }
                    .graphicsLayer {
                        translationY = offsetY.value
                        scaleX = contentScale.value
                        scaleY = contentScale.value
                        alpha = overlayAlpha.value
                    },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 12.dp
            ) {
                if (isVertical) {
                    // ── Vertical layout ──────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = 8.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = "Select Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .weight(1f, fill = false)
                        ) {
                            ExpenseCategory.entries
                                .sortedWith(
                                    compareBy<ExpenseCategory> { it == ExpenseCategory.OTHER }
                                        .thenBy { it.name }
                                )
                                .forEach { category ->
                                    CategoryVerticalItem(
                                        category = category,
                                        selected = currentSelection == category,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        onClick = { onSelect(category) }
                                    )
                                }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        TextButton(
                            onClick = { animateDismiss(onDismiss) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Cancel",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                } else {
                    // ── Horizontal / Grid layout (original) ──────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .navigationBarsPadding()
                            .padding( bottom = 27.dp)
                    ) {
                        Text(
                            text = "Select Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding( top = 16.dp, bottom = 4.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ExpenseCategory.entries
                            .sortedWith(
                                compareBy<ExpenseCategory> { it == ExpenseCategory.OTHER }
                                    .thenBy { it.name }
                            )
                            .chunked(3)
                            .forEachIndexed { index, row ->
                                val isLastRow = row.size == 1
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
//                                    if (isLastRow) {
//                                        Spacer(Modifier.weight(1f))
//                                        row.forEach { category ->
//                                            CategoryGridItem(
//                                                category = category,
//                                                selected = currentSelection == category,
//                                                modifier = Modifier.weight(1f),
//                                                onClick = { onSelect(category) }
//                                            )
//                                        }
//                                        Spacer(Modifier.weight(1f))
//                                    } else {
                                        row.forEach { category ->
                                            CategoryGridItem(
                                                category = category,
                                                selected = currentSelection == category,
                                                modifier = Modifier.weight(1f),
                                                onClick = { onSelect(category) }
                                            )
                                        }
                                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                                    }
//                                }
                            }

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        TextButton(
                            onClick = { animateDismiss(onDismiss) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Cancel",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

fun ExpenseCategory.displayName(): String {
    return this.name
        .lowercase()
        .split("_")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
