package com.example.xpenso.utils

import java.util.Calendar

object DateRangeUtils {

    fun getCycleRange(
        year: Int,
        month: Int,
        startOfMonth: Int
    ): LongRange {

        val start = Calendar.getInstance().apply {
            set(year, month, startOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val end = (start.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        return start.timeInMillis..end.timeInMillis
    }

    fun getPreviousCycleRange(
        year: Int,
        month: Int,
        startOfMonth: Int
    ): LongRange {

        val cal = Calendar.getInstance().apply {
            set(year, month, 1)
            add(Calendar.MONTH, -1)
        }

        return getCycleRange(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            startOfMonth
        )
    }
}