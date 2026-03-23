package com.example.xpenso.ExpenseViewmodel


import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xpenso.Reports.PdfReportGenerator
import com.example.xpenso.Database.ExpenseDatabase
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Reports.CategoryPdfReportGenerator
import com.example.xpenso.Reports.CsvReportGenerator
import com.example.xpenso.Reports.MonthlyPdfReportGenerator
import com.example.xpenso.Reports.CustomRangePdfGenerator
import com.example.xpenso.Repository.ExpenseRepository
import com.example.xpenso.SettingsStore
import com.example.xpenso.Transaction.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// ---------------- UI STATE ----------------
sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object InProgress : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val message: String) : BackupRestoreState()
}

enum class ExportType {
    JSON, PDF, CSV, MONTHLY, CATEGORY, CUSTOM
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository = ExpenseRepository(
        ExpenseDatabase.getDatabase(application).expenseDao()
    )

    private val settingsStore = SettingsStore(application)

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis = _selectedDateMillis.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTotal: StateFlow<Double> = filteredExpenses
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _startDay = MutableStateFlow(1)

    val currentMonthSpent: StateFlow<Double> = combine(
        _selectedMonth,
        _selectedYear,
        _startDay,
        allExpenses
    ) { month, year, startDay, expenses ->
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun updateStartDay(day: Int) {
        _startDay.value = day
    }

    init {
        _isInitialLoadComplete.value = true
    }

    fun updateSelectedDate(dateMillis: Long) {
        _selectedDateMillis.value = dateMillis
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun updateSelectedMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun addExpense(title: String, amount: Double, currencySymbol: String, timestamp: Long, paymentMode: String = "") {
        val formattedTitle = title.trim().split(" ").filter { it.isNotBlank() }
            .joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }

        val expense = ExpenseEntity(
            title = formattedTitle,
            amount = amount,
            currencySymbol = currencySymbol,
            timestamp = timestamp,
            paymentMode = paymentMode
        )

        viewModelScope.launch { repository.insertExpense(expense) }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun deleteAllData() {
        viewModelScope.launch { repository.deleteAllExpenses() }
    }

    fun exportReport(
        type: ExportType,
        uri: Uri,
        startOfMonth: Int,
        currencySymbol: String,
        category: ExpenseCategory? = null,
        customRange: Pair<Long, Long>? = null
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
                        expenses = expenses
                    )
                }

                ExportType.MONTHLY -> {
                    val now = Calendar.getInstance()
                    var month = now.get(Calendar.MONTH)
                    var year = now.get(Calendar.YEAR)

                    if (now.get(Calendar.DAY_OF_MONTH) < startOfMonth) {
                        val prev = (now.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                        month = prev.get(Calendar.MONTH)
                        year = prev.get(Calendar.YEAR)
                    }

                    val expenses = repository.getAllExpensesNow()
                    val isPremium = settingsStore.isPremium.first()

                    MonthlyPdfReportGenerator.generateMonthlyCycleReport(
                        context = getApplication(),
                        uri = uri,
                        allExpenses = expenses,
                        month = month,
                        year = year,
                        startOfMonth = startOfMonth,
                        currencySymbol = currencySymbol,
                        isPremium = isPremium
                    )
                }

                ExportType.CATEGORY -> {
                    if (category == null) return@launch

                    val expenses = repository.getAllExpensesNow()
                    val filtered = expenses.filter {
                        ExpenseCategory.detect(it.title) == category
                    }

                    val currency = settingsStore.currencySymbol.first()

                    CategoryPdfReportGenerator.generateCategoryReport(
                        context = getApplication(),
                        uri = uri,
                        expenses = filtered,
                        category = category,
                        currencySymbol = currency
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

    suspend fun hasAnyData(): Boolean {
        return repository.getExpenseCount() > 0
    }

    fun resetBackupRestoreState() {
        _backupRestoreState.value = BackupRestoreState.Idle
    }
}
