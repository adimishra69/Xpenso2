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
import androidx.compose.ui.graphics.toArgb
import com.example.xpenso.Database.ExpenseEntity
import com.example.xpenso.Transaction.ExpenseCategory
import com.example.xpenso.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.io.use
import kotlin.text.ifBlank
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.replaceFirstChar

object CustomRangePdfGenerator {

    // ─── Color Palette — matches PdfReportGenerator exactly ─────────────────
    private val COLOR_PRIMARY   = 0xFF3949AB.toInt()  // Indigo Primary
    private val COLOR_TEXT_MAIN = 0xFF202124.toInt()  // Main Text
    private val COLOR_TEXT_SEC  = 0xFF5F6368.toInt()  // Secondary Text
    private val COLOR_BG_LIGHT  = 0xFFF8F9FA.toInt()  // Light Background

    // ─── Page / layout constants ─────────────────────────────────────────────
    private const val PAGE_W  = 595
    private const val PAGE_H  = 842
    private const val MARGIN  = 40f

    // ─── Table column X positions — same as PdfReportGenerator ──────────────
    private val COL_DATE = MARGIN + 10f
    private val COL_DESC = MARGIN + 95f
    private val COL_CAT  = MARGIN + 244f
    private val COL_MODE = MARGIN + 364f
    private val COL_AMT  = PAGE_W - MARGIN - 8f

    fun generateCustomRangeReport(
        context: Context,
        uri: Uri,
        allExpenses: List<ExpenseEntity>,
        startDate: Long,
        endDate: Long,
        currencySymbol: String
    ): Result<Unit> {

        // ── 1. Normalize date range ───────────────────────────────────────────
        val adjustedStartDate = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val adjustedEndDate = Calendar.getInstance().apply {
            timeInMillis = endDate
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59);       set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // ── 2. Filter & sort ──────────────────────────────────────────────────
        val expenses = allExpenses
            .filter { it.timestamp in adjustedStartDate..adjustedEndDate }
            .sortedByDescending { it.timestamp }

        if (expenses.isEmpty()) return Result.failure(Exception("No data for the selected range"))

        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()

            // ── Paints — same style as PdfReportGenerator ────────────────────
            val brandPaint = TextPaint().apply {
                textSize = 28f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.WHITE; letterSpacing = 0.1f
            }
            val titlePaint = TextPaint().apply {
                textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = COLOR_TEXT_MAIN; letterSpacing = 0.08f
            }
            val labelPaint = TextPaint().apply {
                textSize = 9f; color = COLOR_TEXT_SEC
            }
            val valuePaint = TextPaint().apply {
                textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = COLOR_PRIMARY
            }
            val tableHeaderPaint = TextPaint().apply {
                textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.WHITE
            }
            val rowPaint = TextPaint().apply {
                textSize = 8.5f; color = COLOR_TEXT_MAIN
            }
            val rowSecPaint = TextPaint().apply {
                textSize = 8.5f; color = COLOR_TEXT_SEC
            }
            val amountPaint = TextPaint(rowPaint).apply {
                textAlign = Paint.Align.RIGHT
                typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color     = COLOR_PRIMARY
            }
            val footerPaint = TextPaint().apply {
                textSize = 8f; textAlign = Paint.Align.CENTER; color = COLOR_TEXT_SEC
            }
            val dividerPaint = Paint().apply {
                color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f
            }
            val bgPaint = Paint()

            // ── Aggregates ───────────────────────────────────────────────────
            val totalAmount    = expenses.sumOf { it.amount }
            val avgExpense     = if (expenses.isNotEmpty()) totalAmount / expenses.size else 0.0
            val highestExpense = expenses.maxByOrNull { it.amount }
            val lowestExpense  = expenses.minByOrNull { it.amount }

            val modeMap = expenses.groupBy {
                when {
                    it.paymentMode.contains("Card", true) -> "Card"
                    it.paymentMode.isBlank()              -> "Unknown"
                    else                                  -> it.paymentMode
                }
            }.mapValues { it.value.size }
            val maxMode      = modeMap.maxByOrNull { it.value }
            val modePercent  = if (expenses.isNotEmpty()) ((maxMode?.value ?: 0) * 100 / expenses.size) else 0
            val mostUsedMode = "${maxMode?.key ?: "-"} ($modePercent%)"

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val periodText = "${dateFormat.format(Date(adjustedStartDate))} – ${dateFormat.format(Date(adjustedEndDate))}"
            val today      = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            // ── All categories with data — no merging, no 1% rule ────────────
            val categorySummary: List<Triple<ExpenseCategory, String, Double>> = expenses
                .groupBy {
                    ExpenseCategory.entries
                        .find { cat -> cat.name == it.category }
                        ?: ExpenseCategory.OTHER
                }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .toList()
                .sortedByDescending { it.second }
                .map { (cat, amt) -> Triple(cat, cat.name.formatCategoryName(), amt) }

            // ── State vars ───────────────────────────────────────────────────
            var pageNumber = 1
            var page       = document.startPage(pageInfo)
            var canvas     = page.canvas

            // ── Helpers ──────────────────────────────────────────────────────
            fun newPage() {
                drawFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
                document.finishPage(page)
                pageNumber++
                page   = document.startPage(pageInfo)
                canvas = page.canvas
            }

            fun sectionTitle(c: Canvas, text: String, y: Float): Float {
                c.drawText(text, MARGIN, y, titlePaint)
                c.drawLine(MARGIN, y + 4f, PAGE_W - MARGIN, y + 4f, dividerPaint)
                return y + 20f
            }

            fun drawTableHeader(c: Canvas, y: Float): Float {
                bgPaint.color = COLOR_PRIMARY
                c.drawRect(MARGIN, y, PAGE_W - MARGIN, y + 26f, bgPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT
                c.drawText("DATE",        COL_DATE, y + 17f, tableHeaderPaint)
                c.drawText("DESCRIPTION", COL_DESC, y + 17f, tableHeaderPaint)
                c.drawText("CATEGORY",    COL_CAT,  y + 17f, tableHeaderPaint)
                c.drawText("MODE",        COL_MODE, y + 17f, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.RIGHT
                c.drawText("AMOUNT",      COL_AMT,  y + 17f, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT
                return y + 26f
            }

            // ─────────────────────────────────────────────────────────────────
            // PAGE 1: HEADER
            // ─────────────────────────────────────────────────────────────────
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 100f, bgPaint)
            canvas.drawText("XPENSO", MARGIN, 60f, brandPaint)

            labelPaint.color = Color.WHITE; labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("Custom Date Range Statement", MARGIN, 80f, labelPaint)
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", PAGE_W - MARGIN, 80f, labelPaint)
            labelPaint.textAlign = Paint.Align.LEFT; labelPaint.color = COLOR_TEXT_SEC
            canvas.drawText("Period: $periodText", MARGIN, 115f, labelPaint)

            var yPos = 138f

            // ─────────────────────────────────────────────────────────────────
            // REPORT SUMMARY
            // ─────────────────────────────────────────────────────────────────
            yPos = sectionTitle(canvas, "REPORT SUMMARY", yPos)
            yPos += 6f

            val summaryH = 116f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + summaryH), 10f, 10f, bgPaint)

            val c1 = MARGIN + 18f; val c2 = MARGIN + 185f; val c3 = MARGIN + 360f
            val r1L = yPos + 20f;  val r1V = yPos + 36f
            val r2L = yPos + 62f;  val r2V = yPos + 78f

            canvas.drawText("Total Expenses",   c1, r1L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(totalAmount)}", c1, r1V, valuePaint)
            canvas.drawText("Transactions",     c2, r1L, labelPaint)
            canvas.drawText("${expenses.size} Entries", c2, r1V, valuePaint)
            canvas.drawText("Most Used Mode",   c3, r1L, labelPaint)
            canvas.drawText(mostUsedMode,        c3, r1V, valuePaint)

            canvas.drawText("Average Expense",  c1, r2L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(avgExpense)}", c1, r2V, valuePaint)
            canvas.drawText("Highest Expense",  c2, r2L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(highestExpense?.amount ?: 0.0)}", c2, r2V, valuePaint)
            canvas.drawText("Lowest Expense",   c3, r2L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(lowestExpense?.amount ?: 0.0)}", c3, r2V, valuePaint)

            yPos += summaryH + 22f

            // ─────────────────────────────────────────────────────────────────
            // CATEGORY SPENDING
            // ─────────────────────────────────────────────────────────────────
            yPos = sectionTitle(canvas, "CATEGORY SPENDING", yPos)
            yPos += 6f

            val catBlockH = (categorySummary.size * 26f) + 20f

            // Page break if category block won't fit
            if (yPos + catBlockH > PAGE_H - 80f) {
                newPage()
                yPos = MARGIN
                yPos = sectionTitle(canvas, "CATEGORY SPENDING", yPos)
                yPos += 6f
            }

            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + catBlockH), 10f, 10f, bgPaint)

            val barStart = MARGIN + 155f
            val barMaxW  = (PAGE_W - MARGIN * 2) - 155f - 60f
            var catY     = yPos + 18f

            categorySummary.forEach { (cat, catName, amt) ->
                val pct       = if (totalAmount > 0) (amt / totalAmount * 100) else 0.0
                val pctStr    = if (pct < 1.0 && pct > 0.0) "%.2f%%".format(pct) else "${pct.toInt()}%"
                val catColor  = cat.color.toArgb()
                val barW      = (pct / 100.0 * barMaxW).toFloat().coerceAtLeast(if (pct > 0.0) 2f else 0f)

                canvas.drawText(catName, MARGIN + 14f, catY, labelPaint)

                // Track
                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(barStart, catY - 7f, barStart + barMaxW, catY + 4f), 3f, 3f, bgPaint)
                // Fill — category's own color
                if (barW > 0f) {
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(barStart, catY - 7f, barStart + barW, catY + 4f), 3f, 3f, bgPaint)
                }

                // Percent text in category color
                valuePaint.color = COLOR_PRIMARY
                canvas.drawText(pctStr, barStart + barMaxW + 8f, catY, valuePaint)
                valuePaint.color = COLOR_PRIMARY  // reset

                catY += 26f
            }

            yPos += catBlockH + 22f

            // ─────────────────────────────────────────────────────────────────
            // TRANSACTION TABLE
            // ─────────────────────────────────────────────────────────────────
            yPos = sectionTitle(canvas, "ALL TRANSACTIONS  (${expenses.size} records)", yPos)
            yPos += 4f
            yPos = drawTableHeader(canvas, yPos)

            val tableDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            expenses.forEachIndexed { index, expense ->
                if (yPos > PAGE_H - 80f) {
                    newPage()
                    yPos = MARGIN
                    yPos = sectionTitle(canvas, "ALL TRANSACTIONS (continued)", yPos)
                    yPos += 4f
                    yPos = drawTableHeader(canvas, yPos)
                }

                if (index % 2 != 0) {
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRect(MARGIN, yPos, PAGE_W - MARGIN, yPos + 24f, bgPaint)
                }

                val dateStr  = tableDate.format(Date(expense.timestamp))
                val title    = if (expense.title.length > 20) expense.title.take(17) + "…" else expense.title
                val category = (
                        ExpenseCategory.entries
                            .find { cat -> cat.name == expense.category }
                            ?: ExpenseCategory.OTHER
                        ).name.formatCategoryName()
                val mode     = when {
                    expense.paymentMode.isBlank()                    -> "-"
                    expense.paymentMode.contains("Card", true) -> "Card"
                    else                                             -> expense.paymentMode
                }

                canvas.drawText(dateStr,  COL_DATE, yPos + 15f, rowSecPaint)
                canvas.drawText(title,    COL_DESC, yPos + 15f, rowPaint)
                canvas.drawText(category, COL_CAT,  yPos + 15f, rowSecPaint)
                canvas.drawText(mode,     COL_MODE, yPos + 15f, rowSecPaint)
                canvas.drawText(
                    "$currencySymbol ${fmt(expense.amount)}",
                    COL_AMT, yPos + 15f, amountPaint
                )

                canvas.drawLine(MARGIN, yPos + 24f, PAGE_W - MARGIN, yPos + 24f, dividerPaint)
                yPos += 24f
            }

            // ── Grand total row ───────────────────────────────────────────────
            if (yPos > PAGE_H - 50f) { newPage(); yPos = MARGIN }
            yPos += 4f
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + 26f), 6f, 6f, bgPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("TOTAL   ${expenses.size} transactions", COL_DESC, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$currencySymbol ${fmt(totalAmount)}", COL_AMT, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT

            // ── Finalize ──────────────────────────────────────────────────────
            drawFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            document.close()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun String.formatCategoryName(): String =
        replace("_", " ")
            .lowercase()
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    private fun fmt(value: Double) = String.format("%,.0f", value)

    private fun drawFooter(canvas: Canvas, pageNum: Int, width: Int, height: Int, paint: Paint) {
        val linePaint = Paint().apply { color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f }
        canvas.drawLine(40f, height - 44f, width - 40f, height - 44f, linePaint)
        canvas.drawText(
            "Xpenso  •  Custom Date Range Report  •  Page $pageNum",
            width / 2f, height - 30f, paint
        )
    }
}
