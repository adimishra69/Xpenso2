package com.example.xpenso.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    /**
     * Returns a non-flowing list of all expenses.
     * Used for the backup process.
     */
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesList(): List<ExpenseEntity>

    /**
     * Inserts a list of expenses into the database.
     * Used for the restore process.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int


}