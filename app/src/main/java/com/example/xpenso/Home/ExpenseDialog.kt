package com.example.xpenso.Home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xpenso.AddExpenses.AmountSection
import com.example.xpenso.AddExpenses.CalcGrid
import com.example.xpenso.AddExpenses.CardTypeBottomSheet
import com.example.xpenso.AddExpenses.PaymentModeSection
import com.example.xpenso.AddExpenses.CategorySection
import com.example.xpenso.AddExpenses.NotesField
import com.example.xpenso.AddExpenses.OPERATORS
import com.example.xpenso.AddExpenses.cleanAmount
import com.example.xpenso.AddExpenses.displaySegment
import com.example.xpenso.AddExpenses.evaluateExpression
import com.example.xpenso.AddExpenses.hasOperator
import com.example.xpenso.Transaction.ExpenseCategory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// PRIVATE HELPERS
// ─────────────────────────────────────────────────────────────────────────────


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    selectedDate: Long,
    title: String,
    confirmText: String,
    name: String,
    amount: String,
    paymentMode: String,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    initialCategory: ExpenseCategory? = null,
    onConfirm: (ExpenseCategory, Boolean) -> Unit,
    onDismiss: () -> Unit,
    isEdit: Boolean = false,
    currencySymbol: String
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val hideKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    var isAmountFocused by remember { mutableStateOf(false) }
    var isItemFocused by remember { mutableStateOf(false) }
    // Full internal expression (e.g. "56+7"
    //
    //
    // )
    var expr by remember {
        mutableStateOf(
            if (amount == "0" || amount.isEmpty())
                ""
            else
                try {
                    cleanAmount(amount.toDoubleOrNull() ?: 0.0)
                } catch (e: Exception) {
                    amount
                }
        )
    }

    var isAutoSuggested by remember { mutableStateOf(false) }
    var shakeTrigger by remember { mutableIntStateOf(0) }
    var itemText by remember { mutableStateOf(name) }
    var cardSheetVisible by remember { mutableStateOf(false) }
    var selectedCategory by remember {
        mutableStateOf(initialCategory)
    }

    var isCategoryManuallyChanged by remember {
        mutableStateOf(initialCategory != null)
    }

    LaunchedEffect(Unit) {

        expr =
            if (amount == "0" || amount.isEmpty())
                ""
            else
                try {
                    cleanAmount(amount.toDouble())
                } catch (e: Exception) {
                    amount
                }

        itemText = name
    }


    // ── Display logic ──────────────────────────────────────────────────────────
    // What to show in the large amount field
    val segment = displaySegment(expr)
    // "segment" can be:
    //   ""        → expression ends with an operator (user just tapped +/−/×/÷)
    //   "7"       → user typed a digit after the operator
    //   "56"      → no operator yet, just a number
    val displayText = segment.ifEmpty { "0" }
    val isZeroPlaceholder = expr.isEmpty()
    val isExpression = hasOperator(expr)
    val endsWithOperator = isExpression && segment.isEmpty()

    // Save enabled only when there's a concrete number (no dangling operator) and item
    val safeExpr = expr.trimEnd('+', '-', '*', '/')
    val amountValue =
        evaluateExpression(safeExpr).toDoubleOrNull() ?: 0.0
    val haptic = LocalHapticFeedback.current
    val canSave = expr.isNotEmpty()
            && !endsWithOperator
            && itemText.isNotBlank()
            && paymentMode.isNotBlank()
            && amountValue != 0.0

    LaunchedEffect(isAutoSuggested) {
        if (isAutoSuggested) {
            delay(800) // not 2 sec, keep fast
            isAutoSuggested = false
        }
    }
    // ── Calculator actions ─────────────────────────────────────────────────────
    fun appendCalc(v: String) {

        // Do not allow zero as first input
//        if (expr.isEmpty() && v == "0") {
//            return
//        }

        val last = expr.lastOrNull()
        val isOp = v.length == 1 && v[0] in OPERATORS
        val lastIsOp = last != null && last in OPERATORS


        when {

            // allow negative start
            // allow operator at start
            // allow only minus after operator
            // operator pressed
            isOp -> {

                // operator at start
                if (expr.isEmpty()) {

                    if (v == "-") {
                        expr = "-"
                        onAmountChange(expr)
                        return
                    }
                    expr = "0$v"
                    onAmountChange(expr)
                    return
                }

                // replace 0 with operator
                if (expr == "0") {
                    expr = "0$v"
                    onAmountChange(expr)
                    return
                }

                // replace last operator
                if (lastIsOp) {
                    expr = expr.dropLast(1) + v
                    onAmountChange(expr)
                    return
                }

                // normal operator
                expr += v
                onAmountChange(expr)
                return
            }

            // replace last operator
//            isOp && lastIsOp -> {
//                expr = expr.dropLast(1) + v
//            }

            // decimal start
            v == "." && (expr.isEmpty() || lastIsOp) -> {
                expr += "0."
            }

            // prevent multiple decimal in same number
            v == "." -> {
//                val lastNumber = expr.split(Regex("[+\\-*/]")).last()
                val lastNumber = expr.takeLastWhile { it !in OPERATORS }
                if (!lastNumber.contains(".")) {
                    expr += "."
                }
            }


            else -> {

                // Allow operators always
//                if (isOp) {
//                    expr += v
//                    onAmountChange(expr)
//                    return
//                }

//                val lastNumber = expr.split(Regex("[+\\-*/]")).last()
                val lastNumber = expr.takeLastWhile { it !in OPERATORS }
                // prevent leading multiple zeros
                if (lastNumber == "0" && v == "0") return

// replace 0 with new digit
                if (lastNumber == "0" && v != ".") { // !isOp
                    expr = expr.dropLast(1) + v
                    onAmountChange(expr)
                    return
                }

                val integerPart = lastNumber.substringBefore(".")//, lastNumber)

                val decimalPart =
                    if (lastNumber.contains("."))
                        lastNumber.substringAfter(".")
                    else
                        ""

                // Block digits after 9 integer digits
                if (integerPart.length >= 9 && !lastNumber.contains(".")) {
                    shakeTrigger++
                    return
                }

                // Block more than 2 decimal digits
                if (decimalPart.length >= 2) {
                    shakeTrigger++
                    return
                }

                val newExpr = expr + v

// detect divide by zero
//                expr += v
                expr = newExpr
            }

        }

        onAmountChange(expr)
    }

    var hasError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun evaluate() {

        if (expr.isEmpty()) return

        var safeExpr = expr

        if (safeExpr.last() in OPERATORS) {
            safeExpr = safeExpr.dropLast(1)
        }

        val result = evaluateExpression(safeExpr)

        val value = result.toDoubleOrNull()

// detect divide by zero / invalid
        if (value == null || value.isNaN() || value.isInfinite()) {
            hasError = true
            haptic.performHapticFeedback(
                HapticFeedbackType.LongPress
            )
            shakeTrigger++

            expr = ""
            onAmountChange("0")
            return
        }
        val plainResult = try {
            result.toBigDecimal().toPlainString()
        } catch (e: Exception) {
            result
        }

        // limit 9 digit before decimal
        val integerPart = result.substringBefore(".").trimStart('-')

        if (integerPart.length > 9) {

            hasError = true
            haptic.performHapticFeedback(
                HapticFeedbackType.LongPress
            )
            shakeTrigger++

            expr = ""
            onAmountChange("0")
            return
        }

        if (plainResult == "0") {
            expr = ""
            onAmountChange("0")
        } else {
            expr = plainResult
            onAmountChange(expr)
        }
    }

    fun backspace() {
        if (expr.isNotEmpty()) {
            expr = expr.dropLast(1)
            onAmountChange(expr.ifEmpty { "0" })
        }
    }

    fun clearAll() {
        expr = ""
        onAmountChange("0")
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },

                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                actions = {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = SimpleDateFormat(
                                "dd MMM yyyy",
                                Locale.getDefault()
                            ).format(Date(selectedDate)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenHeight = maxHeight

                val isCompact = screenHeight < 650.dp
                val isMedium = screenHeight < 800.dp
                val isExpanded = screenHeight >= 800.dp

                val amountVertPad = when {
                    isCompact -> 2.dp
                    isMedium -> 3.dp
                    else -> 4.dp
                }

                val itemVertPad = when {
                    isCompact -> 2.dp
                    isMedium -> 4.dp
                    else -> 8.dp
                }

                val notesVertPad = when {
                    isCompact -> 2.dp
                    isMedium -> 6.dp
                    else -> 10.dp
                }

                val calcVertPad = when {
                    isCompact -> 2.dp
                    isMedium -> 6.dp
                    else -> 10.dp
                }

                val calcRowSpacing = when {
                    isCompact -> 5.dp
                    isMedium -> 7.dp
                    else -> 10.dp
                }

                val calcButtonHeight = when {
                    isCompact -> 48.dp
                    isMedium -> 56.dp
                    else -> 64.dp
                }

                val saveBtnHeight = when {
                    isCompact -> 48.dp
                    isMedium -> 54.dp
                    else -> 60.dp
                }

                val bottomSpacing = when {
                    isCompact -> 4.dp
                    isMedium -> 8.dp
                    else -> 16.dp
                }

                val dividerVertPad = when {
                    isCompact -> 2.dp
                    isMedium -> 4.dp
                    else -> 6.dp
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                    ) {

                    // ── AMOUNT DISPLAY ────────────────────────────────────────
                    AmountSection(
                        currencySymbol = currencySymbol,
                        displayText = displayText,
                        isZeroPlaceholder = isZeroPlaceholder,
                        showBackspace = !isZeroPlaceholder, //&& expr != "0",
                        onBackspace = { backspace() },
                        onClear = { clearAll() },
                        shakeTrigger = shakeTrigger,
                        onAmountClick = {
                            isAmountFocused = true
                        },
                        verticalPadding = amountVertPad
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // ── PAYMENT MODE ──────────────────────────────────────────
                        Box(modifier = Modifier.weight(1f)) {
                            PaymentModeSection(
                                paymentMode = paymentMode,
                                onPaymentModeChange = onPaymentModeChange,
                                onCardClick = { cardSheetVisible = true },
                                onClick = { hideKeyboard() }

                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            CategorySection(
                                selectedCategory = selectedCategory,
                                isSuggested = isAutoSuggested,
                                onCategoryChange = {
                                    hideKeyboard()
                                    selectedCategory = it
                                    isCategoryManuallyChanged = true
                                },
                                onClick = { hideKeyboard() }

                            )
                        }
                    }
                    // ── ITEM FIELD ────────────────────────────────────────────
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = itemVertPad)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "ITEM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.6.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (isItemFocused && itemText.isNotEmpty()) {
                                Text(
                                    text = "${itemText.length}/30",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

//                        Spacer(Modifier.height(2.dp))

                        OutlinedTextField(
                            value = itemText,
                            onValueChange = {
                                if (it.length <= 30) {
                                    itemText = it
                                    onNameChange(it)

//                                    if (it.isBlank() && selectedCategory != null) {
//                                        isCategoryManuallyChanged = false
//                                        selectedCategory = null   // optional but better UX
//                                    }

                                    if (!isCategoryManuallyChanged) {

                                        if (it.isBlank()) {
                                            selectedCategory = null
                                        } else {

                                            val detected = ExpenseCategory.detect(it)

                                            if (detected != selectedCategory) {
                                                selectedCategory = detected

                                                if (detected != ExpenseCategory.OTHER) {
                                                    isAutoSuggested = true
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            label = { Text("Item") },
                            placeholder = { Text("e.g. Lunch, Netflix, Rent…") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isItemFocused = it.isFocused },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                        )
                    }
                    NotesField(
                        notes = notes,
                        onNotesChange = onNotesChange,
                        verticalPadding = notesVertPad
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

// ── CALCULATOR GRID ───────────────────────────────────────
                    CalcGrid(
                        onKey = { k ->

                            // 🔥 CLOSE KEYBOARD FIRST
                            focusManager.clearFocus()
                            keyboardController?.hide()

                            if (k == "=") evaluate()
                            else appendCalc(k)
                        },
                        verticalPadding = calcVertPad,    // ← ADD
                        rowSpacing = calcRowSpacing,      // ← ADD
                        buttonHeight = calcButtonHeight,  // ← ADD
                        modifier = if (isExpanded) {
                            Modifier.height(320.dp)
                        } else {
                            Modifier.weight(1f)
                        }
                    )
                    Spacer(Modifier.height(bottomSpacing))

// ── SAVE BUTTON ───────────────────────────────────────────────
                    Button(
                        onClick = {

                            evaluate()

//                            if (oldExpr != expr && expr.startsWith("-")) {
//                                return@Button
//                            }
                            if (hasError) {
                                hasError = false
                                Toast
                                    .makeText(
                                        context,
                                        "Invalid amount",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@Button
                            }

                            val safeExpr = expr.trimEnd('+', '-', '*', '/')
                            val amountValue =
                                evaluateExpression(safeExpr).toDoubleOrNull() ?: 0.0

                            if (amountValue == 0.0) {
                                Toast
                                    .makeText(
                                        context,
                                        "Amount cannot be zero",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@Button
                            }

                            // Item empty
                            if (itemText.isBlank()) {
                                Toast
                                    .makeText(
                                        context,
                                        "Item cannot be empty",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@Button
                            }

                            // Payment mode empty
                            if (paymentMode.isBlank()) {
                                Toast
                                    .makeText(
                                        context,
                                        "Select payment mode",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                return@Button
                            }

                            // Zero amount
                            val isNegative = amountValue < 0

                            if (isNegative) {
                                Toast
                                    .makeText(
                                        context,
                                        "Amount is negative, but positive value will be saved",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }

                            val finalCategory =
                                selectedCategory ?: ExpenseCategory.OTHER

                            onConfirm(finalCategory, isNegative)
                        },
//                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(saveBtnHeight),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(bottomSpacing))

                    }
                }
            }
        }

        // ── CARD TYPE BOTTOM SHEET ─────────────────────────────────────────────────
        if (cardSheetVisible) {
            CardTypeBottomSheet(
                currentSelection = paymentMode,
                onDebitClick = {
                    onPaymentModeChange("Debit Card")
                    cardSheetVisible = false
                },
                onCreditClick = {
                    onPaymentModeChange("Credit Card")
                    cardSheetVisible = false
                },
                onNoneClick = {
                    onPaymentModeChange("Card")
                    cardSheetVisible = false
                },
                onDismiss = { cardSheetVisible = false }
            )
        }
    }
}


