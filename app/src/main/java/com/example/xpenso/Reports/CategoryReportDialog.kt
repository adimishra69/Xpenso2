package com.example.xpenso.Reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.xpenso.Transaction.CategoryIcon
import com.example.xpenso.Transaction.ExpenseCategory

@Composable
fun CategoryReportDialog(
    categories: List<ExpenseCategory>,
    onDismiss: () -> Unit,
    onCategorySelected: (ExpenseCategory) -> Unit
) {

    val sortedCategories =
        ExpenseCategory.entries
            .filter { it != ExpenseCategory.OTHER }
            .sortedBy { it.name }
            .plus(ExpenseCategory.OTHER)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Create Category Report",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {

            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {

                LazyColumn {

                    items(sortedCategories.size) { index ->

                        val category = sortedCategories[index]

                        ListItem(

                            headlineContent = {
                                Text(
                                    category.name
                                        .replace("_", " ")
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },

                            leadingContent = {
                                CategoryIcon(
                                    category = category,
                                    tint = category.color
                                )
                            },

                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),

                            modifier = Modifier.clickable {
                                onCategorySelected(category)
                            }
                        )

                        if (index != sortedCategories.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                thickness = 0.6.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}