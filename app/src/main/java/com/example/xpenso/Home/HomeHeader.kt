package com.example.xpenso.Home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
//import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun Modifier.headerPill() = this
    .height(40.dp)
    .wrapContentWidth()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
//    showDot: Boolean,
    isNext: Boolean,
    selectedDateDisplay: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onMonthSummaryClick: () -> Unit,
    onDatePickerClick: () -> Unit
) {

    var previousDate = remember { selectedDateDisplay }
    LaunchedEffect(selectedDateDisplay) {
        previousDate = selectedDateDisplay
    }
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column {

            /* ---------- TOP BAR ---------- */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Xpenso",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

//                IconButton(
//                    onClick = onNotificationClick,
//                    modifier = Modifier.size(48.dp)
//                ) {
//                    BadgedBox(
//                        badge = { if (showDot) Badge() }
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Notifications,
//                            contentDescription = "Notifications",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
            }

            /* ---------- HEADER ---------- */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    onClick = onMonthSummaryClick,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        .69f.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.width(7.dp))

                        Text(
                            text = "Monthly",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        0.69.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // 🔹 LEFT ARROW
                        Box(
                            modifier = Modifier
                                .size(25.dp)
                                .clip(RoundedCornerShape(50)) // 🔥 makes ripple circular
                                .clickable { onPreviousDay() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))
                        // 🔹 DATE TEXT
                        AnimatedContent(
                            targetState = selectedDateDisplay,
                            transitionSpec = {
                                if (isNext) {
                                    // 👉 NEXT (forward) → slide UP
                                    slideInVertically { it } + fadeIn() togetherWith
                                            slideOutVertically { -it } + fadeOut()
                                } else {
                                    // 👉 PREVIOUS (back) → slide DOWN
                                    slideInVertically { -it } + fadeIn() togetherWith
                                            slideOutVertically { it } + fadeOut()
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .clickable { onDatePickerClick() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) { targetText ->

                            Text(
                                text = targetText,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // 🔹 RIGHT ARROW
                        val interaction = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .size(25.dp)
                                .clip(RoundedCornerShape(50)) // 🔥 makes ripple circular
                                .clickable { onNextDay() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 🔹 SPACE BEFORE C
                    }
                }
            }

            // ✅ ONLY ONE LINE BELOW ALL CONTENT
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}