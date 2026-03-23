package com.example.xpenso.Reports

import android.content.Context
import android.net.Uri
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.groupBy
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.mapValues
import kotlin.collections.maxByOrNull
import kotlin.collections.maxOf
import kotlin.collections.minOf
import kotlin.collections.sortedByDescending
import kotlin.collections.sumOf
import kotlin.collections.toList
import kotlin.io.use
import kotlin.let
import kotlin.text.format
import kotlin.text.ifBlank
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.replaceFirstChar
import kotlin.text.uppercase

object CsvReportGenerator {

    /**
     * Generates a premium CSV report designed for professional accounting use (Chartered Accountants).
     * Includes Brand info, Financial Summary, Category Analytics, and a Detailed Transaction Ledger.
     * Optimized with structural spacing and professional layout for Excel/Google Sheets.
     */
    fun generateCsvReport(
        context: Context,
        uri: Uri,
        expenses: List<ExpenseEntity>
    ): Result<Unit> {

        if (expenses.isEmpty()) return Result.failure(kotlin.Exception("No data to export"))

        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->

                    // --- FORMATTERS ---
                    val fileDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val timestampFormat =
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    val currency = expenses.firstOrNull()?.currencySymbol ?: "₹"

                    // --- ANALYTICS ---
                    val totalAmount = expenses.sumOf { it.amount }
                    val avgExpense = if (expenses.isNotEmpty()) totalAmount / expenses.size else 0.0
                    val minDate = expenses.minOf { it.timestamp }
                    val maxDate = expenses.maxOf { it.timestamp }
                    val period = "${fileDateFormat.format(Date(minDate))} to ${fileDateFormat.format(
                        Date(maxDate)
                    )}"

                    // 1. --- BRAND & REPORT HEADER ---
                    writer.write("XPENSO - PERSONAL FINANCE ASSISTANT")
                    writer.newLine()
                    writer.write("OFFICIAL FINANCIAL STATEMENT")
                    writer.newLine()
                    writer.write("Report Period,$period")
                    writer.newLine()
                    writer.write("Generated On,${timestampFormat.format(Date())}")
                    writer.newLine()
                    writer.newLine() // Vertical Gap

                    // 2. --- EXECUTIVE SUMMARY ---
                    writer.write("1. EXECUTIVE SUMMARY")
                    writer.newLine()
                    // Increased "cell size" feel by adding spacing columns
                    writer.write("Metric Description,,,Details")
                    writer.newLine()
                    writer.write("Total Transactions recorded,,,${expenses.size}")
                    writer.newLine()
                    writer.write("Total Expenditure for period,,,\"$currency ${String.format("%,.2f", totalAmount)}\"")
                    writer.newLine()
                    writer.write("Average Transaction Value,,,\"$currency ${String.format("%,.2f", avgExpense)}\"")
                    writer.newLine()

                    val highest = expenses.maxByOrNull { it.amount }
                    highest?.let {
                        writer.write("Highest Individual Expense Value,,,\"${it.title} ($currency ${String.format("%,.2f", it.amount)})\"")
                        writer.newLine()
                    }
                    writer.newLine() // Vertical Gap

                    // 3. --- CATEGORY WISE BREAKDOWN ---
                    writer.write("2. CATEGORY BREAKDOWN")
                    writer.newLine()
                    writer.write("Expense Category,,,Total Amount,Percentage (%)")
                    writer.newLine()

                    expenses.groupBy { ExpenseCategory.Companion.detect(it.title) }
                        .mapValues { it.value.sumOf { e -> e.amount } }
                        .toList()
                        .sortedByDescending { it.second }
                        .forEach { (cat, amt) ->
                            val percent = (amt / totalAmount) * 100
                            val catName = cat.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                            writer.write("$catName,,,\"$currency ${String.format("%,.2f", amt)}\",${String.format("%.1f%%", percent)}")
                            writer.newLine()
                        }
                    writer.newLine() // Vertical Gap

                    // 4. --- TRANSACTION LEDGER ---
                    writer.write("3. DETAILED TRANSACTION LEDGER")
                    writer.newLine()

                    // Removed "Currency" column
                    // Added space column between "Date" and "Description"
                    // Increased header word length to increase default column width in some viewers
                    val tableHeaders = listOf(
                        "S.No",
                        "Transaction Date",
                        "", // Structural space between Date and Description
                        "Description / Transaction Details",
                        "Expense Category",
                        "Payment Mode Used",
                        "Total Amount ($currency)"
                    )
                    writer.write(tableHeaders.joinToString(","))
                    writer.newLine()

                    expenses.sortedByDescending { it.timestamp }.forEachIndexed { index, expense ->
                        val category = ExpenseCategory.Companion.detect(expense.title)
                            .name.replace("_", " ")
                            .lowercase().replaceFirstChar { it.uppercase() }

                        val paymentMode = expense.paymentMode.ifBlank { "N/A" }

                        // Clean data for CSV safety
                        val safeTitle = expense.title.replace("\"", "\"\"").replace("\n", " ")

                        val row = listOf(
                            (index + 1).toString(),
                            fileDateFormat.format(Date(expense.timestamp)),
                            "", // Empty cell for spacing
                            "\"$safeTitle\"",
                            category,
                            "\"$paymentMode\"",
                            String.format("%.2f", expense.amount)
                        )
                        writer.write(row.joinToString(","))
                        writer.newLine()
                    }

                    // 5. --- FOOTER ---
                    writer.newLine()
                    writer.write("END OF OFFICIAL REPORT")
                    writer.newLine()
                    writer.write("Prepared using Xpenso Personal Finance Assistant.")

                    writer.flush()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}