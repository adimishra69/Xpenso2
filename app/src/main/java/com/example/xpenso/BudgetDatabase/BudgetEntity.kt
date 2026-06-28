package com.example.xpenso.BudgetDatabase

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["month", "year"], unique = true)]
)
data class BudgetEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 0 = January, 1 = February, ..., 11 = December
    val month: Int,

    // Example: 2026
    val year: Int,

    // Budget amount for this month
    val amount: Double
)