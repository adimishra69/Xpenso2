package com.example.xpenso.Home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState

@Composable
fun ScrollAwareFab(
    listState: LazyListState,
    visibleCondition: Boolean = true,
    onClick: () -> Unit
) {
    // Tracks scroll-based visibility (hide on scroll down, show on scroll up)
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        var previousIndex = 0
        var previousOffset = 0

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            // Only toggle visibility on actual scroll changes, not on recomposition
            val scrollingDown =
                index > previousIndex || (index == previousIndex && offset > previousOffset)
            val scrollingUp =
                index < previousIndex || (index == previousIndex && offset < previousOffset)

            when {
                scrollingDown -> isFabVisible = false
                scrollingUp  -> isFabVisible = true
            }

            previousIndex = index
            previousOffset = offset
        }
    }

    AnimatedVisibility(
        visible = isFabVisible && visibleCondition,
        enter = slideInVertically(
            // Slower slide-in from below — feels calm and deliberate
            animationSpec = tween(
                durationMillis = 600,
                easing = LinearOutSlowInEasing
            )
        ) { fullHeight -> fullHeight + 200 } + fadeIn(
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = 60
            )
        ),
        exit = slideOutVertically(
            // Slightly slower exit — stoic, unhurried
            animationSpec = tween(
                durationMillis = 550,
                easing = FastOutSlowInEasing
            )
        ) { fullHeight -> fullHeight + 200 } + fadeOut(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 40
            )
        )
    ) {
        FloatingActionButton(
            modifier = Modifier
                .size(52.dp),
            // No press scale, no bounce — stable and stoic on click
            onClick = onClick,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Expense",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}