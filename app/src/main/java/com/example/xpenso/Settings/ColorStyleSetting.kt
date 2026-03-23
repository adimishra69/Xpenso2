package com.example.xpenso.Settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import com.example.xpenso.SettingsStore

@Composable
fun ColorStyleSetting(
    colorStyle: Int,
    settingsStore: SettingsStore,
    scope: CoroutineScope
) {

    Spacer(Modifier.height(20.dp))

    Text(
        text = "Color Style",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(Modifier.height(8.dp))

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {

        listOf("App", "System").forEachIndexed { index, label ->

            SegmentedButton(
                selected = colorStyle == index,
                onClick = {
                    scope.launch {
                        settingsStore.saveColorStyle(index)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index, 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}