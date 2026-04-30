package com.example.xpenso.Domain

import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Repository.ExpenseRepository

class DeleteExpenseUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseEntity) {
        repository.deleteExpense(expense)
    }
}


//suspend fun deleteExpense(
//    repository: ExpenseRepository,
//    expense: ExpenseEntity
//) {
//    repository.deleteExpense(expense)
//}