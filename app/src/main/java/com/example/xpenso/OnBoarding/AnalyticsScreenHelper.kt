//package com.example.xpenso.OnBoarding
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.ClickableText
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.outlined.Info
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.surfaceColorAtElevation
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import com.example.xpenso.OnBoarding.XpensoToggle
//
//@Composable
//fun AnalyticsHeader() {
//    Icon(
//        imageVector = Icons.Outlined.Info,
//        contentDescription = null,
//        modifier = Modifier.size(64.dp),
//        tint = MaterialTheme.colorScheme.primary
//    )
//
//    Spacer(Modifier.height(16.dp))
//
//    Text(
//        "Help improve Xpenso",
//        style = MaterialTheme.typography.headlineMedium,
//        fontWeight = FontWeight.Bold,
//        textAlign = TextAlign.Center
//    )
//
//    Spacer(Modifier.height(8.dp))
//
//    Text(
//        "Share anonymous usage data to fix bugs faster",
//        style = MaterialTheme.typography.bodyMedium,
//        color = MaterialTheme.colorScheme.onSurfaceVariant,
//        textAlign = TextAlign.Center
//    )
//}
//
//@Composable
//fun AnalyticsCard(
//    crashReportsEnabled: Boolean,
//    onToggle: (Boolean) -> Unit
//) {
//    Card(
//        shape = RoundedCornerShape(20.dp),
//        elevation = CardDefaults.cardElevation(6.dp),
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    "Usage & Crash Reports",
//                    fontWeight = FontWeight.Bold,
//                    style = MaterialTheme.typography.titleMedium
//                )
//
//                Spacer(Modifier.height(4.dp))
//
//                Text(
//                    "Helps improve app performance",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            XpensoToggle(
//                checked = crashReportsEnabled,
//                onCheckedChange = onToggle
//            )
//        }
//    }
//}
//
//@Composable
//fun TrustRow() {
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        Icon(
//            imageVector = Icons.Default.CheckCircle,
//            contentDescription = null,
//            tint = Color.Green,
//            modifier = Modifier.size(18.dp)
//        )
//        Spacer(Modifier.width(8.dp))
//        Text(
//            "Your data is anonymous and secure",
//            style = MaterialTheme.typography.bodySmall
//        )
//    }
//}
//
//@Composable
//fun AnalyticsFooter(
//    annotatedText: AnnotatedString,
//    onBack: () -> Unit,
//    onStart: () -> Unit,
//    onOpenTerms: () -> Unit
//) {
//    Column {
//
//        ClickableText(
//            text = annotatedText,
//            style = MaterialTheme.typography.bodySmall.copy(
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center
//            ),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp),
//            onClick = { offset ->
//                annotatedText.getStringAnnotations(offset, offset)
//                    .firstOrNull()?.let { annotation ->
//                        when (annotation.tag) {
//                            "TERMS" -> onOpenTerms()
//                            "PRIVACY" -> onOpenTerms()
//                        }
//                    }
//            }
//        )
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 30.dp)
//        ) {
//
//            TextButton(
//                onClick = onBack,
//                modifier = Modifier.align(Alignment.Start)
//            ) {
//                Text(
//                    "Back",
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            Spacer(Modifier.height(8.dp))
//
//            Button(
//                onClick = onStart,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(54.dp),
//                shape = RoundedCornerShape(16.dp),
//                elevation = ButtonDefaults.buttonElevation(6.dp)
//            ) {
//                Text(
//                    "Get Started",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}