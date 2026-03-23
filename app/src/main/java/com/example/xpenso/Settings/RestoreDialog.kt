package com.example.xpenso.Settings


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel

@Composable
fun RestoreDialog(
    expenseViewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var pendingReplace by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            if (context.contentResolver.getType(uri) == "application/pdf") {
                Toast.makeText(context, "Cannot restore from PDF.", Toast.LENGTH_LONG).show()
            } else {
                expenseViewModel.importData(uri, pendingReplace)
            }
        }

        // ✅ Dismiss AFTER result comes back
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Restore") },
        text = {
            Text(
                "Choose how you want to restore your backup\n\n" +
                        "Replace: Removes all current expenses and restores backup data\n\n" +
                        "Merge: Keeps existing data and adds backup entries\n\n" +
                        "Only JSON backup files are supported"
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = {
                    pendingReplace = false
                    restoreLauncher.launch(arrayOf("application/json"))
                }) {
                    Text(
                        text = "Merge",
                        maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                    )

                }

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = {
                    pendingReplace = true
                    restoreLauncher.launch(arrayOf("application/json"))
                }) {
                    Text(
                        text = "Replace",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}