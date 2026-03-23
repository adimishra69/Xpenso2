package com.example.xpenso.utils


import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

fun formatIndianCurrency(amount: Double): String {
    val indianLocale = Locale.forLanguageTag("en-IN")
    val formatter = NumberFormat.getCurrencyInstance(indianLocale)
    val formatted = formatter.format(amount)

    return if (formatted.isNotEmpty() && !formatted[0].isDigit()) {
        formatted.substring(1).trim()
    } else {
        formatted
    }
}

fun formatAmountTwoDecimal(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

fun mergeDateWithCurrentTime(dateMillis: Long): Long {
    val calendar = Calendar.getInstance()
    val currentTime = Calendar.getInstance()

    calendar.timeInMillis = dateMillis
    calendar.set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY))
    calendar.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE))
    calendar.set(Calendar.SECOND, currentTime.get(Calendar.SECOND))
    calendar.set(Calendar.MILLISECOND, currentTime.get(Calendar.MILLISECOND))

    return calendar.timeInMillis
}
