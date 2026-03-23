package com.example.xpenso.InsightsModel

data class InsightStats(
    val total: Double,
    val avgDailySpend: Double,
    val highestDayAmount: Double,
    val highestDayLabel: String,
    val monthName: String
)
