package com.example.xpenso.Viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xpenso.BudgetDatabase.BudgetEntity
import com.example.xpenso.Database.ExpenseDatabase
import com.example.xpenso.Repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(
        ExpenseDatabase.getDatabase(application).budgetDao()
    )

    private val _currentBudget = MutableStateFlow(0.0)
    val currentBudget = _currentBudget.asStateFlow()

    private val _allBudgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val allBudgets = _allBudgets.asStateFlow()

    init {
        refreshAllBudgets()
    }

    fun loadBudget(month: Int, year: Int) {
        viewModelScope.launch {
            _currentBudget.value =
                repository.getBudget(month, year)
        }
    }

    fun saveBudget(month: Int, year: Int, amount: Double) {
        viewModelScope.launch {
            repository.saveBudget(month, year, amount)
            _currentBudget.value = amount
            refreshAllBudgets()
        }
    }

    fun deleteBudget(month: Int, year: Int) {
        viewModelScope.launch {
            repository.deleteBudget(month, year)
            _currentBudget.value = 0.0
            refreshAllBudgets()
        }
    }

    suspend fun getBudget(month: Int, year: Int): Double {
        return repository.getBudget(month, year)
    }

    private fun refreshAllBudgets() {
        viewModelScope.launch {
            _allBudgets.value = repository.getAllBudgets()
        }
    }
}



