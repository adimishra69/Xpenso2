package com.example.xpenso.Transaction
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.xpenso.AppStart.AnalyticsHelper
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.xpenso.ExpenseViewmodel.ExpenseViewModel
import com.example.xpenso.PremiumHandler.PremiumFeatureSheet
import com.example.xpenso.SettingsStore
import com.example.xpenso.utils.AppTopBar
import com.example.xpenso.utils.PremiumDatePickerDialog
import kotlinx.coroutines.delay

enum class TransactionFilter(val label: String) {
    ALL("All"),
    THIS_CYCLE("This Month"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    currencySymbol: String,
    expenseViewModel: ExpenseViewModel,
    settingsStore: SettingsStore,
    initialFilter: TransactionFilter = TransactionFilter.ALL,
    initialSearch: String = "",
//    onNavigateToPremium: () -> Unit,
    initialStartDate: Long? = null, // Add this
    initialEndDate: Long? = null,
    isFromInsights: Boolean = false// Add this
) {
    val allExpenses by expenseViewModel.allExpenses.collectAsState(initial = null)
    val startOfMonth by settingsStore.startOfMonth.collectAsState(initial = 1)
//    val isPremium by settingsStore.isPremium.collectAsState(initial = false)

    var filterMode by remember { mutableStateOf(TransactionFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showCustomPicker by remember { mutableStateOf(initialFilter == TransactionFilter.CUSTOM) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }

    var tempStartDate by remember { mutableStateOf(initialStartDate) }
    var tempEndDate by remember { mutableStateOf(initialEndDate) }

    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    var showSearch by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
//    var isLoading by remember { mutableStateOf(true) }
//    var showPremiumSheet by remember { mutableStateOf(false) }

//    val haptic = LocalHapticFeedback.current


    val isKeyboardOpen =
        WindowInsets.ime.getBottom(density) > 0
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
        }
    }
    var isMinTimeDone by remember { mutableStateOf(true) }
    val isScreenLoading = (allExpenses == null) || !isMinTimeDone
    /* ---------------- FILTER BY DATE ---------------- */
    LaunchedEffect(Unit) {
        AnalyticsHelper.trackScreen("Transactions")
    }


    var isFirstLoad by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isFirstLoad) {

            if (isFromInsights) {
                // 🔥 Apply Insights state
                filterMode = initialFilter
                searchQuery = initialSearch

            } else {
                // 🔥 Normal open → reset everything
                filterMode = TransactionFilter.ALL
                searchQuery = ""
                customStartDate = null
                customEndDate = null
            }

            isFirstLoad = false
        }
    }

    val dateFilteredList = remember(
        allExpenses,
        filterMode,
        startOfMonth,
        customStartDate,
        customEndDate
    ) {
        val list = allExpenses ?: emptyList()

        when (filterMode) {

            TransactionFilter.THIS_CYCLE -> {

                val startCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, startOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val now = Calendar.getInstance()

                if (now.before(startCal)) {
                    startCal.add(Calendar.MONTH, -1)
                }

                val endCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                    add(Calendar.MILLISECOND, -1)
                }

                list.filter {
                    it.timestamp in startCal.timeInMillis..endCal.timeInMillis
                }
            }

            TransactionFilter.THIS_YEAR -> {

                val nowCal = Calendar.getInstance()

                val startCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, startOfMonth)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (nowCal.before(startCal)) {
                    startCal.add(Calendar.YEAR, -1)
                }

                val endCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.YEAR, 1)
                    add(Calendar.MILLISECOND, -1)
                }

                list.filter {
                    it.timestamp in startCal.timeInMillis..endCal.timeInMillis
                }
            }

            TransactionFilter.CUSTOM -> {

                if (customStartDate != null && customEndDate != null) {

                    val start = customStartDate!!
                    val end = customEndDate!!

                    list.filter {
                        it.timestamp in start..end
                    }

                } else {
                    emptyList()
                }
            }

            TransactionFilter.ALL -> list

        }.sortedByDescending { it.timestamp }
    }

    /* ---------------- SEARCH FILTER ---------------- */

    val finalList = remember(dateFilteredList, searchQuery) {
        if (searchQuery.isBlank()) {
            dateFilteredList
        } else {
            val query = searchQuery.lowercase()

            dateFilteredList.filter { expense ->

                val category =
                    ExpenseCategory.entries
                        .find { it.name == expense.category }
                        ?: ExpenseCategory.OTHER

                val matchesNormalSearch =  expense.title.lowercase().contains(query) ||
                        expense.paymentMode.lowercase().contains(query) ||
                        expense.amount.toString().contains(query)

                val normalizedQuery = normalize(searchQuery)
                val normalizedCategory = normalize(category.name)

                if (isFromInsights) {
                    matchesNormalSearch ||
                            normalizedCategory.contains(normalizedQuery)
                } else {
                    matchesNormalSearch
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),

        topBar = {
                Column {
                    // ⭐ Screen title
                    AppTopBar(
                        title = "Transactions",
                        actions = {
                            IconButton(onClick = { showSearch = !showSearch }) {
                                Icon(
                                    imageVector = if (showSearch) Icons.Default.Close
                                                  else Icons.Default.Search,
                                    contentDescription = "Toggle Search"
                                )
                            }
                        }
                    )

                        Column {
                    // ⭐ Filter Tabs
                    ScrollableTabRow(
                        selectedTabIndex = filterMode.ordinal,
                        edgePadding = 16.dp,
                        divider = {},
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        TransactionFilter.entries.forEach { mode ->
                            Tab(
                                selected = filterMode == mode,
                                onClick = {

                                    // 🔒 Block Custom Range for free users
//                                    if (mode == TransactionFilter.CUSTOM && !isPremium) {
//                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                                        // Navigate to premium screen instead
//                                        showPremiumSheet = true
//                                        return@Tab
//                                    }

                                    if (mode != TransactionFilter.CUSTOM) {
                                        customStartDate = null
                                        customEndDate = null
                                    }

                                    filterMode = mode

                                    if (mode == TransactionFilter.CUSTOM) {

                                        tempStartDate = customStartDate
                                        tempEndDate = customEndDate

                                        showCustomPicker = true
                                    }
                                },
                                text = {

//                                    val isLocked = mode == TransactionFilter.CUSTOM && !isPremium

                                    val contentAlpha =  1f

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.graphicsLayer(alpha = contentAlpha)
                                    ) {

                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.labelLarge
                                        )

                                    }
                                }

                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // ⭐ Search Bar
                if(showSearch){
                    AnimatedVisibility(
                        visible = showSearch,
                        enter = fadeIn(tween(180)) +
                                expandVertically(
                                    animationSpec = tween(220),
                                    expandFrom = Alignment.Top
                                ),
                        exit = fadeOut(tween(150)) +
                                shrinkVertically(
                                    animationSpec = tween(200),
                                    shrinkTowards = Alignment.Top
                                )
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .animateContentSize(), // smooth internal resize
                            placeholder = { Text("Search transactions") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(18.dp), // 🔻 smaller size
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // 🔻 lighter color
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
                        }
                }
        }
    ) { padding ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    if (isScreenLoading) {
                           FullScreenTransactionShimmer()
                    }

                    else if (finalList.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            val grouped = finalList.groupBy {
                                getGroupKey(it.timestamp)
                            }

                            grouped.forEach { (header, items) ->

                                item(key = header) {
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                items(items, key = { it.id }) { expense ->
                                    TransactionItem(
                                        expense = expense,
                                        currencySymbol = currencySymbol
                                    )
                                }
                            }
                        }
                    }
                }
            }

    if (showCustomPicker) {

        CustomRangeDialog(

            startDate = tempStartDate,
            endDate = tempEndDate,

            onStartClick = {
                showStartDatePicker = true
            },

            onEndClick = {
                showEndDatePicker = true
            },

            onApply = {

                if (tempStartDate != null && tempEndDate != null) {

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = tempEndDate!!
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }

                    customStartDate = tempStartDate
                    customEndDate = calendar.timeInMillis
                    filterMode = TransactionFilter.CUSTOM
                }

                showCustomPicker = false
            },

            onDismiss = {
                showCustomPicker = false
            }
        )
    }

    if (showStartDatePicker) {

        PremiumDatePickerDialog(
            initialSelectedMillis = tempStartDate ?: System.currentTimeMillis(),
            onDateSelected = {
                tempStartDate = it
            },
            onDismiss = {
                showStartDatePicker = false
            }
        )

    }

    if (showEndDatePicker) {

        PremiumDatePickerDialog(
            initialSelectedMillis = tempEndDate ?: System.currentTimeMillis(),
            onDateSelected = {
                tempEndDate = it
//                showEndDatePicker = false
            },

            onDismiss = {
                showEndDatePicker = false
            }
        )

    }
//    if (showPremiumSheet) {
//        PremiumFeatureSheet(
//            message = "Custom date filtering is available in Xpenso Premium",
//            onDismiss = { showPremiumSheet = false },
//            onUpgradeClick = {
//                showPremiumSheet = false
//                onNavigateToPremium()
//            }
//        )
//    }
}

/* ---------------- HELPERS ---------------- */

fun normalize(text: String): String {
    return text
        .lowercase()
        .replace("_", "")
        .replace(" ", "")
        .replace("-", "")
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun getGroupKey(timestamp: Long): String {
    val now = Calendar.getInstance()

    return when {
        isSameDay(timestamp, now.timeInMillis) -> "Today"
        else -> {
            now.add(Calendar.DAY_OF_YEAR, -1)
            if (isSameDay(timestamp, now.timeInMillis)) "Yesterday"
            else SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(Date(timestamp))
        }
    }
}
