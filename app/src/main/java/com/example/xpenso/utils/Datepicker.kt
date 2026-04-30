package com.example.xpenso.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import java.util.*

// ═══════════════════════════════════════════════
//  PUBLIC API
//
//  if (showDatePicker) {
//      PremiumDatePickerDialog(
//          initialSelectedMillis = selectedDateMillis,
//          onDateSelected = { expenseViewModel.updateSelectedDate(it) },
//          onDismiss = { showDatePicker = false }
//      )
//  }
// ═══════════════════════════════════════════════

@Composable
fun PremiumDatePickerDialog(
    initialSelectedMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val safeInitial = remember(initialSelectedMillis) {
        if (initialSelectedMillis == 0L) normalizeToMidnight(System.currentTimeMillis())
        else normalizeToMidnight(initialSelectedMillis)
    }
    var selectedMillis by remember { mutableStateOf(safeInitial) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PremiumCalendarCard(
            selectedMillis    = selectedMillis,
            onSelectionChange = { selectedMillis = it },
            onConfirm         = { onDateSelected(selectedMillis); onDismiss() },
            onDismiss         = onDismiss
        )
    }
}

// ───────────────────────────────────────────────
//  Root card — handles portrait & landscape
// ───────────────────────────────────────────────

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PremiumCalendarCard(
    selectedMillis: Long,
    onSelectionChange: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val config      = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    val selectedCal = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }

    var displayYear  by remember { mutableIntStateOf(selectedCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(selectedCal.get(Calendar.MONTH)) }
    var viewMode     by remember { mutableStateOf("CALENDAR") }
    var swipeOffset  by remember { mutableFloatStateOf(0f) }
    val haptic       = LocalHapticFeedback.current

    val primaryColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimary    = MaterialTheme.colorScheme.onPrimaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface    = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant

    // Width fraction: tighter in portrait, wider in landscape so it fits
    val widthFraction = if (isLandscape) 0.72f else 0.78f

    Box(
        modifier         = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape           = RoundedCornerShape(24.dp),
            color           = surfaceColor,
            tonalElevation  = 6.dp,
            shadowElevation = 18.dp,
            modifier        = Modifier.fillMaxWidth(widthFraction)
        ) {
            if (isLandscape) {
                // ── LANDSCAPE: banner left | calendar right ──
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    // Left panel — primary banner
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                            .background(primaryColor),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BannerContent(
                            selectedMillis = selectedMillis,
                            viewMode       = viewMode,
                            onPrimary      = onPrimary,
                            onYearClick    = {
                                viewMode = if (viewMode == "YEAR") "CALENDAR" else "YEAR"
                            },
                            vertical       = true
                        )
                    }

                    // Right panel — calendar or year picker + buttons
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                    ) {
                        CalendarOrYearBody(
                            viewMode       = viewMode,
                            displayYear    = displayYear,
                            displayMonth   = displayMonth,
                            selectedMillis  = selectedMillis,
                            swipeOffset    = swipeOffset,
                            primaryColor   = primaryColor,
                            onPrimary      = onPrimary,
                            onSurface      = onSurface,
                            onSurfaceVar   = onSurfaceVar,
                            onSwipeUpdate  = { swipeOffset = it },
                            onMonthChange  = { y, m -> displayYear = y; displayMonth = m },
                            onYearPick     = { y -> displayYear = y; viewMode = "CALENDAR" },
                            onSelectionChange = onSelectionChange,
                            onDisplaySync  = { millis ->
                                val c = Calendar.getInstance().apply { timeInMillis = millis }
                                displayYear  = c.get(Calendar.YEAR)
                                displayMonth = c.get(Calendar.MONTH)
                            }
                        )

                        HorizontalDivider(
                            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 0.5.dp
                        )
                        ActionButtons(
                            onDismiss = onDismiss,
                            onConfirm = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onConfirm() }
                        )
                    }
                }
            } else {
                // ── PORTRAIT: banner top | calendar below ──
                Column {
                    TopBanner(
                        selectedMillis = selectedMillis,
                        viewMode       = viewMode,
                        primaryColor   = primaryColor,
                        onPrimary      = onPrimary,
                        onYearClick    = {
                            viewMode = if (viewMode == "YEAR") "CALENDAR" else "YEAR"
                        }
                    )

                    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
                        Spacer(Modifier.height(4.dp))

                        CalendarOrYearBody(
                            viewMode       = viewMode,
                            displayYear    = displayYear,
                            displayMonth   = displayMonth,
                            selectedMillis  = selectedMillis,
                            swipeOffset    = swipeOffset,
                            primaryColor   = primaryColor,
                            onPrimary      = onPrimary,
                            onSurface      = onSurface,
                            onSurfaceVar   = onSurfaceVar,
                            onSwipeUpdate  = { swipeOffset = it },
                            onMonthChange  = { y, m -> displayYear = y; displayMonth = m },
                            onYearPick     = { y -> displayYear = y; viewMode = "CALENDAR" },
                            onSelectionChange = onSelectionChange,
                            onDisplaySync  = { millis ->
                                val c = Calendar.getInstance().apply { timeInMillis = millis }
                                displayYear  = c.get(Calendar.YEAR)
                                displayMonth = c.get(Calendar.MONTH)
                            }
                        )

                        HorizontalDivider(
                            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 0.5.dp
                        )
                        ActionButtons(
                            onDismiss = onDismiss,
                            onConfirm = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onConfirm() }
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────────
//  Shared: calendar OR year picker body
// ───────────────────────────────────────────────

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CalendarOrYearBody(
    viewMode: String,
    displayYear: Int,
    displayMonth: Int,
    selectedMillis: Long,
    swipeOffset: Float,
    primaryColor: Color,
    onPrimary: Color,
    onSurface: Color,
    onSurfaceVar: Color,
    onSwipeUpdate: (Float) -> Unit,
    onMonthChange: (Int, Int) -> Unit,
    onYearPick: (Int) -> Unit,
    onSelectionChange: (Long) -> Unit,
    onDisplaySync: (Long) -> Unit
) {
    AnimatedContent(
        targetState  = viewMode,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
        label        = "viewMode"
    ) { mode ->
        when (mode) {
            "YEAR" -> YearPickerView(
                selectedYear = displayYear,
                onYearPick   = onYearPick
            )
            else -> Column {
                MonthNavHeader(
                    displayYear  = displayYear,
                    displayMonth = displayMonth,
                    onPrev = {
                        val c = Calendar.getInstance().apply {
                            set(displayYear, displayMonth, 1); add(Calendar.MONTH, -1)
                        }
                        onMonthChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
                    },
                    onNext = {
                        val c = Calendar.getInstance().apply {
                            set(displayYear, displayMonth, 1); add(Calendar.MONTH, 1)
                        }
                        onMonthChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
                    }
                )
                Spacer(Modifier.height(4.dp))
                WeekDayRow(onSurfaceVar = onSurfaceVar)
                Spacer(Modifier.height(2.dp))

                AnimatedContent(
                    targetState  = displayYear * 12 + displayMonth,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(tween(280)) { it * dir } + fadeIn(tween(200))) togetherWith
                                (slideOutHorizontally(tween(280)) { -it * dir } + fadeOut(tween(150)))
                    },
                    label = "monthSlide"
                ) { _ ->
                    var localSwipe by remember { mutableFloatStateOf(0f) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, d -> localSwipe += d },
                                    onDragEnd = {
                                        if (localSwipe > 80) {
                                            val c = Calendar.getInstance().apply {
                                                set(displayYear, displayMonth, 1); add(Calendar.MONTH, -1)
                                            }
                                            onMonthChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
                                        } else if (localSwipe < -80) {
                                            val c = Calendar.getInstance().apply {
                                                set(displayYear, displayMonth, 1); add(Calendar.MONTH, 1)
                                            }
                                            onMonthChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
                                        }
                                        localSwipe = 0f
                                    }
                                )
                            }
                    ) {
                        CalendarGrid(
                            displayYear       = displayYear,
                            displayMonth      = displayMonth,
                            selectedMillis    = selectedMillis,
                            primaryColor      = primaryColor,
                            onPrimary         = onPrimary,
                            onSurface         = onSurface,
                            onSurfaceVar      = onSurfaceVar,
                            onDayClick        = { millis ->
                                onSelectionChange(millis)
                                onDisplaySync(millis)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────────
//  Action buttons row  [Cancel]  [Set]
// ───────────────────────────────────────────────

@Composable
private fun ActionButtons(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick  = onDismiss,
            modifier = Modifier.weight(1f).height(40.dp),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Cancel",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick  = onConfirm,
            modifier = Modifier.weight(1f).height(40.dp),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Text("Set", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ───────────────────────────────────────────────
//  Portrait top banner
// ───────────────────────────────────────────────

@Composable
private fun TopBanner(
    selectedMillis: Long,
    viewMode: String,
    primaryColor: Color,
    onPrimary: Color,
    onYearClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(primaryColor)
            .padding(start = 20.dp, top = 16.dp, bottom = 14.dp, end = 20.dp)
    ) {
        BannerContent(
            selectedMillis = selectedMillis,
            viewMode       = viewMode,
            onPrimary      = onPrimary,
            onYearClick    = onYearClick,
            vertical       = true
        )
    }
}

// ───────────────────────────────────────────────
//  Banner content — shared between portrait & landscape
// ───────────────────────────────────────────────

@Composable
private fun BannerContent(
    selectedMillis: Long,
    viewMode: String,
    onPrimary: Color,
    onYearClick: () -> Unit,
    vertical: Boolean
) {
    val cal = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }
    val dayNames   = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
    val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val dayName    = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
    val monthName  = monthNames[cal.get(Calendar.MONTH)]
    val dayNum     = cal.get(Calendar.DAY_OF_MONTH)
    val year       = cal.get(Calendar.YEAR)

    val padding = if (vertical) PaddingValues(0.dp) else PaddingValues(start = 16.dp, top = 20.dp, bottom = 20.dp, end = 8.dp)

    Column(modifier = Modifier.padding(padding)) {
        Text(
            text       = year.toString(),
            color      = onPrimary.copy(alpha = if (viewMode == "YEAR") 1f else 0.72f),
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onYearClick
                )
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text       = "$dayName, $dayNum $monthName",
            color      = onPrimary.copy(alpha = if (viewMode == "CALENDAR") 1f else 0.72f),
            fontSize   = if (vertical) 26.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { if (viewMode == "YEAR") onYearClick() }
        )
    }
}

// ───────────────────────────────────────────────
//  "< August 2026 >" month navigation
// ───────────────────────────────────────────────

@Composable
private fun MonthNavHeader(
    displayYear: Int,
    displayMonth: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val monthNames = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onPrev),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
        Text(
            text       = "${monthNames[displayMonth]} $displayYear",
            modifier   = Modifier.weight(1f),
            textAlign  = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
    }
}

// ───────────────────────────────────────────────
//  M T W T F S S
// ───────────────────────────────────────────────

@Composable
private fun WeekDayRow(onSurfaceVar: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        listOf("M","T","W","T","F","S","S").forEach { d ->
            Text(
                text       = d,
                modifier   = Modifier.weight(1f),
                textAlign  = TextAlign.Center,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = onSurfaceVar
            )
        }
    }
}

// ───────────────────────────────────────────────
//  Calendar grid
// ───────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    displayYear: Int,
    displayMonth: Int,
    selectedMillis: Long,
    primaryColor: Color,
    onPrimary: Color,
    onSurface: Color,
    onSurfaceVar: Color,
    onDayClick: (Long) -> Unit
) {
    val todayCal    = remember { Calendar.getInstance() }
    val selectedCal = remember(selectedMillis) { Calendar.getInstance().apply { timeInMillis = selectedMillis } }
    val cells       = remember(displayYear, displayMonth) { buildCalendarCells(displayYear, displayMonth) }

    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    val isSelected = cell != null &&
                            cell.get(Calendar.YEAR)         == selectedCal.get(Calendar.YEAR) &&
                            cell.get(Calendar.MONTH)        == selectedCal.get(Calendar.MONTH) &&
                            cell.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH)
                    val isToday = cell != null &&
                            cell.get(Calendar.YEAR)         == todayCal.get(Calendar.YEAR) &&
                            cell.get(Calendar.MONTH)        == todayCal.get(Calendar.MONTH) &&
                            cell.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH)
                    DayCell(
                        day            = cell?.get(Calendar.DAY_OF_MONTH),
                        isSelected     = isSelected,
                        isToday        = isToday,
                        isCurrentMonth = cell?.get(Calendar.MONTH) == displayMonth,
                        primaryColor   = primaryColor,
                        onPrimary      = onPrimary,
                        onSurface      = onSurface,
                        modifier       = Modifier.weight(1f),
                        onClick        = { if (cell != null) onDayClick(normalizeToMidnight(cell.timeInMillis)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    primaryColor: Color,
    onPrimary: Color,
    onSurface: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "dayScale"
    )
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1.0f)
            .scale(scale)
            .clip(CircleShape)
            .then(when {
                isSelected -> Modifier.background(MaterialTheme.colorScheme.primary)
                isToday    -> Modifier.border(1.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.98f),
                    CircleShape
                )
                else       -> Modifier
            })
            .clickable(
                enabled           = day != null,
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Text(
                text       = day.toString(),
                fontSize   = 12.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,

                color = when {
                    isSelected -> MaterialTheme.colorScheme.surface
//                    isToday        -> MaterialTheme.colorScheme.onPrimary
                    isCurrentMonth -> onSurface
                    else           -> onSurface.copy(alpha = 0.28f)
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

// ───────────────────────────────────────────────
//  Year picker drum-roll
// ───────────────────────────────────────────────

@Composable
private fun YearPickerView(selectedYear: Int, onYearPick: (Int) -> Unit) {
    val years     = (1970..2100).toList()
    val startIdx  = (years.indexOf(selectedYear) - 2).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIdx)
    val scope     = rememberCoroutineScope()
    var swipeAcc  by remember { mutableFloatStateOf(0f) }

    val onSurface    = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, d -> swipeAcc += d },
                    onDragEnd = {
                        val items = (-swipeAcc / 48.dp.toPx()).toInt()
                        scope.launch {
                            listState.animateScrollToItem(
                                (listState.firstVisibleItemIndex + items).coerceIn(0, years.size - 1)
                            )
                        }
                        swipeAcc = 0f
                    }
                )
            }
    ) {
        LazyColumn(
            state               = listState,
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding      = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(years) { _, year ->
                val isSel = year == selectedYear
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { onYearPick(year) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = year.toString(),
                        fontSize   = if (isSel) 24.sp else 15.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSel) onSurface else onSurfaceVar.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

// ───────────────────────────────────────────────
//  Helpers
// ───────────────────────────────────────────────

private fun buildCalendarCells(year: Int, month: Int): List<Calendar?> {
    val firstDay = Calendar.getInstance().apply {
        set(year, month, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val rawDow      = firstDay.get(Calendar.DAY_OF_WEEK)
    val startOffset = (rawDow - Calendar.MONDAY + 7) % 7
    val cells       = mutableListOf<Calendar?>()

    repeat(startOffset) { cells.add(null) }
    for (d in 1..firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        cells.add(Calendar.getInstance().apply { set(year, month, d, 0, 0, 0); set(Calendar.MILLISECOND, 0) })
    }
    while (cells.size < 42) cells.add(null)
    return cells
}

private fun normalizeToMidnight(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis