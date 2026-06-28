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

object YearlyPdfReportGenerator {

    private const val COLOR_PRIMARY  = 0xFF3949AB.toInt()
    private const val COLOR_TEXT_MAIN= 0xFF202124.toInt()
    private const val COLOR_TEXT_SEC = 0xFF5F6368.toInt()
    private const val COLOR_BG_LIGHT = 0xFFF8F9FA.toInt()

    // ── page / layout constants ──────────────────────────────────────────────
    private const val PAGE_W  = 595
    private const val PAGE_H  = 842
    private const val MARGIN  = 40f
    private val CONTENT_W get() = PAGE_W - MARGIN * 2          // 515
    private const val FOOTER_Y = PAGE_H - 30f

    // ── table column X positions (mirrors PdfReportGenerator exactly) ────────
    private val COL_DATE = MARGIN + 10f
    private val COL_DESC = MARGIN + 95f
    private val COL_CAT  = MARGIN + 244f
    private val COL_MODE = MARGIN + 364f
    private val COL_AMT  = PAGE_W - MARGIN - 8f   // right-aligned

    // ────────────────────────────────────────────────────────────────────────
    fun generateYearlyReport(
        context: Context,
        uri: Uri,
        expenses: List<ExpenseEntity>,
        currencySymbol: String,
        startOfMonth: Int,
        year: Int
    ): Result<Unit> {

        if (expenses.isEmpty())
            return Result.failure(Exception("No data available"))

        // ── cycle date logic (unchanged) ─────────────────────────────────────
        val now = Calendar.getInstance()
        var month = Calendar.JANUARY
        if (now.get(Calendar.DAY_OF_MONTH) < startOfMonth) {
            month -= 1
            if (month < 0) month = 11
        }
        val startCalendar = Calendar.getInstance().apply {
            set(year, month, startOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCalendar = (startCalendar.clone() as Calendar).apply {
            add(Calendar.YEAR, 1)
            add(Calendar.MILLISECOND, -1)
        }
        val cycleStart = startCalendar.timeInMillis
        val cycleEnd   = endCalendar.timeInMillis

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val periodText = "${dateFormat.format(Date(cycleStart))} – ${dateFormat.format(Date(cycleEnd))}"

        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()

            // ── paints (identical to PdfReportGenerator) ────────────────────
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

            val currency = currencySymbol

            // ── aggregates (logic unchanged) ─────────────────────────────────
            val cycleExpenses = expenses
                .filter { it.timestamp in cycleStart..cycleEnd }
                .sortedByDescending { it.timestamp }

            val totalAmount    = cycleExpenses.sumOf { it.amount }
            val avgPerDay      = totalAmount / maxOf(1, getDaysBetween(cycleExpenses))
            val highestExpense = cycleExpenses.maxByOrNull { it.amount }

            val modeMap = cycleExpenses.groupBy {
                when { it.paymentMode.contains("Card", true) -> "Card"; else -> it.paymentMode }
            }.mapValues { it.value.size }
            val mostMode = modeMap.maxByOrNull { it.value }
            val mostUsedMode = "${mostMode?.key ?: "-"} (${((mostMode?.value ?: 0) * 100 / maxOf(1, cycleExpenses.size))}%)"

            val shortMonthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
            val fullMonthFormat  = SimpleDateFormat("MMMM", Locale.ENGLISH)
            val monthlyMap = cycleExpenses
                .groupBy { shortMonthFormat.format(Date(it.timestamp)) }
                .mapValues { it.value.sumOf { e -> e.amount } }

            val mostMonthShort = monthlyMap.maxByOrNull { it.value }?.key
            val mostMonth = mostMonthShort?.let {
                fullMonthFormat.format(shortMonthFormat.parse(it)!!)
            } ?: "-"

            val categoryMap = cycleExpenses
                .groupBy {
                    ExpenseCategory.entries
                        .find { cat -> cat.name == it.category }
                        ?: ExpenseCategory.OTHER
                }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .toList()
                .sortedByDescending { it.second }

            // ── state vars ───────────────────────────────────────────────────
            var pageNumber = 1
            var page       = document.startPage(pageInfo)
            var canvas     = page.canvas
            val today      = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            // ── helpers ──────────────────────────────────────────────────────
            fun newPage() {
                drawFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
                document.finishPage(page)
                pageNumber++
                page   = document.startPage(pageInfo)
                canvas = page.canvas
            }

            fun ensureSpace(needed: Float, yPos: Float): Float =
                if (yPos + needed > FOOTER_Y - 10f) { newPage(); MARGIN } else yPos

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
            canvas.drawText("Yearly Financial Report", MARGIN, 80f, labelPaint)
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", PAGE_W - MARGIN, 80f, labelPaint)
            labelPaint.textAlign = Paint.Align.LEFT; labelPaint.color = COLOR_TEXT_SEC
            canvas.drawText("Period: $periodText", MARGIN, 115f, labelPaint)

            var yPos = 138f

            // ── YEARLY SUMMARY ───────────────────────────────────────────────
            yPos = sectionTitle(canvas, "YEARLY SUMMARY", yPos)
            yPos += 6f

            val summaryH = 116f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + summaryH), 10f, 10f, bgPaint)

            val c1 = MARGIN + 18f;  val c2 = MARGIN + 185f;  val c3 = MARGIN + 360f
            val r1L = yPos + 20f;   val r1V = yPos + 36f
            val r2L = yPos + 62f;   val r2V = yPos + 78f

            canvas.drawText("Total Spending",        c1, r1L, labelPaint)
            canvas.drawText("$currency ${fmt(totalAmount)}", c1, r1V, valuePaint)
            canvas.drawText("Transactions",           c2, r1L, labelPaint)
            canvas.drawText("${cycleExpenses.size} Entries", c2, r1V, valuePaint)
            canvas.drawText("Average / Day",          c3, r1L, labelPaint)
            canvas.drawText("$currency ${fmt(avgPerDay)}", c3, r1V, valuePaint)

            canvas.drawText("Most Used Mode",         c1, r2L, labelPaint)
            canvas.drawText(mostUsedMode,              c1, r2V, valuePaint)
            canvas.drawText("Largest Expense",        c2, r2L, labelPaint)
            canvas.drawText("$currency ${fmt(highestExpense?.amount ?: 0.0)}", c2, r2V, valuePaint)
            canvas.drawText("Most Spending Month",    c3, r2L, labelPaint)
            canvas.drawText(mostMonth,                 c3, r2V, valuePaint)

            yPos += summaryH + 22f

            // ── MONTHLY TREND ────────────────────────────────────────────────
            yPos = sectionTitle(canvas, "MONTHLY TREND", yPos)
            yPos += 6f

            val months      = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val maxMonthAmt = monthlyMap.maxOfOrNull { it.value } ?: 1.0
            val barStart    = MARGIN + 110f
            val barMaxW     = CONTENT_W - 110f - 130f
            val monthBlockH = (months.size * 26f) + 20f

            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + monthBlockH), 10f, 10f, bgPaint)

            var monY = yPos + 18f
            months.forEach { mon ->
                val amt     = monthlyMap[mon] ?: 0.0
                val percent = (amt / maxMonthAmt).toFloat()
                val barW    = (percent * barMaxW).coerceAtLeast(if (amt > 0) 2f else 0f)

                canvas.drawText(mon, MARGIN + 14f, monY, labelPaint)

                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(barStart, monY - 7f, barStart + barMaxW, monY + 4f), 3f, 3f, bgPaint)
                if (barW > 0f) {
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(barStart, monY - 7f, barStart + barW, monY + 4f), 3f, 3f, bgPaint)
                }

                // Show amount (not percentage) — same style as PdfReportGenerator category section
                valuePaint.textSize = 11f
                canvas.drawText(
                    if (amt > 0) "$currency ${fmt(amt)}" else "-",
                    barStart + barMaxW + 8f,
                    monY + 2f,   // shift slightly downward for better alignment
                    valuePaint
                )
                monY += 26f
            }
            yPos += monthBlockH + 22f

            // ── CATEGORY SPENDING ────────────────────────────────────────────
            val categoryRequiredSpace = categoryMap.size * 26f + 50f
            yPos = ensureSpace(categoryRequiredSpace, yPos)
            yPos = sectionTitle(canvas, "CATEGORY SPENDING", yPos)
            yPos += 6f

            val catBlockH   = (categoryMap.size * 26f) + 20f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + catBlockH), 10f, 10f, bgPaint)

            // Same alignment as MONTHLY TREND
            val catBarStart = MARGIN + 110f
            val catBarMaxW  = CONTENT_W - 110f - 130f
            val maxCatAmt   = categoryMap.firstOrNull()?.second ?: 1.0
            var catY        = yPos + 18f

            categoryMap.forEach { (cat, amt) ->
                val name    = cat.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                val percent = (amt / maxCatAmt).toFloat()
                val barW    = (percent * catBarMaxW).coerceAtLeast(if (amt > 0) 2f else 0f)

                canvas.drawText(name, MARGIN + 14f, catY, labelPaint)

                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(catBarStart, catY - 7f, catBarStart + catBarMaxW, catY + 4f), 3f, 3f, bgPaint)
                if (barW > 0f) {
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(catBarStart, catY - 7f, catBarStart + barW, catY + 4f), 3f, 3f, bgPaint)
                }

                valuePaint.textSize = 11f
                // Show amount (matching PdfReportGenerator — valuePaint, right of bar)
                canvas.drawText("$currency ${fmt(amt)}", catBarStart + catBarMaxW + 8f, catY+2f, valuePaint)
                catY += 26f
            }
            yPos += catBlockH + 22f

            // ── TRANSACTIONS TABLE ───────────────────────────────────────────
            val minimumTableSpace = 130f
            yPos = ensureSpace(minimumTableSpace, yPos)
            yPos = sectionTitle(canvas, "ALL TRANSACTIONS  (${cycleExpenses.size} records)", yPos)
            yPos += 4f
            yPos = drawTableHeader(canvas, yPos)

            val tableDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            var grandTotal = 0.0

            cycleExpenses.forEachIndexed { index, expense ->
                if (yPos > PAGE_H - 80f) {
                    newPage()
                    yPos = MARGIN
                    yPos = sectionTitle(canvas, "ALL TRANSACTIONS (continued)", yPos)
                    yPos += 4f
                    yPos = drawTableHeader(canvas, yPos)
                }

                // alternating row background
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
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
                val mode  = if (expense.paymentMode.isBlank()) "-"
                else if (expense.paymentMode.contains("Card", true)) "Card"
                else expense.paymentMode
                val title = if (expense.title.length > 20) expense.title.take(17) + "…" else expense.title

                canvas.drawText(dateStr,  COL_DATE, yPos + 15f, rowSecPaint)
                canvas.drawText(title,    COL_DESC, yPos + 15f, rowPaint)
                canvas.drawText(category, COL_CAT,  yPos + 15f, rowSecPaint)
                canvas.drawText(mode,     COL_MODE, yPos + 15f, rowSecPaint)
                canvas.drawText("$currency ${fmt(expense.amount)}", COL_AMT, yPos + 15f, amountPaint)

                canvas.drawLine(MARGIN, yPos + 24f, PAGE_W - MARGIN, yPos + 24f, dividerPaint)

                grandTotal += expense.amount
                yPos += 24f
            }

            // ── GRAND TOTAL ROW ───────────────────────────────────────────────
            if (yPos > PAGE_H - 50f) { newPage(); yPos = MARGIN }
            yPos += 4f
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + 26f), 6f, 6f, bgPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("TOTAL SPENDING", COL_DESC, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$currency ${fmt(grandTotal)}", COL_AMT, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT

            // ── finalize ─────────────────────────────────────────────────────
            drawFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            document.close()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun drawFooter(canvas: Canvas, pageNum: Int, width: Int, height: Int, paint: Paint) {
        val linePaint = Paint().apply { color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f }
        canvas.drawLine(40f, height - 44f, width - 40f, height - 44f, linePaint)
        canvas.drawText(
            "Xpenso  •  Yearly Financial Report  •  Page $pageNum",
            width / 2f, height - 30f, paint
        )
    }

    private fun getDaysBetween(expenses: List<ExpenseEntity>): Int {
        val min = expenses.minOf { it.timestamp }
        val max = expenses.maxOf { it.timestamp }
        return ((max - min) / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    private fun fmt(value: Double) = String.format("%,.0f", value)
}