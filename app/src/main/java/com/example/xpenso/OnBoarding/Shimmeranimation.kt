package com.example.xpenso.OnBoarding



import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

// 🔥 Shimmer Brush (Animated Gradient)
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    return Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        start = androidx.compose.ui.geometry.Offset(translateAnim, translateAnim),
        end = androidx.compose.ui.geometry.Offset(
            translateAnim + 200f,
            translateAnim + 200f
        )
    )
}

// 🔥 Single Row Shimmer (Matches CurrencyRow)
@Composable
fun ShimmerCurrencyRow(brush: Brush) {

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔹 Symbol placeholder
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(28.dp)
                    .background(brush, RoundedCornerShape(6.dp))
            )

            Spacer(Modifier.width(12.dp))

            // 🔹 Name + Code
            Column(modifier = Modifier.weight(1f)) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(brush, RoundedCornerShape(6.dp))
                )

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(12.dp)
                        .background(brush, RoundedCornerShape(6.dp))
                )
            }

            Spacer(Modifier.width(12.dp))

            // 🔹 Check icon placeholder
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(brush, RoundedCornerShape(50))
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

// 🔥 Full List Shimmer (Use this in screen)
@Composable
fun ShimmerCurrencyList() {

    val brush = shimmerBrush()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(10) {
            ShimmerCurrencyRow(brush)
        }
    }
}