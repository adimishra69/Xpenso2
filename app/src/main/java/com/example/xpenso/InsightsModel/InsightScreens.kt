package com.example.xpenso.InsightsModel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.PremiumHandler.PremiumFeatureSheet
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.InsightYearView.YearView
import com.example.xpenso.SettingsStore
import com.example.xpenso.utils.AppTopBar

// This enum is already in your file
private enum class InsightTab {
    MONTH, YEAR
}

/* ---------------------------------------------------------
   INSIGHTS SCREEN — PREMIUM REDESIGN
   Logic: 100% unchanged  |  Design: premium elevated
---------------------------------------------------------- */
@Composable
fun InsightsScreen(
    currencySymbol: String,
    expenseViewModel: ExpenseViewModel,
    settingsStore: SettingsStore,
    onCategoryClick: (String, Long, Long) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val currentStartDay by settingsStore.startOfMonth.collectAsState(initial = 1)
    val isPremium by settingsStore.isPremium.collectAsState(initial = false)

    LaunchedEffect(currentStartDay) { expenseViewModel.updateStartDay(currentStartDay) }
    LaunchedEffect(Unit) { AnalyticsHelper.trackScreen("Insights") }

    var selectedTab by remember { mutableStateOf(InsightTab.MONTH) }
    val tabs = listOf("Month", "Year")
    var showPremiumSheet by remember { mutableStateOf(false) }
    var premiumMessage by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {

        AppTopBar("Insights")

        // ── Premium pill-style tab switcher ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.6f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    val tab = InsightTab.entries[index]
                    val isSelected = selectedTab == tab
                    val isLocked = tab == InsightTab.YEAR && !isPremium

                    // Animate the indicator weight/alpha
                    val pillAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(220),
                        label = "pill_alpha"
                    )
                    val contentAlpha = if (isLocked) 0.45f else 1f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Selected pill
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = pillAlpha)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(colorScheme.primary.copy(.9f))
                            )
                        }

                        // Tab content
                        TextButton(
                            onClick = {
                                if (isLocked) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showPremiumSheet = true
                                    premiumMessage =
                                        "Yearly insights are available in Xpenso Premium"
                                } else {
                                    selectedTab = tab
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.graphicsLayer(alpha = contentAlpha)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        letterSpacing = 0.3.sp
                                    ),
                                    color = if (isSelected)
                                        colorScheme.onPrimary
                                    else
                                        colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hairline separator
        HorizontalDivider(
            thickness = 0.5.dp,
            color = colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        // ── Content switcher (logic unchanged) ──────────────────────────────
        when (selectedTab) {
            InsightTab.MONTH -> {
                MonthViewContent(
                    currencySymbol = currencySymbol,
                    expenseViewModel = expenseViewModel,
                    settingsStore = settingsStore,
                    startOfMonth = currentStartDay,
                    onCategoryClick = onCategoryClick,
//                    onPremiumRequired = {
//                        showPremiumSheet = true
//                        premiumMessage =
//                            "View transactions for a specific category with Xpenso Premium"
//                    }
                )
            }

            InsightTab.YEAR -> {
                YearView(
                    currencySymbol = currencySymbol,
                    expenseViewModel = expenseViewModel,
                    settingsStore = settingsStore
                )
            }
        }
    }

    // ── Premium sheet (logic unchanged) ─────────────────────────────────────
    if (showPremiumSheet) {
        PremiumFeatureSheet(
            message = premiumMessage,
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                showPremiumSheet = false
                onNavigateToPremium()
            }
        )
    }
}