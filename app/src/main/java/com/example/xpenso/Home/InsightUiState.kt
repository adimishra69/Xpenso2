package com.example.xpenso.Home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class InsightUiState(
    val title: String,
    val subtitle: String?,
    val color: Color,
    val icon: ImageVector
)
