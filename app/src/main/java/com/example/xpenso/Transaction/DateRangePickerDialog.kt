package com.example.xpenso.Transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomRangeDialog(
    startDate: Long?,
    endDate: Long?,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {

    val formatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,


        confirmButton = {
            Button(
                onClick = onApply,
                enabled = startDate != null && endDate != null
            ) {
                Text("Apply")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text="Cancel" , color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },

        title = {
            Text("Custom Range")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // START DATE
                OutlinedButton(
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val text = startDate?.let {
                        formatter.format(Date(it))
                    } ?: "Select Start Date"

                    Text(text)
                }

                // END DATE
                OutlinedButton(
                    onClick = onEndClick,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val text = endDate?.let {
                        formatter.format(Date(it))
                    } ?: "Select End Date"

                    Text(text)
                }
            }
        }
    )
}