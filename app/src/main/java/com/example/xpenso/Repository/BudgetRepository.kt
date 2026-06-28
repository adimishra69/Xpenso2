package com.example.xpenso.Repository

import com.example.xpenso.BudgetDatabase.BudgetDao
import com.example.xpenso.BudgetDatabase.BudgetEntity

class BudgetRepository(
    private val budgetDao: BudgetDao
) {

    suspend fun saveBudget(month: Int, year: Int, amount: Double) {
        budgetDao.insert(
            BudgetEntity(
                month = month,
                year = year,
                amount = amount
            )
        )
    }

    suspend fun getBudget(month: Int, year: Int): Double {
        return budgetDao.getBudget(month, year)?.amount ?: 0.0
    }

    suspend fun deleteBudget(month: Int, year: Int) {
        budgetDao.deleteBudget(month, year)
    }

    suspend fun getAllBudgets(): List<BudgetEntity> {
        return budgetDao.getAllBudgets()
    }

    suspend fun getBudgetAmount(month: Int, year: Int): Double {
        return budgetDao.getBudget(month, year)?.amount ?: 0.0
    }


}