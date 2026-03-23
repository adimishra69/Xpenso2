package com.example.xpenso.Budget

import AmountTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun BudgetDialog(
    initialValue: String,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var textValue by remember {
        mutableStateOf(
            initialValue
                .toDoubleOrNull()
                ?.let { String.format(Locale.getDefault(), "%.2f", it) }
                ?: ""
        )
    }

    var showError by remember { mutableStateOf(false) }

    val cleanedValue = textValue.trimEnd('.')
    val budgetValue = cleanedValue.toDoubleOrNull() ?: 0.0
    val isValid = budgetValue > 0


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Budget") },

        text = {
            Column {
                val message = if (isEditMode) {
                    "Edit your spending limit for this specific month"
                } else {
                    "Enter your spending limit for this specific month"
                }

                Text(message)
                Spacer(Modifier.height(16.dp))

                // ⭐ Reusable Amount field
                AmountTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        showError = false
                    },
                    isError = showError,
                    label = "Budget Amount"
                )

                // ⭐ Error message
                if (showError) {
                    Text(
                        text = "Budget must be greater than 0",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    if (!isValid) {
                        showError = true
                        return@TextButton
                    }
                    onSave(budgetValue)
                }
            ) {
                Text(if (isEditMode) "Save" else "Set Budget")
            }
        },

        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isEditMode) {
                    TextButton(onClick = onRemove) {
                        Text(
                            "Remove limit",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}
