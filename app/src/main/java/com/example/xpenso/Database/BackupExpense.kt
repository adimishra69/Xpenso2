package com.example.xpenso.Database

/**
 * A temporary data class used only for creating a human-readable JSON backup.
 * The `timestamp` is a formatted String, which is easier to read than a Long.
 */
data class BackupExpense(
    val id: Long,
    val amount: Double,
    val category: String,
    val description: String?,
    val date: String // Changed from timestamp for readability
)