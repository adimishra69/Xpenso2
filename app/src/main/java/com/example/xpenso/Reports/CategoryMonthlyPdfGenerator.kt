package com.example.xpenso.Reports

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.sortedByDescending

object CategoryPdfReportGenerator {

    private const val COLOR_TEXT_MAIN = 0xFF202124.toInt()
    private const val COLOR_TEXT_SEC = 0xFF5F6368.toInt()
    private const val COLOR_BG_LIGHT = 0xFFF8F9FA.toInt()

    /**
     * Generates a premium PDF report for a specific category.
     * Features professional branding, summary analytics, and a detailed transaction ledger.
     */
    fun generateCategoryReport(
        context: Context,
        uri: Uri,
        expenses: List<ExpenseEntity>,
        category: ExpenseCategory,
        currencySymbol: String
    ): Result<Unit> {

        if (expenses.isEmpty()) {
            return Result.failure(Exception("No data for this category"))
        }

        return try {
            val document = PdfDocument()
            val pageWidth = 595 // A4 width
            val pageHeight = 842 // A4 height
            val margin = 40f
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

            var pageNumber = 1
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPos = 0f

            val categoryColor = category.color.toArgb()

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
                color = categoryColor
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

            val watermarkPaint = Paint().apply {
                textSize = 70f
                color = categoryColor
                alpha = 15
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val bgPaint = Paint().apply { style = Paint.Style.FILL }

            // --- HEADER BANNER ---
            bgPaint.color = categoryColor
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, bgPaint)
            
            canvas.drawText("XPENSO", margin, 60f, brandPaint)
            
            labelPaint.color = Color.WHITE
            val categoryDisplayName = category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            canvas.drawText("$categoryDisplayName Spending Report", margin, 85f, labelPaint)
            
            val today = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", pageWidth - margin, 85f, labelPaint)
            labelPaint.textAlign = Paint.Align.LEFT
            labelPaint.color = COLOR_TEXT_SEC

            yPos = 150f

            // --- SUMMARY SECTION ---
            canvas.drawText("CATEGORY SUMMARY", margin, yPos, titlePaint)
            yPos += 30
            
            val totalAmount = expenses.sumOf { it.amount }
            
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(margin, yPos - 20, pageWidth - margin, yPos + 50), 12f, 12f, bgPaint)
            
            canvas.drawText("Total Expenditure ($categoryDisplayName)", margin + 20, yPos + 5, labelPaint)
            canvas.drawText("$currencySymbol ${CurrencyFormatter.format(totalAmount, currencySymbol)}", margin + 20, yPos + 25, valuePaint)

            canvas.drawText("Transactions", pageWidth / 2f + 50, yPos + 5, labelPaint)
            canvas.drawText("${expenses.size} Items", pageWidth / 2f + 50, yPos + 25, valuePaint)
            
            yPos += 90

            // --- TRANSACTION TABLE ---
            val colDate = margin + 10
            val colDesc = margin + 90
            val colMode = margin + 350
            val colAmt = pageWidth - margin - 10

            fun drawTableHeader(y: Float) {
                bgPaint.color = categoryColor
                canvas.drawRect(margin, y, pageWidth - margin, y + 25, bgPaint)
                
                tableHeaderPaint.color = Color.WHITE
                tableHeaderPaint.textAlign = Paint.Align.LEFT
                canvas.drawText("DATE", colDate, y + 16, tableHeaderPaint)
                canvas.drawText("DESCRIPTION", colDesc, y + 16, tableHeaderPaint)
                canvas.drawText("MODE", colMode, y + 16, tableHeaderPaint)
                
                tableHeaderPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("AMOUNT", colAmt, y + 16, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT
            }

            drawTableHeader(yPos)
            yPos += 25
            val tableDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

            expenses.sortedByDescending { it.timestamp }.forEachIndexed { index, expense ->
                // Check for new page
                if (yPos > pageHeight - 80) {
//                    drawWatermark(canvas, pageWidth, pageHeight, watermarkPaint, categoryDisplayName)
                    drawFooter(canvas, pageNumber, pageWidth, pageHeight, footerPaint)
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = margin
                    drawTableHeader(yPos)
                    yPos += 25
                }

                // Zebra striping
                if (index % 2 != 0) {
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 22, bgPaint)
                }

                val dateStr = tableDateFormat.format(Date(expense.timestamp))
                val mode = if(expense.paymentMode.isBlank()) "-" else expense.paymentMode
                val displayTitle = if(expense.title.length > 45) expense.title.take(42) + "..." else expense.title

                canvas.drawText(dateStr, colDate, yPos + 14, rowPaint)
                canvas.drawText(displayTitle, colDesc, yPos + 14, rowPaint)
                canvas.drawText(mode, colMode, yPos + 14, rowPaint)
                canvas.drawText("$currencySymbol ${CurrencyFormatter.format(expense.amount, currencySymbol)}", colAmt, yPos + 14, amountPaint)

                yPos += 22
            }

            // Final components for the last page
//            drawWatermark(canvas, pageWidth, pageHeight, watermarkPaint, categoryDisplayName)
            drawFooter(canvas, pageNumber, pageWidth, pageHeight, footerPaint)
            document.finishPage(page)

            // Write to URI
            context.contentResolver.openOutputStream(uri)?.use {
                document.writeTo(it)
            }
            document.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawFooter(canvas: Canvas, pageNum: Int, width: Int, height: Int, paint: Paint) {
        val y = height - 30f
        canvas.drawText( "Xpenso • Personal Finance Report • Page $pageNum", width / 2f, y, paint)
    }

//    private fun drawWatermark(canvas: Canvas, width: Int, height: Int, paint: Paint, text: String) {
//        canvas.save()
//        canvas.rotate(-30f, width / 2f, height / 2f)
//        canvas.drawText(text.uppercase(), width / 2f, height / 2f, paint)
//        canvas.restore()
//    }
}
