package com.example.xpesno.AppStart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myproject.Settings.SettingsScreen
import com.example.xpenso.*
import com.example.xpenso.Budget.BudgetScreen
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.Home.HomeScreen
import com.example.xpenso.InsightsModel.InsightsScreen
import com.example.xpenso.Transaction.TransactionFilter
import com.example.xpenso.Transaction.TransactionScreen
import com.example.xpenso.utils.BottomTab
import java.util.Calendar

@Composable
fun AppScaffold(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    currencySymbol: String,
    settingsStore: SettingsStore,
    expenseViewModel: ExpenseViewModel,
    // Navigation Callbacks
    onNavigateToNotifications: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onChangeCurrency: () -> Unit,
    onNavigateToReports: () -> Unit,
    // Transaction Filter States
    initialTransactionFilter: TransactionFilter,
    initialTransactionSearch: String,
    initialCustomStart: Long?,
    initialCustomEnd: Long?,
    // Insight Callback
    onCategoryClick: (String, Long, Long) -> Unit,

    isTransactionFromInsights: Boolean
) {
    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
                NavigationBar(
                    tonalElevation = 4.dp
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.HOME,
                        onClick = { onTabSelected(BottomTab.HOME) },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = {
                            Text("Home", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.TRANSACTIONS,
                        onClick = { onTabSelected(BottomTab.TRANSACTIONS) },
                        icon = { Icon(Icons.Default.ReceiptLong, null) },
                        label = {
                            Text(
                                "Transactions",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.INSIGHTS,
                        onClick = { onTabSelected(BottomTab.INSIGHTS) },
                        icon = { Icon(Icons.Default.BarChart, null) },
                        label = {
                            Text("Insights", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.BUDGETS,
                        onClick = { onTabSelected(BottomTab.BUDGETS) },
                        icon = { Icon(Icons.Default.Wallet, null) },
                        label = {
                            Text("Budgets", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.SETTINGS,
                        onClick = { onTabSelected(BottomTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = {
                            Text("Settings", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        colors = navItemColors
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
            when (selectedTab) {
                BottomTab.HOME -> HomeScreen(
                    currencySymbol,
                    settingsStore,
                    expenseViewModel,
                    onNavigateToNotifications = onNavigateToNotifications
                )

                BottomTab.TRANSACTIONS -> {
                    TransactionScreen(
                        currencySymbol = currencySymbol,
                        expenseViewModel = expenseViewModel,
                        settingsStore = settingsStore,
                        onNavigateToPremium = onNavigateToPremium,
                        initialFilter = initialTransactionFilter,
                        initialSearch = initialTransactionSearch,
                        initialStartDate = initialCustomStart,
                        initialEndDate = initialCustomEnd
                        ,isFromInsights = isTransactionFromInsights
                    )

                    // Reset logic remains in MainScreen's scope usually,
                    // but we keep the LaunchedEffect here if we want it encapsulated.
                    LaunchedEffect(Unit) {
                        // These are handled by the parent MainScreen setting them to ALL/empty
                    }
                }

                BottomTab.INSIGHTS -> InsightsScreen(
                    currencySymbol = currencySymbol,
                    expenseViewModel = expenseViewModel,
                    settingsStore = settingsStore,
                    onCategoryClick = onCategoryClick,
                    onNavigateToPremium = onNavigateToPremium
                )

                BottomTab.BUDGETS -> BudgetScreen(currencySymbol, settingsStore, expenseViewModel)
                BottomTab.SETTINGS -> SettingsScreen(
                    settingsStore = settingsStore,
                    expenseViewModel = expenseViewModel,
                    onNavigateToSecurity = onNavigateToSecurity,
                    onNavigateToAbout = onNavigateToAbout,
                    onChangeCurrency = onChangeCurrency,
                    onNavigateToPremium = onNavigateToPremium,
                    onNavigateToReports = onNavigateToReports
                )
            }
        }
    }
}
