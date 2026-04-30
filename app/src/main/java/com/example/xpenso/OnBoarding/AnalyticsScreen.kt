package com.example.xpenso.OnBoarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.xpenso.AppStart.AnalyticsHelper
import com.example.xpenso.OnBoarding.XpensoToggle

// ═══════════════════════════════════════════════════════════════════════════════
// AnalyticsHeader
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnalyticsHeader() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Analytics,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = "Help improve Xpenso",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Share anonymous usage data to fix bugs faster\nand make the app better for everyone.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// AnalyticsCard
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnalyticsCard(
    crashReportsEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val cardBorderColor by animateColorAsState(
        targetValue = if (crashReportsEnabled)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        animationSpec = tween(durationMillis = 300),
        label = "cardBorder"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (crashReportsEnabled)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (crashReportsEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Usage & Crash Reports",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Helps improve app performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            XpensoToggle(
                checked = crashReportsEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TrustRow
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TrustRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF1D9E75),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = "Your data is anonymous and secure",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// AnalyticsFooter
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnalyticsFooter(
    annotatedText: AnnotatedString,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onOpenTerms: () -> Unit
) {
    Column {


        // Legal text
        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            onClick = { offset ->
                annotatedText.getStringAnnotations(offset, offset)
                    .firstOrNull()?.let { annotation ->
                        when (annotation.tag) {
                            "TERMS"   -> onOpenTerms()
                            "PRIVACY" -> onOpenTerms()
                        }
                    }
            }
        )

        // Back + CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 82.dp)
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = "Back",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// AnalyticsScreen
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnalyticsScreen(
    crashReportsEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onOpenTerms: () -> Unit
) {
    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("Analytics")
    }

    BackHandler { onBack() }

    val annotatedText = buildAnnotatedString {
        append("By continuing, you agree to Xpenso's ")

        pushStringAnnotation(tag = "TERMS", annotation = "terms")
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) { append("Terms") }
        pop()

        append(" & ")

        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) { append("Privacy Policy") }
        pop()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        OnboardingStepHeader(step = 1)

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnalyticsHeader()

                Spacer(Modifier.height(36.dp))

                AnalyticsCard(
                    crashReportsEnabled = crashReportsEnabled,
                    onToggle = onToggle
                )

                Spacer(Modifier.height(14.dp))

                TrustRow()
            }
        }

        AnalyticsFooter(
            annotatedText = annotatedText,
            onBack = onBack,
            onStart = onStart,
            onOpenTerms = onOpenTerms
        )
    }
}
