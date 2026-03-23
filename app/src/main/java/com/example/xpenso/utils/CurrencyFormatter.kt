package com.example.xpenso.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    fun format(
        amount: Double,
        currencySymbol: String
    ): String {
        return if (currencySymbol == "₹" || currencySymbol.equals("INR", true)) {
            formatIndian(amount)
        } else {
            formatInternational(amount)
        }
    }

    private fun formatIndian(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale("en", "IN"))
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(amount)
    }

    private fun formatInternational(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,##0.00", symbols)
        return formatter.format(amount)
    }
}


