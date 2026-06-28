package com.example.xpenso.BudgetDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getBudget(month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    suspend fun getAllBudgets(): List<BudgetEntity>

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    suspend fun deleteBudget(month: Int, year: Int)


    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}