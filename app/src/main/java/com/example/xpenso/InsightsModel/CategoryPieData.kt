package com.example.xpenso.InsightsModel

import androidx.compose.ui.graphics.Color

/**
 * Data class to represent a slice in the pie chart.
 *
 * @param categoryName The name of the category (e.g., "Food", "Shopping").
 * @param amount The total amount for this category.
 * @param percentage The percentage of the total amount this category represents (0.0 to 100.0).
 * @param color The color associated with this category.
 */
/**
* Represents a segment in the donut pie chart, typically a category.
*/
data class CategoryPieData(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)
