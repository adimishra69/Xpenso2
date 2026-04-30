package com.example.xpenso.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object TypingCurrencyFormatter {

    fun format(
        amount: String,
        currencySymbol: String
    ): String {

        return try {

            val value = BigDecimal(amount)

            if (currencySymbol == "₹" || currencySymbol.equals("INR", true)) {
                formatIndian(value)
            } else {
                formatInternational(value)
            }

        } catch (e: Exception) {
            amount
        }
    }

    private fun formatIndian(amount: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("en", "IN"))
        val formatter = DecimalFormat("#,##,##0.##", symbols)
        return formatter.format(amount)
    }

    private fun formatInternational(amount: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,##0.##", symbols)
        return formatter.format(amount)
    }
}