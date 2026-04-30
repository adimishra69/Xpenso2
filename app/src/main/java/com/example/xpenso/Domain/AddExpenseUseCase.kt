package com.example.xpenso.Domain

import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Repository.ExpenseRepository

class AddExpenseUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseEntity) {
        repository.insertExpense(expense)
    }
}