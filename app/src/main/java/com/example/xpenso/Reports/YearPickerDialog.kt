package com.example.xpenso.Reports

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.*

@Composable
fun YearPickerDialog(
    selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    onSelected: (year: Int) -> Unit,
    onDismiss: () -> Unit
) {

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var pickedYear by remember { mutableStateOf(selectedYear) }

    val years = ((currentYear + 50) downTo (currentYear - 100)).toList()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val idx = years.indexOf(pickedYear)
        if (idx >= 0) listState.scrollToItem(maxOf(0, idx - 2))
    }

    LaunchedEffect(pickedYear) {

        val index = years.indexOf(pickedYear)

        if (index >= 0) {
            listState.animateScrollToItem(
                index = maxOf(0, index - 2)   // keeps it slightly centered
            )
        }
    }
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primaryCon =  MaterialTheme.colorScheme.onPrimaryContainer

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {

            Column {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                                )
                            ),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(24.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(
                            Icons.Rounded.CalendarMonth,
                            null,
                            tint = primaryCon.copy(.7f),
                            modifier = Modifier.size(26.dp)
                        )

                        Spacer(Modifier.width(14.dp))

                        Column {

                            Text(
                                "Select Year",
                                style = MaterialTheme.typography.labelMedium,
                                color = primaryCon.copy(alpha = 0.7f),
                            )

                            Spacer(Modifier.height(2.dp))

                            Text(
                                "$pickedYear",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryCon
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Arrow Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "Choose year",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(.45f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        IconButton(
                            onClick = {
                                pickedYear = pickedYear - 1
                            }
                        ) {
                            Icon(Icons.Rounded.KeyboardArrowLeft, null)
                        }

                        Text(
                            "$pickedYear",
                            modifier = Modifier.width(70.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )

                        IconButton(
                            onClick = {
                                pickedYear = pickedYear + 1
                            }
                        ) {
                            Icon(Icons.Rounded.KeyboardArrowRight, null)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Year List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .height(220.dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    items(years) { year ->

                        val selected = year == pickedYear

                        val bg by animateColorAsState(
                            if (selected) primary else Color.Transparent
                        )

                        val alpha by animateFloatAsState(
                            if (selected) 1f else .6f
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null
                                ) {
                                    pickedYear = year
                                }
                                .padding(14.dp)
                                .alpha(alpha),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                "$year",
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) onPrimary else onSurface
                            )

                            if (year == currentYear) {
                                YearBadge("This year", selected, primary, onPrimary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel" , color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { onSelected(pickedYear) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

}

@Composable
private fun YearBadge(
    label: String,
    selected: Boolean,
    primary: Color,
    onPrimary: Color
) {
    Surface(
        shape = CircleShape,
        color = if (selected)
            onPrimary.copy(.18f)
        else
            primary.copy(.12f)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) onPrimary else primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
