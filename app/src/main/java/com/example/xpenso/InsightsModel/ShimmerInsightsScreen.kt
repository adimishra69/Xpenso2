package com.example.xpenso.InsightsModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerInsightsFullScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 45.dp)
    ) {

        // 🔥 TOP BAR SHIMMER (AppTopBar style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .height(35.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
        )

        Spacer(Modifier.height(12.dp))

        // 🔥 DIVIDER

        // 🔥 TAB SHIMMER
        Row(
            modifier = Modifier.padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(117.dp)
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 🔥 BODY SHIMMER
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            // 💡 Smart Insight Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(24.dp)
                    )
            )

            Spacer(Modifier.height(32.dp))

            // Title
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(18.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
            )

            Spacer(Modifier.height(24.dp))

            // 📊 Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
            )

            Spacer(Modifier.height(40.dp))

            // Category Title
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(18.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
            )

            Spacer(Modifier.height(24.dp))

            // 🍩 Donut Chart
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(100.dp)
                    )
            )

            Spacer(Modifier.height(32.dp))

            // Legend
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .padding(vertical = 6.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                )
            }

            Spacer(Modifier.height(40.dp))

            // Summary Title
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(18.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
            )

            Spacer(Modifier.height(16.dp))

            // Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}