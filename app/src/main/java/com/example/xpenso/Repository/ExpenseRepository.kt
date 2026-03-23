package com.example.xpenso.Repository


import android.content.Context
import android.net.Uri
import com.example.xpenso.Database.BackupExpense
import com.example.xpenso.Database.ExpenseDao
import com.example.xpenso.Database.ExpenseEntity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {

    val allExpenses: Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses()

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun getAllExpensesNow(): List<ExpenseEntity> {
        return expenseDao.getAllExpensesList()
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }
    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
    suspend fun getExpenseCount(): Int {
        return expenseDao.getExpenseCount()
    }
    suspend fun deleteAllExpenses() {
        expenseDao.deleteAllExpenses()
    }

    /**
     * Exports all expense data to a human-readable JSON file via a given URI.
     * @return A Result wrapper indicating success or an Exception on failure.
     */
    suspend fun exportData(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val expenses = expenseDao.getAllExpensesList()
            if (expenses.isEmpty()) {
                return@withContext Result.failure(Exception("No data to export."))
            }

            // Convert to backup-friendly format with readable dates
            val backupExpenses = expenses.map { it.toBackupExpense() }

            // Use Gson with Pretty Printing
            val gson = GsonBuilder().setPrettyPrinting().create()
            val backupWrapper = mapOf(
                "app" to "Xpenso",
                "version" to 1,
                "backup_type" to "expenses",
                "exported_at" to System.currentTimeMillis(),
                "data" to backupExpenses
            )

            val jsonString = gson.toJson(backupWrapper)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: return@withContext Result.failure(Exception("Failed to open output stream."))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Imports expense data from a JSON file URI.
     * @param uri The URI of the JSON file to import.
     * @param replaceExisting If true, all current data will be deleted before importing.
     * @return A Result wrapper indicating success or failure.
     */
    suspend fun importData(context: Context, uri: Uri, replaceExisting: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Read JSON string from the URI using ContentResolver
            val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }

            if (jsonString.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Selected file is empty or could not be read."))
            }

            val gson = GsonBuilder().create()
            val jsonObject = gson.fromJson(jsonString, Map::class.java)
            val dataJson = gson.toJson(jsonObject["data"])

            val type = object : TypeToken<List<BackupExpense>>() {}.type
            val restoredBackupExpenses: List<BackupExpense> = gson.fromJson(dataJson, type)
            // Convert from backup format back to database entity format
            val sanitizedExpenses = restoredBackupExpenses.map { it.toExpenseEntity() }

            // Perform database operations
            if (replaceExisting) {
                expenseDao.deleteAllExpenses()
            }
            expenseDao.insertAll(sanitizedExpenses)

            Result.success(Unit)
        } catch (e: Exception) {
            // This catches file reading errors, JSON parsing errors (JsonSyntaxException), etc.
            Result.failure(Exception("Invalid file format or corrupted data. ${e.message}"))
        }
    }
}

// --- Conversion Helper Functions ---

private fun ExpenseEntity.toBackupExpense(): BackupExpense {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return BackupExpense(
        id = this.id.toLong(),
        amount = this.amount,
        category = this.title,
        description = this.paymentMode,
        date = dateFormat.format(Date(this.timestamp))
    )
}

private fun BackupExpense.toExpenseEntity(): ExpenseEntity {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val timestamp = dateFormat.parse(this.date)?.time ?: System.currentTimeMillis()
    return ExpenseEntity(
        id = 0, // Set to 0 to allow Room to auto-generate a new ID
        title = this.category,
        amount = this.amount,
        currencySymbol = "", // Currency symbol is not part of the backup, handle as needed
        timestamp = timestamp,
        paymentMode = this.description ?: ""
    )
}
