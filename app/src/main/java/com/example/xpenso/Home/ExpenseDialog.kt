package com.example.xpenso.Home

import AmountTextField
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun ExpenseDialog(
    title: String,
    confirmText: String,
    name: String,
    amount: String,
    paymentMode: String,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    isEdit: Boolean = false
) {

    var showMainDialog by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAmountError by remember { mutableStateOf(false) }
    // 1. Add this state at the top of ExpenseDialog
    var isSaving by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    val isSaveEnabled =
        name.isNotBlank() &&
                amount.isNotBlank() &&
                (isEdit || paymentMode.isNotBlank())

    // ===============================
    // DELETE CONFIRMATION
    // ===============================
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                showMainDialog = true
            },
            title = { Text("Delete Expense?") },
            text = { Text("This action can’t be undone") },

            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteConfirm = false
                        onDelete?.invoke()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss() // close all dialogs
                    }
                ) {
                    Text("Cancel" , color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ===============================
    // MAIN DIALOG
    // ===============================
    if (showMainDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },

            text = {
                Column {

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            val trimmed = it.trimStart()
                            if (trimmed.length <= 69) onNameChange(trimmed)
                        },
                        label = { Text("Item") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    // ✅ USE YOUR EXISTING FIELD
                    AmountTextField(
                        value = amount,
                        onValueChange = {
                            showAmountError = false
                            onAmountChange(it)
                        },
                        isError = showAmountError
                    )

                    if (showAmountError) {
                        Text(
                            text = "Enter amount greater than 0",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    if (!isEdit) {

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Payment Mode",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Cash", "Card", "UPI").forEach { mode ->
                                FilterChip(
                                    selected = paymentMode == mode,
                                    onClick = { onPaymentModeChange(mode) },
                                    label = { Text(mode) }
                                )
                            }
                        }
                    }
                }
            },

            confirmButton = {
                Row {

                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (onDelete != null) {
                        TextButton(
                            onClick = {
                                showMainDialog = false
                                showDeleteConfirm = true
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    TextButton(
                        // Disable button if inputs are invalid OR if we are already saving
                        enabled = isSaveEnabled && !isSaving,
                        onClick = {
                            val value = amount.toDoubleOrNull() ?: 0.0
                            if (value <= 0.0) {
                                showAmountError = true
                            } else {
                                // --- THE FIX ---
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSaving = true // Disable button immediately
                                showAmountError = false
                                onConfirm() // Call the save logic
                            }
                        }
                    ) {
                        Text(if (isSaving) "Saving..." else confirmText)
                    }
                }
            },

            dismissButton = {}
        )
    }
}
