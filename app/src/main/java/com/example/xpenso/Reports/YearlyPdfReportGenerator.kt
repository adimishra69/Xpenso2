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

    private const val COLOR_PRIMARY   = 0xFF3949AB.toInt() // Indigo Primary (Premium Header / Bars)

    private const val COLOR_TEXT_MAIN = 0xFF202124.toInt() // Main Text (unchanged)

    private const val COLOR_TEXT_SEC  = 0xFF5F6368.toInt() // Secondary Text (unchanged)

    private const val COLOR_BG_LIGHT  = 0xFFF8F9FA.toInt() // Light Background (unchanged)

    // ── layout constants ────────────────────────────────────────────────────
    private const val PAGE_W   = 595
    private const val PAGE_H   = 842
    private const val MARGIN   = 40f
    private const val CONTENT_W = PAGE_W - MARGIN * 2          // 515
    private const val FOOTER_Y  = PAGE_H - 30f

    // ── table column X positions (left edge of each column) ─────────────────
    // #  | Date       | Title              | Category      | Mode        | Amount
    private val COL_NUM  = MARGIN
    private val COL_DATE = MARGIN + 24f
    private val COL_TITLE= MARGIN + 92f
    private val COL_CAT  = MARGIN + 228f
    private val COL_MODE = MARGIN + 330f
    private val COL_AMT  = MARGIN + 420f          // right-aligned to MARGIN+515

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

            // ── paints ──────────────────────────────────────────────────────
            val brandPaint = TextPaint().apply {
                textSize = 28f; typeface = Typeface.DEFAULT_BOLD; color = Color.WHITE
            }
            val titlePaint = TextPaint().apply {
                textSize = 13f; typeface = Typeface.DEFAULT_BOLD; color = COLOR_TEXT_MAIN
                letterSpacing = 0.08f
            }
            val labelPaint = TextPaint().apply {
                textSize = 9f; color = COLOR_TEXT_SEC
            }
            val valuePaint = TextPaint().apply {
                textSize = 13f; typeface = Typeface.DEFAULT_BOLD; color = COLOR_PRIMARY
            }
            val footerPaint = TextPaint().apply {
                textSize = 8f; textAlign = Paint.Align.CENTER; color = COLOR_TEXT_SEC
            }
            val tableHeaderPaint = TextPaint().apply {
                textSize = 8f; typeface = Typeface.DEFAULT_BOLD
                color = Color.WHITE
            }
            val tableCellPaint = TextPaint().apply {
                textSize = 8f; color = COLOR_TEXT_MAIN
            }
            val tableCellSecPaint = TextPaint().apply {
                textSize = 8f; color = COLOR_TEXT_SEC
            }
            val amtCellPaint = TextPaint().apply {
                textSize = 8f; typeface = Typeface.DEFAULT_BOLD
                color = COLOR_PRIMARY; textAlign = Paint.Align.RIGHT
            }
            val dividerPaint = Paint().apply {
                color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f
            }
            val bgPaint   = Paint()
            val barPaint  = Paint().apply { color = COLOR_PRIMARY }
            val barBgPaint= Paint().apply { color = 0xFFE3F2FD.toInt() }

            val currency = currencySymbol

            val cycleExpenses = expenses
                .filter { it.timestamp in cycleStart..cycleEnd }
                .sortedByDescending { it.timestamp }

            val totalAmount  = cycleExpenses.sumOf { it.amount }
            val avgPerDay    = totalAmount / maxOf(1, getDaysBetween(cycleExpenses))
            val highestExpense = cycleExpenses.maxByOrNull { it.amount }

            val modeMap = cycleExpenses.groupBy {
                when { it.paymentMode.contains("Card", true) -> "Card"; else -> it.paymentMode }
            }.mapValues { it.value.size }
            val mostMode = modeMap.maxByOrNull { it.value }
            val mostUsedMode = "${mostMode?.key ?: "-"} (${((mostMode?.value ?: 0) * 100 / maxOf(1,cycleExpenses.size))}%)"

            val shortMonthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
            val fullMonthFormat  = SimpleDateFormat("MMMM", Locale.ENGLISH)

            val monthlyMap = cycleExpenses
                .groupBy { shortMonthFormat.format(Date(it.timestamp)) }
                .mapValues { it.value.sumOf { e -> e.amount } }

            val mostMonthShort = monthlyMap.maxByOrNull { it.value }?.key

            val mostMonth = mostMonthShort?.let {
                fullMonthFormat.format(
                    shortMonthFormat.parse(it)!!
                )
            } ?: "-"

            val categoryMap  = cycleExpenses
                .groupBy {
                    ExpenseCategory.entries
                        .find { cat -> cat.name == it.category }
                        ?: ExpenseCategory.OTHER
                }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .toList().sortedByDescending { it.second }

            // ── state vars ──────────────────────────────────────────────────
            var pageNumber = 1
            var page       = document.startPage(pageInfo)
            var canvas     = page.canvas
            val today      = SimpleDateFormat("dd MMM yyyy, hh:mm a" , Locale.getDefault()).format(Date())

            // ── helper lambdas ───────────────────────────────────────────────
            fun newPage() {
                drawFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
                document.finishPage(page)
                pageNumber++
                page   = document.startPage(pageInfo)
                canvas = page.canvas
            }

            fun ensureSpace(needed: Float, yPos: Float): Float {
                return if (yPos + needed > FOOTER_Y - 10) { newPage(); MARGIN }
                else yPos
            }

            fun drawHeader(c: Canvas) {
                bgPaint.color = COLOR_PRIMARY
                c.drawRect(0f, 0f, PAGE_W.toFloat(), 100f, bgPaint)
                c.drawText("XPENSO", MARGIN, 60f, brandPaint)
                labelPaint.color = Color.WHITE; labelPaint.textAlign = Paint.Align.LEFT
                c.drawText("Yearly Financial Report", MARGIN, 80f, labelPaint)
                labelPaint.textAlign = Paint.Align.RIGHT
                c.drawText("Generated: $today", PAGE_W - MARGIN, 80f, labelPaint)
                labelPaint.textAlign = Paint.Align.LEFT; labelPaint.color = COLOR_TEXT_SEC
                c.drawText("Period: $periodText", MARGIN, 115f, labelPaint)
            }

            fun drawSectionTitle(c: Canvas, text: String, y: Float): Float {
                c.drawText(text, MARGIN, y, titlePaint)
                c.drawLine(MARGIN, y + 4f, PAGE_W - MARGIN, y + 4f, dividerPaint)
                return y + 20f
            }

            // ── PAGE 1 HEADER ────────────────────────────────────────────────
            drawHeader(canvas)
            var yPos = 138f

            // ── YEARLY SUMMARY SECTION ───────────────────────────────────────
            yPos = drawSectionTitle(canvas, "YEARLY SUMMARY", yPos)
            yPos += 6f

            val summaryBoxH = 110f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + summaryBoxH), 10f, 10f, bgPaint)

            val col1 = MARGIN + 18f
            val col2 = MARGIN + 180f
            val col3 = MARGIN + 350f
            val row1L = yPos + 22f;  val row1V = yPos + 38f
            val row2L = yPos + 62f;  val row2V = yPos + 78f

            // row 1
            canvas.drawText("Total Spending",      col1, row1L, labelPaint)
            canvas.drawText("$currency ${fmt(totalAmount)}", col1, row1V, valuePaint)
            canvas.drawText("Transactions",         col2, row1L, labelPaint)
            canvas.drawText("${cycleExpenses.size} Entries", col2, row1V, valuePaint)
            canvas.drawText("Average / Day",        col3, row1L, labelPaint)
            canvas.drawText("$currency ${fmt(avgPerDay)}", col3, row1V, valuePaint)
            // row 2
            canvas.drawText("Most Used Mode",       col1, row2L, labelPaint)
            canvas.drawText(mostUsedMode,            col1, row2V, valuePaint)
            canvas.drawText("Largest Expense",      col2, row2L, labelPaint)
            canvas.drawText("$currency ${fmt(highestExpense?.amount ?: 0.0)}", col2, row2V, valuePaint)
            canvas.drawText("Most Spending Month",  col3, row2L, labelPaint)
            canvas.drawText(mostMonth,              col3, row2V, valuePaint)

            yPos += summaryBoxH + 22f

            // ── MONTHLY TREND ────────────────────────────────────────────────
            yPos = drawSectionTitle(canvas, "MONTHLY TREND", yPos)
            yPos += 6f

            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val maxMonthAmt = monthlyMap.maxOfOrNull { it.value } ?: 1.0
            val barStart = MARGIN + 36f
            val barMaxW  = CONTENT_W - 36f - 110f       // leave room for amount text
            val rowH     = 18f

            months.forEach { mon ->
                val amt     = monthlyMap[mon] ?: 0.0
                val percent = (amt / maxMonthAmt).toFloat()
                val barW    = (percent * barMaxW).coerceAtLeast(if (amt > 0) 2f else 0f)

                canvas.drawText(mon, MARGIN, yPos + 2f, labelPaint)

                // background track
                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(barStart, yPos - 7f, barStart + barMaxW, yPos + 5f), 3f, 3f, bgPaint)
                // fill bar
                if (barW > 0f) {
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(barStart, yPos - 7f, barStart + barW, yPos + 5f), 3f, 3f, bgPaint)
                }

                canvas.drawText(
                    if (amt > 0) "$currency ${fmt(amt)}" else "-",
                    barStart + barMaxW + 8f, yPos + 2f, labelPaint
                )
                yPos += rowH
            }
            yPos += 10f

            // ── CATEGORY SPENDING ────────────────────────────────────────────
            yPos = ensureSpace(categoryMap.size * 18f + 40f, yPos)
            yPos = drawSectionTitle(canvas, "CATEGORY SPENDING", yPos)
            yPos += 6f

            val maxCatAmt = categoryMap.firstOrNull()?.second ?: 1.0
            val catBarStart = MARGIN + 148f
            val catBarMaxW  = CONTENT_W - 148f - 110f

            categoryMap.forEach { (cat, amt) ->
                val name    = cat.name.replace("_"," ").lowercase().replaceFirstChar { it.uppercase() }
                val percent = (amt / maxCatAmt).toFloat()
                val barW    = (percent * catBarMaxW).coerceAtLeast(if (amt > 0) 2f else 0f)

                canvas.drawText(name, MARGIN, yPos + 2f, labelPaint)

                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(catBarStart, yPos - 7f, catBarStart + catBarMaxW, yPos + 5f), 3f, 3f, bgPaint)
                if (barW > 0f) {
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(catBarStart, yPos - 7f, catBarStart + barW, yPos + 5f), 3f, 3f, bgPaint)
                }

                canvas.drawText(
                    "$currency ${fmt(amt)}",
                    catBarStart + catBarMaxW + 8f, yPos + 2f, labelPaint
                )
                yPos += 18f
            }
            yPos += 14f

            // ── TRANSACTIONS TABLE ───────────────────────────────────────────
            // always start on a fresh page for the table
            newPage()
            yPos = MARGIN

            // draw table section title
            yPos = drawSectionTitle(canvas, "ALL TRANSACTIONS  (${cycleExpenses.size} records)", yPos)
            yPos += 6f

            // table header row
            fun drawTableHeader(c: Canvas, y: Float): Float {
                bgPaint.color = COLOR_PRIMARY
                c.drawRect(MARGIN, y - 12f, PAGE_W - MARGIN, y + 6f, bgPaint)
                c.drawText("#",        COL_NUM,  y, tableHeaderPaint)
                c.drawText("Date",     COL_DATE, y, tableHeaderPaint)
                c.drawText("Title",    COL_TITLE,y, tableHeaderPaint)
                c.drawText("Category", COL_CAT,  y, tableHeaderPaint)
                c.drawText("Mode",     COL_MODE, y, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.RIGHT
                c.drawText("Amount",   PAGE_W - MARGIN, y, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT
                return y + 16f
            }

            yPos = drawTableHeader(canvas, yPos)

            val rowDateFmt = SimpleDateFormat("dd MMM yy", Locale.getDefault())
            var rowIndex   = 0
            var grandTotal = 0.0

            cycleExpenses.forEach { expense ->
                // check page overflow (row height = 16, leave room for total row = 28)
                if (yPos > FOOTER_Y - 44f) {
                    newPage()
                    yPos = MARGIN
                    yPos = drawSectionTitle(canvas, "ALL TRANSACTIONS (continued)", yPos)
                    yPos += 6f
                    yPos = drawTableHeader(canvas, yPos)
                }

                // alternating row background
                if (rowIndex % 2 == 0) {
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRect(MARGIN, yPos - 10f, PAGE_W - MARGIN, yPos + 5f, bgPaint)
                }

                val catName = (
                        ExpenseCategory.entries
                            .find { cat -> cat.name == expense.category }
                            ?: ExpenseCategory.OTHER
                        ).name
                    .replace("_"," ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
                val dateStr = rowDateFmt.format(Date(expense.timestamp))
                val title   = truncate(expense.title, 18)
                val mode    = truncate(when { expense.paymentMode.contains("Card",true) -> "Card"; else -> expense.paymentMode }, 12)

                canvas.drawText("${rowIndex + 1}",  COL_NUM,   yPos, tableCellSecPaint)
                canvas.drawText(dateStr,             COL_DATE,  yPos, tableCellSecPaint)
                canvas.drawText(title,               COL_TITLE, yPos, tableCellPaint)
                canvas.drawText(catName,             COL_CAT,   yPos, tableCellSecPaint)
                canvas.drawText(mode,                COL_MODE,  yPos, tableCellSecPaint)
                amtCellPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("$currency ${fmt(expense.amount)}", PAGE_W - MARGIN, yPos, amtCellPaint)
                amtCellPaint.textAlign = Paint.Align.LEFT

                // row bottom divider
                canvas.drawLine(MARGIN, yPos + 6f, PAGE_W - MARGIN, yPos + 6f, dividerPaint)

                grandTotal += expense.amount
                rowIndex++
                yPos += 16f
            }

            // ── GRAND TOTAL ROW ───────────────────────────────────────────────
            if (yPos > FOOTER_Y - 32f) { newPage(); yPos = MARGIN }

            yPos += 6f
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRoundRect(RectF(MARGIN, yPos - 12f, PAGE_W - MARGIN, yPos + 8f), 6f, 6f, bgPaint)
            tableHeaderPaint.textSize = 9f
            canvas.drawText("TOTAL SPENDING", COL_DATE, yPos, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$currency ${fmt(grandTotal)}", PAGE_W - MARGIN, yPos, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT
            tableHeaderPaint.textSize  = 8f

            // ── finalize last page ───────────────────────────────────────────
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
        // thin top line
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

    /** Format a double as a comma-separated integer string, e.g. 12345.6 → "12,346" */
    private fun fmt(value: Double) = String.format("%,.0f", value)

    /** Truncate a string with ellipsis if it exceeds [maxLen] characters */
    private fun truncate(text: String, maxLen: Int) =
        if (text.length <= maxLen) text else text.take(maxLen - 1) + "…"
}