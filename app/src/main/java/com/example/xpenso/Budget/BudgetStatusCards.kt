package com.example.xpenso.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.xpenso.ui.theme.LocalBudgetColors

@Composable
fun BudgetStatusCard(
    budget: Double,
    usagePercentage: Float,
    displayPercentage: Int
) {
     val customColors = LocalBudgetColors.current
    when {

        // --------------------------------------------------
// 💤 NO SPENDING YET (0%)
// --------------------------------------------------
        budget > 0 && usagePercentage == 0f -> {

            StatusUI(
                title = "No spending yet ",
                description = "You haven’t used your budget this month\nA great start!",
                icon = Icons.Default.HourglassEmpty,
                bg = customColors.blueBg,
                titleColor = customColors.blueTitle,
                descColor = customColors.blueDesc
            )
        }
        // --------------------------------------------------
        // 🔵 JUST STARTED (1–5%)
        // --------------------------------------------------
        budget > 0 && usagePercentage > 0f && usagePercentage <= 0.05f -> {
            StatusUI(
                title = "Just getting started",
                description = "You’ve just started spending this month\nEverything looks fine so far",
                icon = Icons.Default.RocketLaunch,
                bg = customColors.blueBg,
                titleColor = customColors.blueTitle,
                descColor = customColors.blueDesc
            )
        }

        // 🟢 AMAZING JOB (0–60%)
        // --------------------------------------------------
        budget > 0 && usagePercentage <= 0.6f -> {

            StatusUI(title = "Great progress ",
                description = "Only $displayPercentage% used\nYou're managing your budget well",
                icon = Icons.Default.EmojiEvents,
                bg = customColors.greenBg,
                titleColor = customColors.greenTitle,
                descColor = customColors.greenDesc
            )
        }

        // --------------------------------------------------
        // 🟠 GOOD JOB (61–80%)
        // --------------------------------------------------
        // 🟠 GOOD JOB / ON TRACK (61–80%)
        budget > 0 && usagePercentage > 0.6f && usagePercentage <= 0.8f -> {
            StatusUI(
                title = "On track ",
                description = "You’ve used $displayPercentage% of your budget\nSpending is fine — just keep an eye on it",
                icon = Icons.Default.TrendingUp,
                bg = customColors.yellowBg,
                titleColor = customColors.yellowTitle,
                descColor = customColors.yellowDesc
            )
        }

        // --------------------------------------------------
        // ⚠️ WATCH SPENDING (81–100%)
        // --------------------------------------------------
        budget > 0 && usagePercentage <= 1f -> {
            StatusUI(
                title = "Watch your spending ",
                description = "You’re close to the monthly limit",
                icon = Icons.Default.Warning,
                bg = customColors.orangeBg,
                titleColor = customColors.orangeTitle,
                descColor = customColors.orangeDesc
            )
        }

        // --------------------------------------------------
        // ❌ EXCEEDED
        // --------------------------------------------------
        budget > 0 && usagePercentage > 1f -> {

            StatusUI(
                title = "Budget exceeded 🚨",
                description = "You’ve spent more than your monthly limit",
                icon = Icons.Default.Error,
                bg = MaterialTheme.colorScheme.errorContainer,
                titleColor = MaterialTheme.colorScheme.error,
                descColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/* ------------------------------------------------------
   🔹 PRIVATE UI COMPONENT
   ------------------------------------------------------ */
@Composable
private fun StatusUI(
    title: String,
    description: String,
    icon: ImageVector,
    bg: Color,
    titleColor: Color,
    descColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(28.dp), // More modern, rounder corners
        colors = CardDefaults.cardColors(containerColor = bg),
        // Add a very subtle border using the title color
        // Increase thickness to 1.5.dp and alpha to 0.6f for a dark, sharp outline
        border = BorderStroke(1.5.dp, titleColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ⭐ PREMIUM TOUCH: Put the icon inside its own soft-colored container
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(18.dp),
                color = titleColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = titleColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.width(18.dp))

            Column {
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.Bold, // Stronger weight for premium feel
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    color = descColor.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = 22.sp // Better spacing between lines
                )
            }
        }
    }
}
@Composable
fun CommonNoBudgetView(
    icon: ImageVector,
    title: String,
   description: String,
//    buttonText: String = "Set Budget",
//    onSetBudgetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary.copy(0.9f)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

//        Spacer(Modifier.height(30.dp))
//
//        Button(
//            onClick = onSetBudgetClick,
//            shape = RoundedCornerShape(14.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.primary.copy(0.9f),
//                contentColor = MaterialTheme.colorScheme.onPrimary
//            ),
//            modifier = Modifier
//                .padding(horizontal = 16.dp) // Adds a bit of breathing room
//                .wrapContentWidth()           // Only as wide as the text itself
//        ) {
//            Text(
//                text = buttonText,
//                modifier = Modifier.padding(horizontal = 12.dp) // Space inside the button
//            )
//        }
    }
}










