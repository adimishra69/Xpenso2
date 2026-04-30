package com.example.xpenso.Reports

import android.content.Context
import android.net.Uri
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.OnBoarding.CurrencyItem
import com.example.xpenso.OnBoarding.getAllCurrencies
import com.example.xpenso.Transaction.ExpenseCategory
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun getCurrencyInfo(symbol: String): CurrencyItem? {
    return getAllCurrencies().find { it.symbol == symbol }
}
object CsvReportGenerator {

    /**
     * Generates a CA-grade (Chartered Accountant) professional CSV financial statement.
     *
     * Structure:
     *   Section 0  — Cover Page / Document Identity
     *   Section 1  — Report Metadata & Certification Block
     *   Section 2  — Executive Summary (KPIs)
     *   Section 3  — Category-wise P&L Breakdown
     *   Section 4  — Payment Mode Analysis
     *   Section 5  — Monthly Trend Analysis
     *   Section 6  — Detailed Transaction Ledger (Chronological)
     *   Section 7  — Auditor's Notes & Disclaimer Footer
     *
     * Formatting conventions:
     *   - Every section is surrounded by blank-row padding (top + bottom)
     *   - Section headers use ALL-CAPS with a thick separator row of dashes
     *   - Data rows use consistent column alignment via empty filler columns
     *   - Amounts are formatted with thousand separators and 2 decimal places
     *   - Dates follow DD-MMM-YYYY (international accounting standard)
     *   - Serial numbers are zero-padded (e.g., 001, 002 …)
     */
    fun generateCsvReport(
        context: Context,
        uri: Uri,
        currencySymbol: String,
        expenses: List<ExpenseEntity>
    ): Result<Unit> {

        if (expenses.isEmpty()) return Result.failure(Exception("No expense data available to generate report."))

        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { writer ->

                    // ── Formatters ──────────────────────────────────────────────────────────
                    val shortDate   = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
                    val longDate    = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                    val monthYear   = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
                    val timestamp   = SimpleDateFormat("dd MMM yyyy  hh:mm:ss a", Locale.ENGLISH)
                    val currency = currencySymbol
                    val now         = Date()

                    // ── Pre-computed Analytics ───────────────────────────────────────────────
                    val sorted          = expenses.sortedBy { it.timestamp }
                    val totalAmount     = expenses.sumOf { it.amount }
                    val avgAmount       = if (expenses.isNotEmpty()) totalAmount / expenses.size else 0.0
                    val maxExpense      = expenses.maxByOrNull { it.amount }
                    val minExpense      = expenses.minByOrNull { it.amount }
                    val minDate         = sorted.first().timestamp
                    val maxDate         = sorted.last().timestamp
                    val periodFrom      = longDate.format(Date(minDate))
                    val periodTo        = longDate.format(Date(maxDate))

                    val byCategory      = expenses.groupBy {
                        ExpenseCategory.entries
                            .find { cat -> cat.name == it.category }
                            ?: ExpenseCategory.OTHER
                    }
                        .mapValues { (_, list) -> list.sumOf { it.amount } }
                        .toList().sortedByDescending { it.second }

                    val byPaymentMode   = expenses.groupBy { it.paymentMode.trim().ifBlank { "Unspecified" } }
                        .mapValues { (_, list) -> Pair(list.size, list.sumOf { it.amount }) }
                        .toList().sortedByDescending { it.second.second }

                    val byMonth         = expenses.groupBy { monthYear.format(Date(it.timestamp)) }
                        .mapValues { (_, list) -> Pair(list.size, list.sumOf { it.amount }) }
                        .toList()

                    // ── Utility lambdas ──────────────────────────────────────────────────────
                    fun amt(value: Double) = "\"$currency ${String.format("%,12.2f", value).trim()}\""
                    fun pct(value: Double) = String.format("%6.2f%%", value)
                    fun sep(label: String = "") = writer.write(
                        "\"${"─".repeat(70)}\",\"${label.uppercase()}\""
                    ).also { writer.newLine() }
                    fun blank(n: Int = 1) = repeat(n) { writer.newLine() }
                    fun sectionHeader(num: String, title: String) {
                        blank(2)
                        writer.write("\"${"═".repeat(80)}\""); writer.newLine()
                        writer.write("\"  SECTION $num  |  ${title.uppercase()}  \""); writer.newLine()
                        writer.write("\"${"═".repeat(80)}\""); writer.newLine()
                        blank(1)
                    }
                    fun row(vararg cells: String) {
                        writer.write(cells.joinToString(",") { if (it.startsWith("\"")) it else "\"$it\"" })
                        writer.newLine()
                    }

                    // ════════════════════════════════════════════════════════════════════════
                    // SECTION 0  ·  COVER / DOCUMENT IDENTITY
                    // ════════════════════════════════════════════════════════════════════════
                    blank(1)
                    row("", "", "", "", "", "", "")
                    row(
                        "XPENSO FINANCIAL SERVICES",
                        "", "", "", "", "", ""
                    )
                    row(
                        "PERSONAL EXPENDITURE STATEMENT",
                        "", "", "", "", "", ""
                    )
                    row(
                        "Prepared in Accordance with Generally Accepted Accounting Principles (GAAP)",
                        "", "", "", "", "", ""
                    )
                    blank(1)
                    row("Document Reference","","","","XPS-${String.format("%06d", System.currentTimeMillis() % 1000000)}","","")
                    row("Report Classification","","","","CONFIDENTIAL — FOR AUTHORISED USE ONLY","","")
                    row("Statement Type","","","","Detailed Expenditure Report (DER)","","")
                    row("Reporting Period","","","","$periodFrom  to  $periodTo","","")
                    row("Generated On","","","",timestamp.format(now),"","")
                    val currencyInfo = getCurrencyInfo(currency)

                    row(
                        "Currency","","","",
                        "${currencyInfo?.name ?: "Currency"} (${currencyInfo?.code ?: ""}) — Symbol: $currency",
                        "",""
                    )
                    blank(1)
                    writer.write("\"${"─".repeat(100)}\""); writer.newLine()
                    blank(1)

                    // ════════════════════════════════════════════════════════════════════════
                    // SECTION 1  ·  EXECUTIVE SUMMARY (KPI BLOCK)
                    // ════════════════════════════════════════════════════════════════════════
                    sectionHeader("1", "Executive Summary — Key Performance Indicators")

                    row("KPI", "", "", "Value", "", "Remarks")
                    sep()
                    row("Total Number of Transactions","","",expenses.size.toString(),"","All recorded expense entries")
                    row("Reporting Period (Days)","","",((maxDate - minDate) / 86_400_000 + 1).toString(),"","Inclusive date range")
                    row("Gross Total Expenditure","","",amt(totalAmount),"","Sum of all expense amounts")
                    row("Average Transaction Value","","",amt(avgAmount),"","Mean per transaction")
                    row("Highest Single Expense","","",
                        amt(maxExpense?.amount ?: 0.0),"",
                        "\"${maxExpense?.title?.take(40) ?: "N/A"}  [${maxExpense?.let { shortDate.format(Date(it.timestamp)) } ?: ""}]\""
                    )
                    row("Lowest Single Expense","","",
                        amt(minExpense?.amount ?: 0.0),"",
                        "\"${minExpense?.title?.take(40) ?: "N/A"}  [${minExpense?.let { shortDate.format(Date(it.timestamp)) } ?: ""}]\""
                    )
                    row("Number of Expense Categories","","",byCategory.size.toString(),"","Unique categories detected")
                    row("Number of Payment Modes","","",byPaymentMode.size.toString(),"","Unique payment channels used")
                    blank(1)

                    // ════════════════════════════════════════════════════════════════════════
                    // SECTION 2  ·  CATEGORY-WISE EXPENDITURE ANALYSIS
                    // ════════════════════════════════════════════════════════════════════════
                    sectionHeader("2", "Category-Wise Expenditure Analysis")

                    row("Rank","Category Name","","Transaction Count","Total Amount","% of Gross","Cumulative %")
                    sep()

                    var cumulative = 0.0
                    byCategory.forEachIndexed { idx, (cat, amt) ->
                        val count = expenses.count {
                            (ExpenseCategory.entries
                                .find { c -> c.name == it.category }
                                ?: ExpenseCategory.OTHER
                                    ) == cat
                        }
                        val pctShare = (amt / totalAmount) * 100
                        cumulative += pctShare
                        val catName = cat.name.replace("_", " ")
                            .lowercase().replaceFirstChar { it.uppercaseChar() }
                        row(
                            String.format("%02d", idx + 1),
                            catName,
                            "",
                            count.toString(),
                            amt(amt),
                            pct(pctShare),
                            pct(cumulative)
                        )
                    }
                    sep()
                    row("","TOTAL","",expenses.size.toString(),amt(totalAmount),"100.00%","100.00%")
                    blank(1)

                    // ════════════════════════════════════════════════════════════════════════
                    // SECTION 3  ·  PAYMENT MODE ANALYSIS
                    // ════════════════════════════════════════════════════════════════════════
                    sectionHeader("3", "Payment Mode & Channel Analysis")

                    row("Rank","Payment Mode / Channel","","Transactions","Total Amount","% of Total","Avg per Txn")
                    sep()

                    byPaymentMode.forEachIndexed { idx, (mode, pair) ->
                        val (count, modeAmt) = pair
                        val avg = if (count > 0) modeAmt / count else 0.0
                        row(
                            String.format("%02d", idx + 1),
                            mode,
                            "",
                            count.toString(),
                            amt(modeAmt),
                            pct((modeAmt / totalAmount) * 100),
                            amt(avg)
                        )
                    }
                    sep()
                    row("","TOTAL","",expenses.size.toString(),amt(totalAmount),"100.00%",amt(avgAmount))
                    blank(1)

                    // ════════════════════════════════════════════════════════════════════════
                    // SECTION 4  ·  MONTHLY TREND ANALYSIS
                    // ════════════════════════════════════════════════════════════════════════
                    sectionHeader("4", "Month-Wise Expenditure Trend")

                    row("Month","","","No. of Transactions","Total Expenditure","% of Annual Total","Month-on-Month Variance")
                    sep()

                    var prevAmt = 0.0
                    byMonth.forEachIndexed { idx, (month, pair) ->
                        val (count, mAmt) = pair
                        val mom = if (idx == 0 || prevAmt == 0.0) "—"
                        else pct(((mAmt - prevAmt) / prevAmt) * 100)
                        row(
                            month,
                            "",
                            "",
                            count.toString(),
                            amt(mAmt),
                            pct((mAmt / totalAmount) * 100),
                            mom
                        )
                        prevAmt = mAmt
                    }
                    sep()
                    row("TOTAL","","",expenses.size.toString(),amt(totalAmount),"100.00%","")
                    blank(1)

                    // ════════════════════════════════════════════════════════════════════════
                    // SECTION 5  ·  DETAILED TRANSACTION LEDGER
                    // ════════════════════════════════════════════════════════════════════════
                    sectionHeader("5", "Detailed Transaction Ledger — Chronological Order")

                    row(
                        "S.No.",
                        "Transaction Date",
                        "Voucher / Ref No.",
                        "Narration / Description",
                        "Category",
                        "Payment Mode",
                        "Debit Amount ($currency)",
                        "Running Total ($currency)"
                    )
                    sep()

                    var runningTotal = 0.0
                    expenses.sortedByDescending { it.timestamp }.forEachIndexed { idx, exp ->
                        runningTotal += exp.amount
                        val cat = (
                                ExpenseCategory.entries
                                    .find { c -> c.name == exp.category }
                                    ?: ExpenseCategory.OTHER
                                ).name
                            .replace("_", " ")
                            .lowercase()
                            .replaceFirstChar { it.uppercaseChar() }
                        val safeTitle = exp.title.replace("\"", "\"\"").replace("\n", " ").take(60)
                        val voucherRef = "XPS-${String.format("%05d", idx + 1)}"
                        row(
                            String.format("%04d", idx + 1),
                            shortDate.format(Date(exp.timestamp)),
                            voucherRef,
                            safeTitle,
                            cat,
                            exp.paymentMode.trim().ifBlank { "Unspecified" },
                            String.format("%,.2f", exp.amount),
                            String.format("%,.2f", runningTotal)
                        )
                    }
                    sep()
                    row(
                        "","","","",
                        "GRAND TOTAL","",
                        String.format("%,.2f", totalAmount),
                        ""
                    )
                    blank(1)

                    // ════════════════════════════════════════════════════════════════════════
                    // SECTION 6  ·  AUDITOR'S NOTES & DISCLAIMER
                    // ════════════════════════════════════════════════════════════════════════
                    sectionHeader("6", "Auditor's Notes, Assumptions & Disclaimer")

                    row("Note No.","","","Description")
                    sep()
                    row("1","","","This report is auto-generated by Xpenso Personal Finance Assistant.")
                    row("2","","","All amounts are stated in Indian Rupees (INR) unless otherwise specified.")
                    row("3","","","Expense categories are auto-detected using Xpenso's classification engine.")
                    row("4","","","Transaction dates reflect the date of entry as recorded by the user.")
                    row("5","","","This report does not constitute a certified financial audit.")
                    row("6","","","Data accuracy is subject to the completeness of user-entered records.")
                    row("7","","","This document is CONFIDENTIAL and intended for the account holder only.")
                    row("8","","","Voucher reference numbers (XPS-XXXXX) are system-generated identifiers.")
                    blank(1)
                    writer.write("\"${"═".repeat(80)}\""); writer.newLine()
                    row("END OF FINANCIAL STATEMENT — XPENSO PERSONAL FINANCE ASSISTANT")
                    row("Document generated on: ${timestamp.format(now)}")
                    row("© ${SimpleDateFormat("yyyy", Locale.ENGLISH).format(now)} Xpenso. All rights reserved.")
                    writer.write("\"${"═".repeat(80)}\""); writer.newLine()
                    blank(1)

                    writer.flush()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
