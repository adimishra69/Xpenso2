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

object MonthlyPdfReportGenerator {

    private val COLOR_PRIMARY   = Color.parseColor("#3949AB") // Indigo Primary (Premium Header / Bars)

    private val COLOR_TEXT_MAIN = Color.parseColor("#212121") // Main Text (Keep Same)

    private val COLOR_TEXT_SEC  = Color.parseColor("#757575") // Secondary Text (Keep Same)

    private val COLOR_BG_LIGHT  = Color.parseColor("#F8F9FA") // Light Background (Keep Same)

    // ── layout constants ─────────────────────────────────────────────────────
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f
    private val CONTENT_W get() = PAGE_W - MARGIN * 2  // 515

    // ── table column X positions ─────────────────────────────────────────────
    private val COL_DATE = MARGIN + 10f
    private val COL_DESC = MARGIN + 105f
    private val COL_CAT  = MARGIN + 255f
    private val COL_MODE = MARGIN + 375f
    private val COL_AMT  = PAGE_W - MARGIN - 8f

    // ────────────────────────────────────────────────────────────────────────
    fun generateMonthlyCycleReport(
        context: Context,
        uri: Uri,
        allExpenses: List<ExpenseEntity>,
        month: Int,
        year: Int,
        startOfMonth: Int,
        currencySymbol: String,
        isPremium: Boolean
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

        if (expenses.isEmpty()) return Result.failure(Exception("No data  cycle"))

        return try {

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()

            // ── paints ───────────────────────────────────────────────────────
            val brandPaint = TextPaint().apply {
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color    = Color.WHITE
            }
            val titlePaint = TextPaint().apply {
                textSize     = 13f
                typeface     = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color        = COLOR_TEXT_MAIN
                letterSpacing= 0.08f
            }
            val labelPaint = TextPaint().apply {
                textSize = 9f
                color    = COLOR_TEXT_SEC
            }
            val valuePaint = TextPaint().apply {
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color    = COLOR_PRIMARY
            }
            val tableHeaderPaint = TextPaint().apply {
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color    = Color.WHITE
            }
            val rowPaint = TextPaint().apply {
                textSize = 8.5f
                color    = COLOR_TEXT_MAIN
            }
            val rowSecPaint = TextPaint().apply {
                textSize = 8.5f
                color    = COLOR_TEXT_SEC
            }
            val amountPaint = TextPaint(rowPaint).apply {
                textAlign = Paint.Align.RIGHT
                typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color     = COLOR_PRIMARY
            }
            val footerPaint = TextPaint().apply {
                textSize  = 8f
                textAlign = Paint.Align.CENTER
                color     = COLOR_TEXT_SEC
            }
            val watermarkPaint = Paint().apply {
                textSize  = 75f
                color     = Color.LTGRAY
                alpha     = 35
                textAlign = Paint.Align.CENTER
            }
            val dividerPaint = Paint().apply {
                color       = Color.parseColor("#E0E0E0")
                strokeWidth = 0.7f
            }
            val bgPaint = Paint()

            // ── aggregates ───────────────────────────────────────────────────
            val currency       = currencySymbol
            val totalAmount    = expenses.sumOf { it.amount }
            val avgExpense     = totalAmount / expenses.size
            val highestExpense = expenses.maxByOrNull { it.amount }

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val tableDate  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val periodText = "${dateFormat.format(startCal.time)} – ${dateFormat.format(endCal.time)}"
            val today      = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            val modeMap = expenses.groupBy {
                when {
                    it.paymentMode.contains("Card", true) -> "Card"
                    it.paymentMode.isBlank()               -> "Other"
                    else                                   -> it.paymentMode
                }
            }.mapValues { it.value.size }
            val maxMode      = modeMap.maxByOrNull { it.value }
            val modePercent  = ((maxMode?.value ?: 0) * 100 / expenses.size)
            val mostUsedMode = "${maxMode?.key ?: "-"} ($modePercent%)"

            // ── state vars ───────────────────────────────────────────────────
            var pageNumber = 1
            var page       = document.startPage(pageInfo)
            var canvas     = page.canvas
            var yPos       = 0f

            // ── helpers ──────────────────────────────────────────────────────
            fun drawFooterLine() {
                canvas.drawLine(MARGIN, PAGE_H - 44f, PAGE_W - MARGIN, PAGE_H - 44f, dividerPaint)
                canvas.drawText(
                    "Xpenso  •  Monthly Financial Report  •  Page $pageNumber",
                    PAGE_W / 2f, PAGE_H - 30f, footerPaint
                )
            }

            fun newPage() {
                drawWatermark(canvas, PAGE_W, PAGE_H, watermarkPaint, isPremium)
                drawFooterLine()
                document.finishPage(page)
                pageNumber++
                page   = document.startPage(pageInfo)
                canvas = page.canvas
            }

            fun sectionTitle(text: String, y: Float): Float {
                canvas.drawText(text, MARGIN, y, titlePaint)
                canvas.drawLine(MARGIN, y + 4f, PAGE_W - MARGIN, y + 4f, dividerPaint)
                return y + 20f
            }

            fun drawTableHeader(y: Float): Float {
                bgPaint.color = COLOR_PRIMARY
                canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + 26f, bgPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT
                canvas.drawText("DATE",        COL_DATE, y + 17f, tableHeaderPaint)
                canvas.drawText("DESCRIPTION", COL_DESC, y + 17f, tableHeaderPaint)
                canvas.drawText("CATEGORY",    COL_CAT,  y + 17f, tableHeaderPaint)
                canvas.drawText("MODE",        COL_MODE, y + 17f, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("AMOUNT",      COL_AMT,  y + 17f, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT
                return y + 26f
            }

            // ── PAGE 1: HEADER ───────────────────────────────────────────────
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 110f, bgPaint)

            canvas.drawText("XPENSO", MARGIN, 60f, brandPaint)

            labelPaint.color     = Color.WHITE
            labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(
                if (isPremium) "Premium Monthly Statement" else "Basic Monthly Report",
                MARGIN, 85f, labelPaint
            )
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", PAGE_W - MARGIN, 85f, labelPaint)

            labelPaint.textAlign = Paint.Align.LEFT
            labelPaint.color     = COLOR_TEXT_SEC
            canvas.drawText("Period: $periodText", MARGIN, 125f, labelPaint)

            yPos = 148f

            // ── CYCLE SUMMARY ────────────────────────────────────────────────
            yPos = sectionTitle("CYCLE SUMMARY", yPos)
            yPos += 6f

            val summaryH = 116f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + summaryH), 10f, 10f, bgPaint)

            val col1 = MARGIN + 18f
            val col2 = MARGIN + 185f
            val col3 = MARGIN + 360f
            val row1L = yPos + 20f; val row1V = yPos + 36f
            val row2L = yPos + 62f; val row2V = yPos + 78f

            // row 1
            canvas.drawText("Cycle Period",   col1, row1L, labelPaint)
            canvas.drawText(periodText,        col1, row1V, valuePaint)
            canvas.drawText("Total Spent",     col2, row1L, labelPaint)
            canvas.drawText("$currency ${fmt(totalAmount)}", col2, row1V, valuePaint)
            canvas.drawText("Transactions",    col3, row1L, labelPaint)
            canvas.drawText("${expenses.size} Entries", col3, row1V, valuePaint)
            // row 2
            canvas.drawText("Average Expense", col1, row2L, labelPaint)
            canvas.drawText("$currency ${fmt(avgExpense)}", col1, row2V, valuePaint)
            canvas.drawText("Largest Expense", col2, row2L, labelPaint)
            canvas.drawText("$currency ${fmt(highestExpense?.amount ?: 0.0)}", col2, row2V, valuePaint)
            canvas.drawText("Most Used Mode",  col3, row2L, labelPaint)
            canvas.drawText(mostUsedMode,       col3, row2V, valuePaint)

            yPos += summaryH + 22f

            // ── CATEGORY BREAKDOWN (premium only) ────────────────────────────
            if (isPremium) {

                // all categories sorted descending, no merging
                val categorySummary = expenses
                    .groupBy {
                        ExpenseCategory.entries
                            .find { cat -> cat.name == it.category }
                            ?: ExpenseCategory.OTHER
                    }
                    .mapValues { it.value.sumOf { e -> e.amount } }
                    .toList()
                    .sortedByDescending { it.second }

                val catBlockH = (categorySummary.size * 26f) + 20f

                // push to new page if not enough room
                if (yPos + catBlockH + 40f > PAGE_H - 60f) {
                    newPage(); yPos = MARGIN
                }

                yPos = sectionTitle("CATEGORY BREAKDOWN", yPos)
                yPos += 6f

                bgPaint.color = COLOR_BG_LIGHT
                canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + catBlockH), 10f, 10f, bgPaint)

                val barStart = MARGIN + 155f
                val barMaxW  = CONTENT_W - 155f - 60f
                var catY     = yPos + 18f

                categorySummary.forEach { (cat, amt) ->
                    val percent    = amt / totalAmount * 100
                    val percentStr = if (percent < 1.0 && percent > 0.0)
                        String.format("%.2f%%", percent)
                    else
                        "${percent.toInt()}%"

                    val barW = (percent / 100.0 * barMaxW).toFloat()
                        .coerceAtLeast(if (percent > 0.0) 2f else 0f)

                    val name = cat.name.replace("_", " ")
                        .lowercase().replaceFirstChar { it.uppercase() }

                    canvas.drawText(name, MARGIN + 14f, catY, labelPaint)

                    // track background
                    bgPaint.color = Color.parseColor("#E3F2FD")
                    canvas.drawRoundRect(RectF(barStart, catY - 7f, barStart + barMaxW, catY + 4f), 3f, 3f, bgPaint)
                    // filled bar
                    if (barW > 0f) {
                        bgPaint.color = COLOR_PRIMARY
                        canvas.drawRoundRect(RectF(barStart, catY - 7f, barStart + barW, catY + 4f), 3f, 3f, bgPaint)
                    }

                    canvas.drawText(percentStr, barStart + barMaxW + 8f, catY, valuePaint)
                    catY += 26f
                }

                yPos += catBlockH + 22f
            }

            // ── TRANSACTIONS TABLE ───────────────────────────────────────────
            if (yPos + 60f > PAGE_H - 60f) { newPage(); yPos = MARGIN }

            yPos = sectionTitle("ALL TRANSACTIONS  (${expenses.size} records)", yPos)
            yPos += 4f
            yPos = drawTableHeader(yPos)

            expenses.forEachIndexed { index, expense ->

                if (yPos > PAGE_H - 80f) {
                    newPage()
                    yPos = MARGIN
                    yPos = sectionTitle("ALL TRANSACTIONS (continued)", yPos)
                    yPos += 4f
                    yPos = drawTableHeader(yPos)
                }

                // alternating row bg
                if (index % 2 != 0) {
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRect(MARGIN, yPos, PAGE_W - MARGIN, yPos + 24f, bgPaint)
                }

                val dateStr  = tableDate.format(Date(expense.timestamp))
                val title    = if (expense.title.length > 18) expense.title.take(15) + "…" else expense.title
                val category = (
                        ExpenseCategory.entries
                            .find { cat -> cat.name == expense.category }
                            ?: ExpenseCategory.OTHER
                        ).name
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
                val mode     = when {
                    expense.paymentMode.isBlank()                  -> "-"
                    expense.paymentMode.contains("Card", true)     -> "Card"
                    else                                           -> expense.paymentMode
                }

                canvas.drawText(dateStr,  COL_DATE, yPos + 15f, rowSecPaint)
                canvas.drawText(title,    COL_DESC, yPos + 15f, rowPaint)
                canvas.drawText(category, COL_CAT,  yPos + 15f, rowSecPaint)
                canvas.drawText(mode,     COL_MODE, yPos + 15f, rowSecPaint)
                canvas.drawText(
                    "$currency ${fmt(expense.amount)}",
                    COL_AMT, yPos + 15f, amountPaint
                )

                // row divider
                canvas.drawLine(MARGIN, yPos + 24f, PAGE_W - MARGIN, yPos + 24f, dividerPaint)

                yPos += 24f
            }

            // ── grand total row ───────────────────────────────────────────────
            if (yPos > PAGE_H - 50f) { newPage(); yPos = MARGIN }
            yPos += 4f
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + 26f), 6f, 6f, bgPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("TOTAL", COL_DESC, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$currency ${fmt(totalAmount)}", COL_AMT, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT

            // ── finalize last page ────────────────────────────────────────────
            drawWatermark(canvas, PAGE_W, PAGE_H, watermarkPaint, isPremium)
            drawFooterLine()
            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            document.close()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fmt(value: Double) = String.format("%,.0f", value)
}