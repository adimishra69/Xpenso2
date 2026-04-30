package com.example.xpenso.Domain

import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetExpensesUseCase(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<ExpenseEntity>> {
        return repository.allExpenses
    }
}