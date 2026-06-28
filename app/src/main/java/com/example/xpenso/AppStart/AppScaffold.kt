package com.example.xpenso.AppStart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.*
import com.example.xpenso.R
import com.example.xpenso.Budget.BudgetScreen
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Viewmodel.BudgetViewModel
import com.example.xpenso.Viewmodel.ExpenseViewModel
import com.example.xpenso.Home.HomeScreen
import com.example.xpenso.InsightsModel.InsightsScreen
import com.example.xpenso.Settings.SettingsScreen
import com.example.xpenso.Transaction.TransactionFilter
import com.example.xpenso.Transaction.TransactionScreen
import com.example.xpenso.utils.BottomTab

@Composable
fun AppScaffold(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    currencySymbol: String,
    settingsStore: SettingsStore,
    expenseViewModel: ExpenseViewModel,
    DirectPremium: () -> Unit,
    budgetViewModel: BudgetViewModel,
//    onYearlyBudgetClick: () -> Unit,
    // Navigation Callbacks
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
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
    onDeleteExpenseClick: (ExpenseEntity) -> Unit,
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
                    tonalElevation = 4.dp,
                    modifier = Modifier.height(92.dp)
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )

                    NavigationBarItem(
                        selected = selectedTab == BottomTab.HOME,
                        onClick = { onTabSelected(BottomTab.HOME) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (selectedTab == BottomTab.HOME) R.drawable.ic_home_premium
                                    else R.drawable.home_hollow
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(size = 25.dp).offset(y = 4.dp)
                            )
                        },

                        label = {
                            Text(
                                "Home",
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.offset(y = (-4).dp) // pull text up
                            )
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.TRANSACTIONS,
                        onClick = { onTabSelected(BottomTab.TRANSACTIONS) },
                        icon = {
                            Icon(
                                if (selectedTab == BottomTab.TRANSACTIONS) Icons.Filled.ReceiptLong
                                else Icons.Outlined.ReceiptLong,
                                null,
                                modifier = Modifier.size(25.dp).offset(y = 4.dp)
                            )
                        },
                        label = {
                            Text(
                                "Transactions",
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.offset(y = (-4).dp)
                            )
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.INSIGHTS,
                        onClick = { onTabSelected(BottomTab.INSIGHTS) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (selectedTab == BottomTab.INSIGHTS) R.drawable.insights_solid
                                    else R.drawable.insights_hollow
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(size = 25.dp).offset(y = 4.dp)
                            )
                        },
                        label = {
                            Text(
                                "Insights",
                                maxLines = 1,
                                fontSize = 10.sp,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.offset(y = (-4).dp)
                            )
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.BUDGETS,
                        onClick = { onTabSelected(BottomTab.BUDGETS) },
                        icon = {
                            Icon(
                                painter = if (selectedTab == BottomTab.BUDGETS)
                                    painterResource(R.drawable.wallet_24dp_e3e3e3_fill0_wght400_grad0_opsz24__1_) // replace this
                                else
                                    painterResource(R.drawable.budget_hollow),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(25.dp)
                                    .offset(y = 4.dp)
                            )
                        },
                        label = {
                            Text(
                                "Budgets",
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.offset(y = (-4).dp)
                            )
                        },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.SETTINGS,
                        onClick = { onTabSelected(BottomTab.SETTINGS) },
                        icon = {
                            Icon(
                                if (selectedTab == BottomTab.SETTINGS)
                                    Icons.Filled.Settings
                                else
                                    Icons.Outlined.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(25.dp).offset(y = 4.dp)
                            )
                        },
                        label = {
                            Text(
                                "Settings",
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.offset(y = (-4).dp)
                            )
                        },
                        colors = navItemColors
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding
                (bottom = padding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                BottomTab.HOME -> HomeScreen(
                    currencySymbol = currencySymbol,
                    settingsStore = settingsStore,
                    expenseViewModel = expenseViewModel,
                    budgetViewModel = budgetViewModel,
                    onAddExpense = {
                        onAddExpenseClick()
                    },
                    onEditExpense = { expense ->
                        onEditExpenseClick(expense)
                    },
                    onDeleteExpense = { expense ->
                        onDeleteExpenseClick(expense)
                    }
                )

                BottomTab.TRANSACTIONS -> {
                    TransactionScreen(
                        currencySymbol = currencySymbol,
                        expenseViewModel = expenseViewModel,
                        settingsStore = settingsStore,
                        initialFilter = initialTransactionFilter,
                        initialSearch = initialTransactionSearch,
                        initialStartDate = initialCustomStart,
                        initialEndDate = initialCustomEnd,
                        isFromInsights = isTransactionFromInsights
                    )
                    // Reset logic remains in MainScreen's scope usually,
                    // but we keep the LaunchedEffect here if we want it encapsulated.
                }

                BottomTab.INSIGHTS -> InsightsScreen(
                    currencySymbol = currencySymbol,
                    expenseViewModel = expenseViewModel,
                    settingsStore = settingsStore,
                    budgetViewModel = budgetViewModel,
                    onCategoryClick = onCategoryClick,
                   DirectPremium = DirectPremium
                )

                BottomTab.BUDGETS -> BudgetScreen(
                    currencySymbol,
                    settingsStore,
                    expenseViewModel,
//                    onYearlyBudgetClick,
                    budgetViewModel = budgetViewModel
                )

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
