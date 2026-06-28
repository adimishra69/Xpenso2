package com.example.xpenso.Settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.AppStart.AnalyticsHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBack: () -> Unit) {

    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("Terms")
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Terms & Policy",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Last updated: April 27, 2026",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Xpenso is built with a privacy-first approach. By using this app, you agree to the following terms designed to keep your data safe and your experience reliable.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // TERMS
            TermsHeader("Terms of Service", Icons.Default.Gavel)

            TermsItem(
                title = "1. Acceptance of Terms",
                content = "By using Xpenso, you agree to these terms. If you do not agree, please discontinue use of the app."
            )

            TermsItem(
                title = "2. User Responsibility",
                content = "You are responsible for how you use the app and for maintaining the security of your device and data."
            )

            TermsItem(
                title = "3. Intellectual Property",
                content = "All design, features, and functionality of Xpenso are owned by the developer and protected under applicable laws."
            )

            Spacer(Modifier.height(16.dp))

            // PRIVACY
            TermsHeader("Privacy Policy", Icons.Default.PrivacyTip)

            TermsItem(
                title = "Data Collection",
                content = "Xpenso does not collect or store your personal financial data. All records remain on your device. The app may use anonymous analytics (such as Firebase Analytics) to improve user experience."
            )

            TermsItem(
                title = "Local Storage",
                content = "Your personal financial data is never transmitted to external servers. Anonymous analytics data may be collected to improve the app."
            )

            TermsItem(
                title = "Analytics",
                content = "Xpenso uses Firebase Analytics (by Google) to collect anonymous usage data such as app interactions and device information. This data does not include personal financial information and does not personally identify users. You can disable analytics anytime from app settings."
            )

            TermsItem(
                title = "Security",
                content = "We recommend using device-level security such as PIN or biometric lock to protect your data.",
                icon = Icons.Default.Security
            )

            Spacer(Modifier.height(32.dp))

            // CONTACT
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.89f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactSupport, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Have questions?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Reach out anytime. Xpenso is actively improved based on your feedback.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val email = "xpenso.app.contact@gmail.com"
                            val subject = "Xpenso App Feedback"

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                            }

                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("xpenso.app.contact@gmail.com")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://xpenso-official.github.io/xpenso-policy/")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View full policy online")
            }
            Spacer(Modifier.height(48.dp))

            Text(
                text = "© 2026 Xpenso. All rights reserved",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun TermsHeader(title: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun TermsItem(title: String, content: String, icon: ImageVector? = null) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp).padding(end = 6.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 26.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
