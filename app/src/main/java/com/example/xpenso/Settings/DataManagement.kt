package com.example.xpenso.Settings
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*

import androidx.compose.ui.platform.LocalContext
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel

@Composable
fun DataManagementDialogs(
    expenseViewModel: ExpenseViewModel,
    isPremium: Boolean,
    onNavigateToPremium: () -> Unit,
    showBackup: Boolean,
    onDismissBackup: () -> Unit,
    showRestore: Boolean,
    onDismissRestore: () -> Unit
) {
    val context = LocalContext.current
    // 🔥 FIX: Proper state for replace/merge
    var replaceExisting by remember { mutableStateOf(false) }


    // ---------------- RESTORE DIALOG ----------------

    if (showRestore) {
        RestoreDialog(
            expenseViewModel = expenseViewModel,
            onDismiss = onDismissRestore
        )
    }
}