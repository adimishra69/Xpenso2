package com.example.xpenso.utils

import kotlin.math.abs
import kotlin.math.floor

object CurrencyCompactFormatter {

    fun format(
        amount: Double,
        currencySymbol: String
    ): String {

        val value = abs(amount)

        return if (isIndian(currencySymbol)) {
            formatIndianExtended(value)
        } else {
            formatInternational(value)
        }
    }

    private fun isIndian(symbol: String): Boolean {
        return symbol == "₹" || symbol.equals("INR", true)
    }

    // 🇮🇳 Indian compact units
    private fun formatIndianExtended(value: Double): String {

        fun formatNumber(num: Double): String {
            val rounded = kotlin.math.floor(num * 10) / 10
            return if (rounded % 1 == 0.0)
                rounded.toInt().toString()
            else
                String.format("%.1f", rounded)
        }

        return when {
            value >= 1_00_00_00_00_000 ->
                "${formatNumber(value / 1_00_00_00_00_000)} Kh"

            value >= 1_00_00_00_000 ->
                "${formatNumber(value / 1_00_00_00_000)} Ar"

            value >= 1_00_00_000 ->
                "${formatNumber(value / 1_00_00_000)} Cr"

            value >= 1_00_000 ->
                "${formatNumber(value / 1_00_000)} L"

            value >= 1_000 ->
                "${formatNumber(value / 1_000)} K"

            else ->
                value.toInt().toString()
        }
    }

}

// 🌍 International compact units
private fun formatInternational(value: Double): String {

    fun formatNumber(num: Double): String {
        val rounded = floor(num * 10) / 10
        return if (rounded % 1 == 0.0)
            rounded.toInt().toString()
        else
            String.format("%.1f", rounded)
    }

    return when {
        value >= 1_000_000_000_000 ->
            "${formatNumber(value / 1_000_000_000_000)} T"

        value >= 1_000_000_000 ->
            "${formatNumber(value / 1_000_000_000)} B"

        value >= 1_000_000 ->
            "${formatNumber(value / 1_000_000)} M"

        value >= 1_000 ->
            "${formatNumber(value / 1_000)} K"

        else ->
            value.toInt().toString()
    }
}

