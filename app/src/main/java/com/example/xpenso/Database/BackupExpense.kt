package com.example.xpenso.Database

/**
 * A temporary data class used only for creating a human-readable JSON backup.
 * The `timestamp` is a formatted String, which is easier to read than a Long.
 */
data class BackupExpense(
    val id: Long,
    val title: String?,
    val amount: Double,
    val category: String?,
    val notes: String?,
    val paymentMode: String?,
//    val currencySymbol: String?,
    val date: String
)