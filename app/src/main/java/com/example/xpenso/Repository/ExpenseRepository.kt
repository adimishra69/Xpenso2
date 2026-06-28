package com.example.xpenso.Repository


import android.content.Context
import android.net.Uri
import com.example.xpenso.Database.BackupExpense
import com.example.xpenso.Database.ExpenseDao
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.xpenso.BudgetDatabase.BudgetDao
import com.example.xpenso.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
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

    suspend fun deleteAllBudgets() {
        budgetDao.deleteAllBudgets()
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
            val backupExpenses = expenses
                .sortedByDescending { it.timestamp }
                .map { it.toBackupExpense() }

            // 🔥 GET ALL BUDGETS

            val allBudgets = budgetDao.getAllBudgets().map { budget ->
                mapOf(
                    "month" to java.text.DateFormatSymbols().months[budget.month],
                    "year" to budget.year,
                    "amount" to budget.amount
                )
            }
            // Use Gson with Pretty Printing
            val gson = GsonBuilder().setPrettyPrinting().create()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())


            val backupWrapper = linkedMapOf(
                "app" to "Xpenso",
                "version" to "1.0.0",
                "backup_type" to "full_backup",

                // ✅ machine (keep this)
                "exported_at" to System.currentTimeMillis(),

                // ✅ human readable (add this)
                "exported_at_readable" to dateFormat.format(Date()),

                "budgets" to allBudgets,

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

            val budgetsJson = jsonObject["budgets"]
            if (budgetsJson != null) {

                val budgetsList = gson.fromJson<List<Map<String, Any>>>(
                    gson.toJson(budgetsJson),
                    object : TypeToken<List<Map<String, Any>>>() {}.type
                )

                val settingsStore = SettingsStore(context)

                if (replaceExisting) {
                    budgetDao.getAllBudgets().forEach {
                        budgetDao.deleteBudget(it.month, it.year)
                    }
                }

                for (budget in budgetsList) {
                    val monthName = budget["month"] as String

                    val month = java.text.DateFormatSymbols().months
                        .indexOfFirst { it.equals(monthName, ignoreCase = true) }

                    if (month == -1) continue
                    val year = (budget["year"] as Double).toInt()
                    val amount = budget["amount"] as Double

                    budgetDao.insert(
                        com.example.xpenso.BudgetDatabase.BudgetEntity(
                            month = month,
                            year = year,
                            amount = amount
                        )
                    )
                }
            }

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
    val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
    return BackupExpense(
        id = this.id.toLong(),
        title = this.title,
        amount = this.amount,
        category = this.category,
        notes = this.notes,
        paymentMode = this.paymentMode,
        date = dateFormat.format(Date(this.timestamp))
    )
}

private fun BackupExpense.toExpenseEntity(): ExpenseEntity {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
    val timestamp =
        dateFormat.parse(this.date)?.time ?: System.currentTimeMillis()

    val resolvedCategory =
        ExpenseCategory.entries
            .find { it.name == this.category }
            ?.name
            ?: ExpenseCategory.OTHER.name

    return ExpenseEntity(
        id = 0,
        title = this.title ?: "",
        amount = this.amount,
        category = resolvedCategory,
        notes = this.notes ?: "",
        currencySymbol = "",
        timestamp = timestamp,
        paymentMode = this.paymentMode ?: ""
    )
}