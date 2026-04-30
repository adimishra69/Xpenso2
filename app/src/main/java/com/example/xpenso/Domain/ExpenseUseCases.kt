package com.example.xpenso.Domain

data class ExpenseUseCases(
    val addExpense: AddExpenseUseCase,
    val updateExpense: UpdateExpenseUseCase,
    val deleteExpense: DeleteExpenseUseCase,
    val getExpenses: GetExpensesUseCase
)