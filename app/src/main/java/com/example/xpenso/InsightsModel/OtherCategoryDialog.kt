package com.example.xpenso.InsightMonthComponents

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OtherCategoryItem(
    val name: String,
    val amount: Double,
    val color: Color
)

@Composable
fun OtherCategoryDialog(
    categories: List<OtherCategoryItem>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onCategoryClick: (String) -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,

        title = {
            Text(
                "Other Categories",
                style = MaterialTheme.typography.titleMedium
            )
        },

        text = {

            Column {

                categories.forEachIndexed { index, item ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onCategoryClick(item.name)
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                item.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {

                            Text(
                                buildAnnotatedString {

                                    withStyle(
                                        SpanStyle(
                                            fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value - 2).sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        append(currencySymbol)
                                        append(" ")
                                    }

                                    withStyle(
                                        SpanStyle(
                                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    ) {
                                        append("%.0f".format(item.amount))
                                    }

                                }
                            )
                        }
                    }

                    if (index != categories.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                .6f),
                            thickness = 0.7.dp
                        )
                    }
                }
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel" , color  = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },

        confirmButton = {}
    )
}