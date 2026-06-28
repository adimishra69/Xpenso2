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

    private const val COLOR_PRIMARY   = 0xFF3949AB.toInt() // Indigo Primary (Premium Header / Bars)

    private const val COLOR_TEXT_MAIN = 0xFF202124.toInt() // Main Text (unchanged)

    private const val COLOR_TEXT_SEC  = 0xFF5F6368.toInt() // Secondary Text (unchanged)

    private const val COLOR_BG_LIGHT  = 0xFFF8F9FA.toInt() // Light Background (unchanged)

    // ── page / layout constants ──────────────────────────────────────────────
    private const val PAGE_W  = 595
    private const val PAGE_H  = 842
    private const val MARGIN  = 40f
    private val CONTENT_W get() = PAGE_W - MARGIN * 2   // 515

    // ── table column X positions ─────────────────────────────────────────────
    private val COL_DATE = MARGIN + 10f
    private val COL_DESC = MARGIN + 95f
    private val COL_CAT  = MARGIN + 244f
    private val COL_MODE = MARGIN + 364f
    private val COL_AMT  = PAGE_W - MARGIN - 8f   // right-aligned

    // ────────────────────────────────────────────────────────────────────────
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
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()

            // ── paints ───────────────────────────────────────────────────────
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
            val watermarkPaint = Paint().apply {
                textSize = 75f; color = Color.LTGRAY; alpha = 35
                textAlign = Paint.Align.CENTER
            }
            val dividerPaint = Paint().apply {
                color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f
            }
            val bgPaint  = Paint()
            val barPaint = Paint().apply { color = COLOR_PRIMARY }

            val currency = currencySymbol

            // ── aggregates ───────────────────────────────────────────────────
            val totalAmount     = expenses.sumOf { it.amount }
            val avgExpense      = totalAmount / expenses.size
            val highestExpense  = expenses.maxByOrNull { it.amount }
            val lowestExpense   = expenses.minByOrNull { it.amount }

            val minDate = expenses.minOf { it.timestamp }
            val maxDate = expenses.maxOf { it.timestamp }

            val dateFormat  = SimpleDateFormat("dd MMM yyyy ", Locale.getDefault())
            val periodText  = "${dateFormat.format(Date(minDate))} – ${dateFormat.format(Date(maxDate))}"

            val modeMap = expenses.groupBy {
                when {
                    it.paymentMode.contains("Card", true) -> "Card"
                    it.paymentMode.isBlank()               -> "Un" +
                            "known"
                    else                                   -> it.paymentMode
                }
            }.mapValues { it.value.size }
            val maxMode     = modeMap.maxByOrNull { it.value }
            val modePercent = if (expenses.isNotEmpty()) ((maxMode?.value ?: 0) * 100 / expenses.size) else 0
            val mostUsedMode= "${maxMode?.key ?: "-"} ($modePercent%)"

            // ── state vars ───────────────────────────────────────────────────
            var pageNumber = 1
            var page       = document.startPage(pageInfo)
            var canvas     = page.canvas

            val today = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            // ── helpers ──────────────────────────────────────────────────────
            fun newPage() {
                drawWatermark(canvas, PAGE_W, PAGE_H, watermarkPaint, isPremium)
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

            // ── PAGE 1: HEADER ───────────────────────────────────────────────
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 100f, bgPaint)

            canvas.drawText("XPENSO", MARGIN, 60f, brandPaint)

            labelPaint.color = Color.WHITE; labelPaint.textAlign = Paint.Align.LEFT
            val subtitle = if (isPremium) " Complete Financial Report" else "Summary Financial Report"
            canvas.drawText(subtitle, MARGIN, 80f, labelPaint)

            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", PAGE_W - MARGIN, 80f, labelPaint)

            labelPaint.textAlign = Paint.Align.LEFT; labelPaint.color = COLOR_TEXT_SEC
            canvas.drawText("Period: $periodText", MARGIN, 115f, labelPaint)

            var yPos = 138f

            // ── REPORT SUMMARY ───────────────────────────────────────────────
            yPos = sectionTitle(canvas, "REPORT SUMMARY", yPos)
            yPos += 6f

            val summaryH = 116f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + summaryH), 10f, 10f, bgPaint)

            val c1 = MARGIN + 18f;  val c2 = MARGIN + 185f;  val c3 = MARGIN + 360f
            val r1L = yPos + 20f;   val r1V = yPos + 36f
            val r2L = yPos + 62f;   val r2V = yPos + 78f
            val r3L = yPos + 96f

            // row 1
            canvas.drawText("Total Expenses",    c1, r1L, labelPaint)
            canvas.drawText("$currency ${fmt(totalAmount)}", c1, r1V, valuePaint)
            canvas.drawText("Transactions",       c2, r1L, labelPaint)
            canvas.drawText("${expenses.size} Entries", c2, r1V, valuePaint)
            canvas.drawText("Most Used Mode",     c3, r1L, labelPaint)
            canvas.drawText(mostUsedMode,          c3, r1V, valuePaint)
            // row 2
            canvas.drawText("Average Expense",   c1, r2L, labelPaint)
            canvas.drawText("$currency ${fmt(avgExpense)}", c1, r2V, valuePaint)
            canvas.drawText("Highest Expense",   c2, r2L, labelPaint)
            canvas.drawText("$currency ${fmt(highestExpense?.amount ?: 0.0)}", c2, r2V, valuePaint)
            canvas.drawText("Lowest Expense",    c3, r2L, labelPaint)
            canvas.drawText("$currency ${fmt(lowestExpense?.amount ?: 0.0)}", c3, r2V, valuePaint)

            yPos += summaryH + 22f

            // ── CATEGORY SPENDING ────────────────────────────────────────────
            // AFTER — all categories, sorted by amount, no merging
            val categorySummary = expenses
                .groupBy {
                    ExpenseCategory.entries
                        .find { cat -> cat.name == it.category }
                        ?: ExpenseCategory.OTHER
                }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .toList()
                .sortedByDescending { it.second }

            yPos = sectionTitle(canvas, "CATEGORY SPENDING", yPos)
            yPos += 6f

            val catBlockH = (categorySummary.size * 26f) + 20f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + catBlockH), 10f, 10f, bgPaint)

            var catY        = yPos + 18f
            var accumulated = 0
            val barStart    = MARGIN + 155f
            val barMaxW     = CONTENT_W - 155f - 60f

            // AFTER
            categorySummary.forEachIndexed { index, (cat, amt) ->
                // AFTER
                val percent = amt / totalAmount * 100
                val percentStr = if (percent < 1.0 && percent > 0.0) String.format("%.2f%%", percent) else "${percent.toInt()}%"
                val name = cat.name.replace("_"," ").lowercase().replaceFirstChar { it.uppercase() }

                val barW = (percent / 100.0 * barMaxW).toFloat().coerceAtLeast(if (percent > 0.0) 2f else 0f)


                canvas.drawText(name, MARGIN + 14f, catY, labelPaint)

                // track
                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(barStart, catY - 7f, barStart + barMaxW, catY + 4f), 3f, 3f, bgPaint)
                // fill
                if (barW > 0f) {
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(barStart, catY - 7f, barStart + barW, catY + 4f), 3f, 3f, bgPaint)
                }

                // AFTER
                canvas.drawText(percentStr, barStart + barMaxW + 8f, catY, valuePaint)
                catY += 26f
            }

            yPos += catBlockH + 22f

            // ── TRANSACTIONS TABLE ───────────────────────────────────────────
            // Start transactions on the current page only if there is enough space.
// Otherwise, move the entire section to a new page.

            val minimumTransactionSpace = 130f   // title + header + ~3 rows

            if (yPos + minimumTransactionSpace > PAGE_H - 60f) {
                newPage()
                yPos = MARGIN
            }

            yPos = sectionTitle(canvas, "ALL TRANSACTIONS  (${expenses.size} records)", yPos)
            yPos += 4f
            yPos = drawTableHeader(canvas, yPos)

            val tableDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // AFTER — newest date first
            val sortedExpenses = expenses.sortedByDescending { it.timestamp }
            sortedExpenses.forEachIndexed { index, expense ->

                // page break
                if (yPos > PAGE_H - 80f) {
                    newPage()
                    yPos = MARGIN
                    yPos = sectionTitle(canvas, "ALL TRANSACTIONS (continued)", yPos)
                    yPos += 4f
                    yPos = drawTableHeader(canvas, yPos)
                }

                // alternating row bg
                if (index % 2 != 0) {
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRect(MARGIN, yPos, PAGE_W - MARGIN, yPos + 24f, bgPaint)
                }

                val dateStr  = tableDate.format(Date(expense.timestamp))
                val category = (
                        ExpenseCategory.entries
                            .find { cat -> cat.name == expense.category }
                            ?: ExpenseCategory.OTHER
                        ).name
                    .replace("_"," ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
                val mode     = if (expense.paymentMode.isBlank()) "-"
                else if (expense.paymentMode.contains("Card",true)) "Card"
                else expense.paymentMode
                val title    = if (expense.title.length > 20) expense.title.take(17) + "…" else expense.title

                canvas.drawText(dateStr,   COL_DATE, yPos + 15f, rowSecPaint)
                canvas.drawText(title,     COL_DESC, yPos + 15f, rowPaint)
                canvas.drawText(category,  COL_CAT,  yPos + 15f, rowSecPaint)
                canvas.drawText(mode,      COL_MODE, yPos + 15f, rowSecPaint)
                canvas.drawText("$currency ${fmt(expense.amount)}", COL_AMT, yPos + 15f, amountPaint)

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

            // ── finalize ─────────────────────────────────────────────────────
            drawWatermark(canvas, PAGE_W, PAGE_H, watermarkPaint, isPremium)
            drawFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            document.close()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── footer ───────────────────────────────────────────────────────────────
    private fun drawFooter(canvas: Canvas, pageNum: Int, width: Int, height: Int, paint: Paint) {
        val linePaint = Paint().apply { color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f }
        canvas.drawLine(40f, height - 44f, width - 40f, height - 44f, linePaint)
        canvas.drawText(
            "Xpenso  •  Personal Finance Report  •  Page $pageNum",
            width / 2f, height - 30f, paint
        )
    }

    // ── number formatter ─────────────────────────────────────────────────────
    private fun fmt(value: Double) = String.format("%,.0f", value)
}

// ── watermark (kept top-level to match original signature) ───────────────────
fun drawWatermark(canvas: Canvas, pageWidth: Int, pageHeight: Int, paint: Paint, isPremium: Boolean) {
    if (isPremium) return
    canvas.save()
    canvas.rotate(-30f, pageWidth / 2f, pageHeight / 2f)
    canvas.drawText("XPENSO", pageWidth / 2f, pageHeight / 2f, paint)
    canvas.restore()
}