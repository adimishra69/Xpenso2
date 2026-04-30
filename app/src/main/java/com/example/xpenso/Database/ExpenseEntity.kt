package com.example.xpenso.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val amount: Double,

    val category: String, // Add this: To store "FOOD", "BILL", etc.

    val notes: String = "",

    val currencySymbol: String,

    val timestamp: Long,

    val paymentMode: String = "", // Added paymentMode field
)
