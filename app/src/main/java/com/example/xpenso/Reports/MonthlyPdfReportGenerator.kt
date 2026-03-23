package com.example.xpenso.Reports

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.TextPaint
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.collections.drop
import kotlin.collections.filter
import kotlin.collections.forEachIndexed
import kotlin.collections.groupBy
import kotlin.collections.mapValues
import kotlin.collections.maxByOrNull
import kotlin.collections.sortedByDescending
import kotlin.collections.sumOf
import kotlin.collections.take
import kotlin.collections.toList
import kotlin.compareTo
import kotlin.io.use
import kotlin.let
import kotlin.text.format
import kotlin.text.ifBlank
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.replaceFirstChar
import kotlin.text.take
import kotlin.text.toInt
import kotlin.text.uppercase
import kotlin.to

object MonthlyPdfReportGenerator {

    private val COLOR_PRIMARY = Color.parseColor("#2196F3")   // Blue
    private val COLOR_TEXT_MAIN = Color.parseColor("#212121") // Dark Grey
    private val COLOR_TEXT_SEC = Color.parseColor("#757575")  // Grey
    private val COLOR_BG_LIGHT = Color.parseColor("#F5F5F5")

    fun generateMonthlyCycleReport(
        context: Context,
        uri: Uri,
        allExpenses: List<ExpenseEntity>,
        month: Int,
        year: Int,
        startOfMonth: Int,
        currencySymbol: String,
        isPremium :Boolean
    ): Result<Unit> {

        val startCal = Calendar.getInstance().apply {
            set(year, month, startOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        val expenses = allExpenses
            .filter { it.timestamp in startCal.timeInMillis..endCal.timeInMillis }
            .sortedByDescending { it.timestamp }

        if (expenses.isEmpty()) return Result.failure(kotlin.Exception("No data for this cycle"))

        return try {

            val document = PdfDocument()

            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

            var pageNumber = 1
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            var yPos = 0f

            val currency = currencySymbol
            val totalAmount = expenses.sumOf { it.amount }
            val avgExpense = totalAmount / expenses.size
            val highestExpense = expenses.maxByOrNull { it.amount }

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val tableDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

            val periodText =
                "${dateFormat.format(startCal.time)} - ${dateFormat.format(endCal.time)}"

            val brandPaint = TextPaint().apply {
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.WHITE
            }

            val titlePaint = TextPaint().apply {
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = COLOR_TEXT_MAIN
            }

            val labelPaint = TextPaint().apply {
                textSize = 10f
                color = COLOR_TEXT_SEC
            }

            val valuePaint = TextPaint().apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = COLOR_PRIMARY
            }

            val tableHeaderPaint = TextPaint().apply {
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.WHITE
            }

            val rowPaint = TextPaint().apply {
                textSize = 9f
                color = COLOR_TEXT_MAIN
            }

            val amountPaint = TextPaint(rowPaint).apply {
                textAlign = Paint.Align.RIGHT
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val footerPaint = TextPaint().apply {
                textSize = 8f
                textAlign = Paint.Align.CENTER
                color = COLOR_TEXT_SEC
            }

            val bgPaint = Paint()


            val watermarkPaint = Paint().apply {
                textSize = 75f
                color = Color.LTGRAY
                alpha = 35
                textAlign = Paint.Align.CENTER
            }

            // HEADER
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, bgPaint)

            canvas.drawText("XPENSO", margin, 60f, brandPaint)

            labelPaint.color = Color.WHITE
            val subtitle =
                if (isPremium) "Monthly Financial Statement"
                else "Basic Monthly Report"

            canvas.drawText(subtitle, margin, 85f, labelPaint)

            val today =
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", pageWidth - margin, 85f, labelPaint)

            labelPaint.textAlign = Paint.Align.LEFT
            labelPaint.color = COLOR_TEXT_SEC

            yPos = 150f

            // SUMMARY
            canvas.drawText("CYCLE SUMMARY", margin, yPos, titlePaint)

            yPos += 35

            bgPaint.color = COLOR_BG_LIGHT

            canvas.drawRoundRect(
                RectF(margin, yPos - 25, pageWidth - margin, yPos + 75),
                12f,
                12f,
                bgPaint
            )

            canvas.drawText("Cycle Period", margin + 20, yPos, labelPaint)
            canvas.drawText(periodText, margin + 20, yPos + 25, valuePaint)

            canvas.drawText("Total Spent", pageWidth / 2f, yPos, labelPaint)
            canvas.drawText(
                "$currency ${String.format("%,.0f", totalAmount)}",
                pageWidth / 2f,
                yPos + 25,
                valuePaint
            )

            canvas.drawText("Average Expense", pageWidth / 2f, yPos + 45, labelPaint)
            canvas.drawText(
                "$currency ${String.format("%,.0f", avgExpense)}",
                pageWidth / 2f,
                yPos + 65,
                valuePaint
            )

            canvas.drawText("Transactions", pageWidth - margin - 110, yPos, labelPaint)
            canvas.drawText("${expenses.size} Entries", pageWidth - margin - 110, yPos + 25, valuePaint)

            highestExpense?.let {
                canvas.drawText("Largest Expense", pageWidth - margin - 110, yPos + 45, labelPaint)
                canvas.drawText(
                    "$currency ${String.format("%,.0f", it.amount)}",
                    pageWidth - margin - 110,
                    yPos + 65,
                    valuePaint
                )
            }

            yPos += 120

            // CATEGORY BREAKDOWN
            if (isPremium) {

                canvas.drawText("CATEGORY BREAKDOWN", margin, yPos, titlePaint)
                yPos += 30

                val categoryMap = expenses
                    .groupBy { ExpenseCategory.detect(it.title) }
                    .mapValues { it.value.sumOf { e -> e.amount } }

                val sortedCategories =
                    categoryMap.toList().sortedByDescending { it.second }

                val top3 = sortedCategories.take(3)

                val othersAmount =
                    sortedCategories.drop(3).sumOf { it.second }

                val categorySummary =
                    if (othersAmount > 0)
                        top3 + listOf(ExpenseCategory.OTHER to othersAmount)
                    else
                        top3

                var startX = margin
                var usedPercent = 0

                categorySummary.forEachIndexed { index, (cat, amt) ->

                    val percent =
                        if (index == categorySummary.lastIndex)
                            100 - usedPercent
                        else {
                            val p = ((amt / totalAmount) * 100).toInt()
                            usedPercent += p
                            p
                        }

                    val name =
                        cat.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }

                    canvas.drawText(name, startX, yPos, labelPaint)
                    canvas.drawText("$percent%", startX, yPos + 18, valuePaint)
                    canvas.drawText(
                        "$currency ${String.format("%,.0f", amt)}",
                        startX,
                        yPos + 34,
                        labelPaint
                    )

                    startX += 140f
                }

                yPos += 70
            }


            val colDate = margin + 10
            val colDesc = margin + 80
            val colCat = margin + 250
            val colMode = margin + 380
            val colAmt = pageWidth - margin - 10

            fun drawHeader() {
                bgPaint.color = COLOR_PRIMARY
                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 30, bgPaint)

                canvas.drawText("DATE", colDate, yPos + 20, tableHeaderPaint)
                canvas.drawText("DESCRIPTION", colDesc, yPos + 20, tableHeaderPaint)
                canvas.drawText("CATEGORY", colCat, yPos + 20, tableHeaderPaint)
                canvas.drawText("MODE", colMode, yPos + 20, tableHeaderPaint)

                tableHeaderPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("AMOUNT", colAmt, yPos + 20, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT

                yPos += 30
            }

            drawHeader()

            expenses.forEachIndexed { index, expense ->

                if (yPos > pageHeight - 80) {

                    drawWatermark(canvas, pageWidth, pageHeight, watermarkPaint, isPremium)

                    canvas.drawText(
                        "Xpenso Finance Report • Page $pageNumber",
                        pageWidth / 2f,
                        pageHeight - 30f,
                        footerPaint
                    )

                    document.finishPage(page)

                    pageNumber++

                    page = document.startPage(pageInfo)
                    canvas = page.canvas

                    yPos = 120f

                    drawHeader()
                }

                if (index % 2 != 0) {
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 24, bgPaint)
                }

                val dateStr = tableDate.format(Date(expense.timestamp))

                val title =
                    if (expense.title.length > 22)
                        expense.title.take(19) + "..."
                    else
                        expense.title

                val category =
                    ExpenseCategory.Companion.detect(expense.title)
                        .name.replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                val mode = expense.paymentMode.ifBlank { "-" }

                canvas.drawText(dateStr, colDate, yPos + 15, rowPaint)
                canvas.drawText(title, colDesc, yPos + 15, rowPaint)
                canvas.drawText(category, colCat, yPos + 15, rowPaint)
                canvas.drawText(mode, colMode, yPos + 15, rowPaint)

                canvas.drawText(
                    "$currency ${String.format("%,.0f", expense.amount)}",
                    colAmt,
                    yPos + 15,
                    amountPaint
                )

                yPos += 24
            }

            drawWatermark(canvas, pageWidth, pageHeight, watermarkPaint, isPremium)
            canvas.drawText(
                "Xpenso Finance Report • Page $pageNumber",
                pageWidth / 2f,
                pageHeight - 30f,
                footerPaint
            )

            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use {
                document.writeTo(it)
            }

            document.close()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}