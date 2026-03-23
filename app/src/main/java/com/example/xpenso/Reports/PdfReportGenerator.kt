package com.example.xpenso.Reports

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.TextPaint
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    private const val COLOR_PRIMARY = 0xFF1A73E8.toInt()
    private const val COLOR_TEXT_MAIN = 0xFF202124.toInt()
    private const val COLOR_TEXT_SEC = 0xFF5F6368.toInt()
    private const val COLOR_BG_LIGHT = 0xFFF8F9FA.toInt()

    fun generateExpenseReport(
        context: Context,
        uri: Uri,
        expenses: List<ExpenseEntity>,
        currencySymbol: String,
        isPremium: Boolean
    ): Result<Unit> {

        if (expenses.isEmpty()) return Result.failure(Exception("No data to export"))

        return try {

            val document = PdfDocument()

            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

            val watermarkPaint = Paint().apply {
                textSize = 75f
                color = Color.LTGRAY
                alpha = 35
                textAlign = Paint.Align.CENTER
            }

            var pageNumber = 1
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            var yPos = 0f

            val brandPaint = TextPaint().apply {
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.WHITE
                letterSpacing = 0.1f
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

            val progressPaint = Paint().apply {
                color = COLOR_PRIMARY
            }

            val currency = currencySymbol

            val totalAmount = expenses.sumOf { it.amount }
            val avgExpense = totalAmount / expenses.size
            val highestExpense = expenses.maxByOrNull { it.amount }

            val minDate = expenses.minOf { it.timestamp }
            val maxDate = expenses.maxOf { it.timestamp }

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val periodText =
                "${dateFormat.format(Date(minDate))} - ${dateFormat.format(Date(maxDate))}"

            bgPaint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 100f, bgPaint)

            canvas.drawText("XPENSO", margin, 60f, brandPaint)

            labelPaint.color = Color.WHITE
            val subtitle =
                if (isPremium) "Premium Financial Statement"
                else "Basic Expense Report"

            canvas.drawText(subtitle, margin, 80f, labelPaint)

            val today = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(Date())

            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", pageWidth - margin, 80f, labelPaint)

            labelPaint.textAlign = Paint.Align.LEFT
            labelPaint.color = COLOR_TEXT_SEC

            canvas.drawText("Period: $periodText", margin, 115f, labelPaint)

            yPos = 150f

            canvas.drawText("REPORT SUMMARY", margin, yPos, titlePaint)
            yPos += 30

            bgPaint.color = COLOR_BG_LIGHT

            canvas.drawRoundRect(
                RectF(margin, yPos - 20, pageWidth - margin, yPos + 60),
                12f,
                12f,
                bgPaint
            )

            canvas.drawText("Total Expenses", margin + 20, yPos, labelPaint)
            canvas.drawText(
                "$currency ${String.format("%,.0f", totalAmount)}",
                margin + 20,
                yPos + 20,
                valuePaint
            )
            canvas.drawText("Transactions", margin + 200, yPos, labelPaint)
            canvas.drawText("${expenses.size}", margin + 200, yPos + 20, valuePaint)

            canvas.drawText("Average Expense", margin + 350, yPos, labelPaint)
            canvas.drawText(
                "$currency ${String.format("%,.0f", avgExpense)}",
                margin + 350,
                yPos + 20,
                valuePaint
            )

            highestExpense?.let {
                canvas.drawText("Highest Expense", margin + 350, yPos + 35, labelPaint)
                canvas.drawText(
                    "$currency ${String.format("%,.0f", it.amount)}",
                    margin + 350,
                    yPos + 55,
                    valuePaint
                )
            }

            yPos += 90

            if (isPremium) {

                canvas.drawText("CATEGORY SPENDING", margin, yPos, titlePaint)
                yPos += 20

                val categorySummary = expenses
                    .groupBy { ExpenseCategory.detect(it.title) }
                    .mapValues { it.value.sumOf { e -> e.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(4)

                var startX = margin

                categorySummary.forEach { (cat, amt) ->

                    val percent = (amt / totalAmount * 100).toInt()

                    val name = cat.name
                        .replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                    canvas.drawText(name, startX, yPos, labelPaint)

                    val barWidth = percent * 1.2f

                    canvas.drawRect(
                        startX,
                        yPos + 5,
                        startX + barWidth,
                        yPos + 12,
                        progressPaint
                    )

                    canvas.drawText("$percent%", startX + barWidth + 6, yPos + 12, labelPaint)

                    startX += 130f
                }

                yPos += 40
            }

            val colDate = margin + 10
            val colDesc = margin + 80
            val colCat = margin + 240
            val colMode = margin + 360
            val colAmt = pageWidth - margin - 10

            fun drawTableHeader(y: Float) {

                bgPaint.color = COLOR_PRIMARY
                canvas.drawRect(margin, y, pageWidth - margin, y + 25, bgPaint)

                tableHeaderPaint.textAlign = Paint.Align.LEFT
                canvas.drawText("DATE", colDate, y + 16, tableHeaderPaint)
                canvas.drawText("DESCRIPTION", colDesc, y + 16, tableHeaderPaint)
                canvas.drawText("CATEGORY", colCat, y + 16, tableHeaderPaint)
                canvas.drawText("MODE", colMode, y + 16, tableHeaderPaint)

                tableHeaderPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("AMOUNT", colAmt, y + 16, tableHeaderPaint)
            }

            drawTableHeader(yPos)

            yPos += 25

            val tableDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

            expenses.forEachIndexed { index, expense ->

                if (yPos > pageHeight - 80) {

                    drawWatermark(canvas, pageWidth, pageHeight, watermarkPaint , isPremium)

                    drawFooter(canvas, pageNumber, pageWidth, pageHeight, footerPaint)

                    document.finishPage(page)

                    pageNumber++

                    page = document.startPage(pageInfo)
                    canvas = page.canvas

                    yPos = margin

                    drawTableHeader(yPos)

                    yPos += 25
                }

                if (index % 2 != 0) {

                    bgPaint.color = COLOR_BG_LIGHT

                    canvas.drawRect(
                        margin,
                        yPos,
                        pageWidth - margin,
                        yPos + 24,
                        bgPaint
                    )
                }

                val dateStr = tableDate.format(Date(expense.timestamp))

                val category = ExpenseCategory.detect(expense.title)
                    .name
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }

                val mode = if (expense.paymentMode.isBlank()) "-" else expense.paymentMode

                val title =
                    if (expense.title.length > 22)
                        expense.title.take(19) + "..."
                    else
                        expense.title

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

            drawWatermark(canvas, pageWidth, pageHeight, watermarkPaint , isPremium)

            drawFooter(canvas, pageNumber, pageWidth, pageHeight, footerPaint)

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

    private fun drawFooter(
        canvas: Canvas,
        pageNum: Int,
        width: Int,
        height: Int,
        paint: Paint
    ) {

        canvas.drawText(
            "Xpenso • Personal Finance Report • Page $pageNum",
            width / 2f,
            height - 30f,
            paint
        )
    }
}

 fun drawWatermark(
    canvas: Canvas,
    pageWidth: Int,
    pageHeight: Int,
    paint: Paint,
    isPremium:Boolean
) {
    if (isPremium) return
    canvas.save()

    canvas.rotate(-30f, pageWidth / 2f, pageHeight / 2f)

    canvas.drawText(
        "XPENSO",
        pageWidth / 2f,
        pageHeight / 2f,
        paint
    )

    canvas.restore()
}