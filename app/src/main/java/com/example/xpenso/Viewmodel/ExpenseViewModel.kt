package com.example.xpenso.Viewmodel


import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xpenso.Reports.PdfReportGenerator
import com.example.xpenso.Database.ExpenseDatabase
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Domain.ExpenseUseCases
import com.example.xpenso.Reports.CategoryPdfReportGenerator
import com.example.xpenso.Reports.CsvReportGenerator
import com.example.xpenso.Reports.MonthlyPdfReportGenerator
import com.example.xpenso.Reports.CustomRangePdfGenerator
import com.example.xpenso.Reports.YearlyPdfReportGenerator
import com.example.xpenso.Repository.ExpenseRepository
import com.example.xpenso.Domain.*
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

// ---------------- UI STATE ----------------
sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object InProgress : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val message: String) : BackupRestoreState()
}

enum class ExportType {
    JSON, PDF, CSV, MONTHLY, CATEGORY, CUSTOM , YEARLY
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ExpenseDatabase.getDatabase(application)

    private val repository: ExpenseRepository = ExpenseRepository(
        database.expenseDao(),
        database.budgetDao()
    )

    private val settingsStore = SettingsStore(application)

//    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000),
//        emptyList()
//    )
private val useCases = ExpenseUseCases(
    addExpense = AddExpenseUseCase(repository),
    updateExpense = UpdateExpenseUseCase(repository),
    deleteExpense = DeleteExpenseUseCase(repository),
    getExpenses = GetExpensesUseCase(repository)
)

    val allExpenses = useCases.getExpenses().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis = _selectedDateMillis.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val selectedMonth = _selectedMonth.asStateFlow()
    val selectedYear = _selectedYear.asStateFlow()


    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete = _isInitialLoadComplete.asStateFlow()

    private val _backupRestoreState = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val backupRestoreState = _backupRestoreState.asStateFlow()

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        _selectedDateMillis,
        allExpenses
    ) { dateMillis, expenses ->
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)

        expenses.filter {
            val expCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            expCal.get(Calendar.DAY_OF_MONTH) == d &&
                    expCal.get(Calendar.MONTH) == m &&
                    expCal.get(Calendar.YEAR) == y
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
    emptyList()
    )

    val dailyTotal: StateFlow<Double> = filteredExpenses
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0.0
        )

    private val _startDay = MutableStateFlow(1)

    val currentMonthSpent: StateFlow<Double> = combine(
        _selectedMonth,
        _selectedYear,
        _startDay,
        allExpenses
    ) {
        month, year, startDay, expenses ->

        val startCal = Calendar.getInstance().apply {
            set(year, month, startDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        expenses.filter { it.timestamp in startCal.timeInMillis..endCal.timeInMillis }
            .sumOf { it.amount }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )



    fun updateStartDay(day: Int) {
        _startDay.value = day
    }

    init {
        _isInitialLoadComplete.value = true
    }

    fun updateSelectedDate(dateMillis: Long) {
        _selectedDateMillis.value = dateMillis
    }

//    fun updateExpense(expense: ExpenseEntity) {
//
//        val updatedExpense = expense.copy(
//            title = formatTitle(expense.title)   // ✅ FIX
//        )
//
//        viewModelScope.launch {
//            repository.updateExpense(updatedExpense)
//        }
//    }
fun updateExpense(
    old: ExpenseEntity,
    title: String,
    amount: String,
    paymentMode: String,
    category: String,
    notes: String
) {
    val updatedExpense = old.copy(
        title = formatTitle(title),
        amount = abs(amount.toDoubleOrNull() ?: 0.0),
        paymentMode = paymentMode,
        category = category,
        notes = formatTitle(notes)
    )

    viewModelScope.launch {
//        repository.updateExpense(updatedExpense)
        useCases.updateExpense(updatedExpense)
    }
}

    fun updateSelectedMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun addExpense(
        title: String,
        amount: Double,
        currencySymbol: String,
        timestamp: Long,
        paymentMode: String = "",
        category: String = ExpenseCategory.OTHER.name,
        notes: String = ""
    ) {

        val expense = ExpenseEntity(
            title = formatTitle(title),
            amount = amount,
            category = category,
            notes = formatTitle(notes),
            currencySymbol = currencySymbol,
            timestamp = timestamp,
            paymentMode = paymentMode
        )

        viewModelScope.launch {
//            repository.insertExpense(expense)
            useCases.addExpense(expense)
        }
    }
    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
//            repository.deleteExpense(expense)
            useCases.deleteExpense(expense)

        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllExpenses()
            repository.deleteAllBudgets()
        }
    }

    suspend fun hasAnyData(): Boolean {
        return repository.getExpenseCount() > 0
    }

    fun resetBackupRestoreState() {
        _backupRestoreState.value = BackupRestoreState.Idle
    }

    fun exportReport(
        type: ExportType,
        uri: Uri,
        startOfMonth: Int,
        currencySymbol: String,
        category: ExpenseCategory? = null,
        customRange: Pair<Long, Long>? = null,
        year: Int? = null,
        month : Int? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            _backupRestoreState.value = BackupRestoreState.InProgress

            val result = when (type) {

                ExportType.JSON -> repository.exportData(getApplication(), uri)

                ExportType.PDF -> {
                    val expenses = repository.getAllExpensesNow()
                    val currency = settingsStore.currencySymbol.first()
                    val isPremium = settingsStore.isPremium.first()

                    PdfReportGenerator.generateExpenseReport(
                        context = getApplication(),
                        uri = uri,
                        expenses = expenses,
                        currencySymbol = currency,
                        isPremium = isPremium
                    )
                }

                ExportType.CSV -> {
                    val expenses = repository.getAllExpensesNow()
                    CsvReportGenerator.generateCsvReport(
                        context = getApplication(),
                        uri = uri,
                        expenses = expenses,
                        currencySymbol = currencySymbol
                    )
                }

                ExportType.MONTHLY -> {

                    val selectedMonth = month ?: Calendar.getInstance().get(Calendar.MONTH)
                    val selectedYear = year ?: Calendar.getInstance().get(Calendar.YEAR)

                    val expenses = repository.getAllExpensesNow()
                    val isPremium = settingsStore.isPremium.first()

                    MonthlyPdfReportGenerator.generateMonthlyCycleReport(
                        context = getApplication(),
                        uri = uri,
                        allExpenses = expenses,
                        month = selectedMonth,
                        year = selectedYear,
                        startOfMonth = startOfMonth,
                        currencySymbol = currencySymbol,
                        isPremium = isPremium
                    )
                }

                ExportType.CATEGORY -> {
                    if (category == null) return@launch

                    val expenses = repository.getAllExpensesNow()
                    val filtered = expenses.filter {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = it.timestamp
                        }

                        (ExpenseCategory.entries
                            .find { cat -> cat.name == it.category }
                            ?: ExpenseCategory.OTHER
                                ) == category &&
                                (month == null || cal.get(Calendar.MONTH) == month) &&
                                (year == null || cal.get(Calendar.YEAR) == year)
                    }

                    val currency = settingsStore.currencySymbol.first()

                    val totalOverall = expenses.sumOf { it.amount }

                    CategoryPdfReportGenerator.generateCategoryReport(
                        context = getApplication(),
                        uri = uri,
                        expenses = filtered,
                        category = category,
                        currencySymbol = currency,
                        totalOverall = totalOverall,
                        month = month,
                        year = year,
                        startOfMonth = startOfMonth
                    )
                }

                ExportType.CUSTOM -> {
                    if (customRange == null) return@launch

                    val expenses = repository.getAllExpensesNow()

                    CustomRangePdfGenerator.generateCustomRangeReport(
                        context = getApplication(),
                        uri = uri,
                        allExpenses = expenses,
                        startDate = customRange.first,
                        endDate = customRange.second,
                        currencySymbol = currencySymbol
                    )
                }

                ExportType.YEARLY -> {

                    val expenses = repository.getAllExpensesNow()
                    val currency = settingsStore.currencySymbol.first()

                    YearlyPdfReportGenerator.generateYearlyReport(
                        context = getApplication(),
                        uri = uri,
                        expenses = expenses,
                        currencySymbol = currency,
                        startOfMonth = startOfMonth,
                        year = year ?: Calendar.getInstance().get(Calendar.YEAR),

                    )
                }
            }

            _backupRestoreState.value = result.fold(
                onSuccess = { BackupRestoreState.Success("Export successful") },
                onFailure = { BackupRestoreState.Error("Export failed: ${it.message}") }
            )
        }
    }
    // Add this to ExpenseViewModel.kt
    fun importData(uri: Uri, replaceExisting: Boolean) {
        viewModelScope.launch {
            _backupRestoreState.value = BackupRestoreState.InProgress
            val result = repository.importData(getApplication(), uri, replaceExisting)
            _backupRestoreState.value = result.fold(
                onSuccess = { BackupRestoreState.Success("Restore successful") },
                onFailure = { BackupRestoreState.Error("Restore failed: ${it.message}") }
            )
        }
    }

    suspend fun hasDataForExport(
        type: ExportType,
        category: ExpenseCategory? = null,   // ✅ ADD THIS
        month: Int? = null,
        year: Int? = null
    ): Boolean{

        val expenses = repository.getAllExpensesNow()

        return when (type) {

            ExportType.MONTHLY -> {
                expenses.any {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = it.timestamp
                    }
                    cal.get(Calendar.MONTH) == month &&
                            cal.get(Calendar.YEAR) == year
                }
            }

            ExportType.YEARLY -> {
                expenses.any {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = it.timestamp
                    }
                    cal.get(Calendar.YEAR) == year
                }
            }

            ExportType.CATEGORY -> {
                expenses.any {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = it.timestamp
                    }

                    (ExpenseCategory.entries
                        .find { cat -> cat.name == it.category }
                        ?: ExpenseCategory.OTHER
                            ) == category &&
                            (month == null || cal.get(Calendar.MONTH) == month) &&
                            (year == null || cal.get(Calendar.YEAR) == year)
                }
            }
            else -> expenses.isNotEmpty()
        }
    }
}

private fun formatTitle(title: String): String {
    return title.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") {
            it.lowercase()
                .replaceFirstChar { char -> char.uppercase() }
        }
}

