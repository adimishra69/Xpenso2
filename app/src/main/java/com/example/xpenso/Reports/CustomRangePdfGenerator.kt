package com.example.xpenso.Reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.TextPaint
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.groupBy
import kotlin.collections.mapValues
import kotlin.collections.sortedByDescending
import kotlin.collections.sumOf
import kotlin.collections.take
import kotlin.collections.toList
import kotlin.io.use
import kotlin.text.ifBlank
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.replaceFirstChar
import kotlin.text.take
import kotlin.text.toInt
import kotlin.text.uppercase

object CustomRangePdfGenerator {

        // Removed 'const' because toInt() or parseColor are functions
        private val COLOR_PRIMARY = Color.parseColor("#2196F3")   // Modern Blue
        private val COLOR_TEXT_MAIN = Color.parseColor("#212121") // Dark Grey/Black
        private val COLOR_TEXT_SEC = Color.parseColor("#757575")  // Grey
        private val COLOR_BG_LIGHT = Color.parseColor("#F5F5F5") // Very Light Grey

        // ... rest of your code


    fun generateCustomRangeReport(
        context: Context,
        uri: Uri,
        allExpenses: List<ExpenseEntity>,
        startDate: Long,
        endDate: Long,
        currencySymbol: String
    ): Result<Unit> {

        val expenses = allExpenses.filter { it.timestamp in startDate..endDate }
            .sortedByDescending { it.timestamp }

        if (expenses.isEmpty()) return Result.failure(kotlin.Exception("No data for the selected range"))

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

            // --- PAINTS ---
            val brandPaint = TextPaint().apply {
                textSize = 28f
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
                textSize = 70f
                color = Color.LTGRAY
                alpha = 25
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val totalAmount = expenses.sumOf { it.amount }
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val periodText = "${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}"

            // --- HEADER BANNER ---
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, bgPaint)
            canvas.drawText("XPENSO CUSTOM REPORT", margin, 60f, brandPaint)
            labelPaint.color = Color.WHITE
            canvas.drawText("Statement for Custom Period", margin, 85f, labelPaint)
            val today = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", pageWidth - margin, 85f, labelPaint)
            labelPaint.textAlign = Paint.Align.LEFT
            labelPaint.color = COLOR_TEXT_SEC

            yPos = 150f

            // --- SUMMARY SECTION ---
            canvas.drawText("REPORT SUMMARY", margin, yPos, titlePaint)
            yPos += 30
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(margin, yPos - 20, pageWidth - margin, yPos + 60), 12f, 12f, bgPaint)
            canvas.drawText("Custom Date Range", margin + 20, yPos, labelPaint)
            canvas.drawText(periodText, margin + 20, yPos + 25, valuePaint)
            canvas.drawText("Total Spent", pageWidth / 2f, yPos, labelPaint)
            canvas.drawText("$currencySymbol ${CurrencyFormatter.format(totalAmount, currencySymbol)}", pageWidth / 2f, yPos + 25, valuePaint)
            canvas.drawText("Total Items", pageWidth - margin - 100, yPos, labelPaint)
            canvas.drawText("${expenses.size} Entries", pageWidth - margin - 100, yPos + 25, valuePaint)

            yPos += 100

            // --- CATEGORY SPENDING ---
            canvas.drawText("SPENDING BY CATEGORY", margin, yPos, titlePaint)
            yPos += 25
            val categorySummary = expenses.groupBy { ExpenseCategory.detect(it.title) }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .toList().sortedByDescending { it.second }.take(4)

            var startX = margin
            categorySummary.forEach { (cat, amt) ->
                val percent = if(totalAmount > 0) (amt / totalAmount * 100).toInt() else 0
                canvas.drawText(cat.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, startX, yPos, labelPaint)
                canvas.drawText("$percent%", startX, yPos + 18, valuePaint)
                startX += 130f
            }

            yPos += 60

            // --- TRANSACTION TABLE ---
            val colDate = margin + 10
            val colDesc = margin + 90
            val colCat = margin + 240
            val colMode = margin + 360
            val colAmt = pageWidth - margin - 10

            fun drawTableHeader(y: Float) {
                bgPaint.color = COLOR_PRIMARY
                canvas.drawRect(margin, y, pageWidth - margin, y + 25, bgPaint)
                tableHeaderPaint.color = Color.WHITE
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
                    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 24, bgPaint)
                }

                val dateStr = tableDate.format(Date(expense.timestamp))
                val title = if (expense.title.length > 28) expense.title.take(25) + "..." else expense.title
                val category = ExpenseCategory.detect(expense.title).name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                val mode = expense.paymentMode.ifBlank { "-" }

                canvas.drawText(dateStr, colDate, yPos + 15, rowPaint)
                canvas.drawText(title, colDesc, yPos + 15, rowPaint)
                canvas.drawText(category, colCat, yPos + 15, rowPaint)
                canvas.drawText(mode, colMode, yPos + 15, rowPaint)
                canvas.drawText("$currencySymbol ${CurrencyFormatter.format(expense.amount, currencySymbol)}", colAmt, yPos + 15, amountPaint)
                yPos += 24
            }

//            drawWatermark(canvas, pageWidth, pageHeight, watermarkPaint)
            drawFooter(canvas, pageNumber, pageWidth, pageHeight, footerPaint)
            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            document.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawFooter(canvas: Canvas, pageNum: Int, width: Int, height: Int, paint: Paint) {
        canvas.drawText("Custom Date Range Report • Page $pageNum • Generated via Xpenso", width / 2f, height - 30f, paint)
    }

//    private fun drawWatermark(canvas: Canvas, pageWidth: Int, pageHeight: Int, paint: Paint) {
//        canvas.save()
//        canvas.rotate(-30f, pageWidth / 2f, pageHeight / 2f)
//        canvas.drawText("XPENSO PREMIUM", pageWidth / 2f, pageHeight / 2f, paint)
//        canvas.restore()
//    }
}