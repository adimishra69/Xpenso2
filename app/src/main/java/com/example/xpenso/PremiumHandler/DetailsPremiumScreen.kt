package com.example.xpenso.PremiumHandler

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODEL
// ─────────────────────────────────────────────────────────────────────────────

private data class Feature(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private data class FeatureSection(
    val label: String,
    val items: List<Feature>
)

// ─────────────────────────────────────────────────────────────────────────────
// FEATURE CONTENT  (everything the user gets with Premium)
// ─────────────────────────────────────────────────────────────────────────────

private val sections = listOf(
    FeatureSection(
        label = "REPORTS",
        items = listOf(
            Feature(
                Icons.Outlined.CalendarMonth,
                "Yearly Financial Report",
                "Full year spending insights and analytics"
            ),
            Feature(
                Icons.Outlined.PieChart,
                "Monthly Category Breakdown",
                "Export a detailed report for any spending category"
            ),
            Feature(
                Icons.Outlined.Tune,
                "Custom Date Range Reports",
                "Generate reports for any specific time period"
            )
        )
    ),
    FeatureSection(
        label = "DATA EXPORT",
        items = listOf(
            Feature(
                Icons.Outlined.PictureAsPdf,
                "Export as PDF",
                "Clean, printable document of all your records"
            ),
            Feature(
                Icons.Outlined.TableChart,
                "Export as CSV / Excel",
                "Open your data directly in Google Sheets or Excel"
            )
        )
    ),
    FeatureSection(
        label = "ANALYTICS & INSIGHTS",
        items = listOf(
            Feature(
                Icons.Outlined.TrendingUp,
                "Yearly Insights",
                "Get a complete breakdown of your yearly spending"
            ),
            Feature(
                Icons.Outlined.AutoGraph,
                "Year Projection",
                "Spending forecast based on your habits"
            ),
            Feature(
                Icons.Outlined.CompareArrows,
                "Year-over-Year Comparison",
                "Compare spending patterns across years"
            )
        )
    ),
    FeatureSection(
        label = "GENERAL",
        items = listOf(
            Feature(
                Icons.Outlined.CloudSync,
                "Cloud Backup & Sync",
                "Keep your data safe across all devices"
            ),
            Feature(
                Icons.Outlined.Block,
                "No Ads, Ever",
                "Completely ad-free experience"
            )
        )
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumDetailsScreen(
    isPremium: Boolean,
    onUpgradeClick: () -> Unit,
    onBackClick: () -> Unit
) {

    BackHandler {
        onBackClick()
    }
    var showSheet by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xpenso Premium", ) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (!isPremium) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 6.dp) // ← shift upward
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .padding(bottom = 29.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "One-time purchase · No subscription",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(6.dp))

                        Button(
                            onClick = onUpgradeClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Get Premium",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Hero ─────────────────────────────────────────────────────────
            HeroBanner(isPremium)

            Spacer(Modifier.height(28.dp))

            // ── What you get ─────────────────────────────────────────────────
            Text(
                text = "Everything included",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            sections.forEach { section ->
                SectionBlock(section)
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO BANNER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(isPremium: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isPremium) "Premium Active" else "Unlock Everything",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isPremium)
                    "All reports, exports and analytics are unlocked"
                else
                    "Yearly reports, PDF & CSV exports, budget analysis,\nand advanced insights — one upgrade",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION BLOCK
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionBlock(section: FeatureSection) {
    Text(
        text = section.label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            section.items.forEachIndexed { index, feature ->
                FeatureRow(feature)
                if (index < section.items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FEATURE ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeatureRow(feature: Feature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        // Premium check badge
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM UPGRADE CARD  — drop this into Reports & other screens
// ─────────────────────────────────────────────────────────────────────────────

//@Composable
//fun PremiumUpgradeCard(
//    isPremium: Boolean,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 16.dp)
//            .animateContentSize(),
//        shape = RoundedCornerShape(22.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.primaryContainer
//        ),
//        elevation = CardDefaults.cardElevation(0.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { onClick() }
//                .padding(18.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = Icons.Default.WorkspacePremium,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(34.dp)
//            )
//
//            Spacer(Modifier.width(16.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = if (isPremium) "Xpenso Premium Active" else "Xpenso Premium",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//                Text(
//                    text = if (isPremium)
//                        "All features unlocked and active"
//                    else
//                        "Upgrade to unlock all features",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = if (isPremium) "View" else "Upgrade",
//                    color = MaterialTheme.colorScheme.primary,
//                    fontWeight = FontWeight.SemiBold,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                Spacer(Modifier.width(4.dp))
//                Icon(
//                    imageVector = Icons.Default.ChevronRight,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(18.dp)
//                )
//            }
//        }
//    }
//}