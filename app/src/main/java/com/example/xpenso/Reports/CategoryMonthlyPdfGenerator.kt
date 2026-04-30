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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CategoryPdfReportGenerator {

    // ── consistent color palette ─────────────────────────────────────────────
    private const val COLOR_PRIMARY      =0xFF3949AB.toInt()
    private const val COLOR_TEXT_MAIN    = 0xFF202124.toInt()
    private const val COLOR_TEXT_SEC     = 0xFF5F6368.toInt()
    private const val COLOR_BG_LIGHT     = 0xFFF8F9FA.toInt()

    // ── layout ───────────────────────────────────────────────────────────────
    private const val PAGE_W  = 595
    private const val PAGE_H  = 842
    private const val MARGIN  = 40f
    private val CONTENT_W get() = PAGE_W - MARGIN * 2   // 515

    // ── table columns ────────────────────────────────────────────────────────
    private val COL_DATE = MARGIN + 10f
    private val COL_DESC = MARGIN + 105f
    private val COL_MODE = MARGIN + 360f
    private val COL_AMT  = PAGE_W - MARGIN - 8f

    // ────────────────────────────────────────────────────────────────────────
    fun generateCategoryReport(
        context: Context,
        uri: Uri,
        expenses: List<ExpenseEntity>,
        category: ExpenseCategory,
        currencySymbol: String,
        totalOverall: Double,
        isPremium: Boolean = true,
        month: Int? = null,
        year: Int? = null,
        startOfMonth: Int = 1
    ): Result<Unit> {

        // ── filter to the requested month / year ─────────────────────────────
        val filteredExpenses: List<ExpenseEntity> = if (month != null && year != null) {
            val startCalendar = Calendar.getInstance().apply {
                set(year, month, startOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCalendar = (startCalendar.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }
            expenses.filter {
                it.timestamp in startCalendar.timeInMillis..endCalendar.timeInMillis
            }
        } else {
            expenses
        }

        if (filteredExpenses.isEmpty())
            return Result.failure(Exception("No data for this category in the selected period"))

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
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = COLOR_PRIMARY
            }
            val footerPaint = TextPaint().apply {
                textSize = 8f; textAlign = Paint.Align.CENTER; color = COLOR_TEXT_SEC
            }
            // ── watermark paint (used on every page for non-premium) ──────────
            val watermarkPaint = Paint().apply {
                textSize  = 72f
                color     = Color.parseColor("#BBBBBB")   // visible grey
                alpha     = 60                            // raised from 35 → clearly visible
                textAlign = Paint.Align.CENTER
                typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val dividerPaint = Paint().apply {
                color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f
            }
            val bgPaint = Paint().apply { style = Paint.Style.FILL }

            // ── aggregates (on filtered data) ────────────────────────────────
            val sortedExpenses  = filteredExpenses.sortedByDescending { it.timestamp }
            val totalAmount     = filteredExpenses.sumOf { it.amount }
            val avgExpense      = totalAmount / filteredExpenses.size
            val highestExpense  = filteredExpenses.maxByOrNull { it.amount }
            val lowestExpense   = filteredExpenses.minByOrNull { it.amount }

            val percentValue = if (totalOverall > 0) (totalAmount / totalOverall) * 100 else 0.0
            val shareStr = if (percentValue < 1.0 && percentValue > 0.0)
                String.format("%.2f%%", percentValue)
            else "${percentValue.toInt()}%"

            val modeMap = filteredExpenses.groupBy {
                when {
                    it.paymentMode.contains("Card", true) -> "Card"
                    it.paymentMode.isBlank()              -> "Unknown"
                    else                                  -> it.paymentMode
                }
            }.mapValues { it.value.size }
            val maxMode    = modeMap.maxByOrNull { it.value }
            val modePercent = if (filteredExpenses.isNotEmpty())
                ((maxMode?.value ?: 0) * 100 / filteredExpenses.size) else 0
            val mostUsedMode = "${maxMode?.key ?: "-"} ($modePercent%)"

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val today      = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            val periodText: String = if (month != null && year != null) {
                val monthName = SimpleDateFormat("MMMM", Locale.getDefault())
                    .format(Calendar.getInstance().apply {
                        set(Calendar.MONTH, month)
                        set(Calendar.YEAR, year)
                    }.time)
                "$monthName $year  (from day $startOfMonth)"
            } else {
                val firstDate = filteredExpenses.minOf { it.timestamp }
                val lastDate  = filteredExpenses.maxOf { it.timestamp }
                "${dateFormat.format(Date(firstDate))} – ${dateFormat.format(Date(lastDate))}"
            }

            // ── weekly / monthly trend ────────────────────────────────────────
            val weeklyMap: Map<String, Double> = if (month != null && year != null) {
                filteredExpenses
                    .groupBy { expense ->
                        val cal = Calendar.getInstance().apply { timeInMillis = expense.timestamp }
                        val day = cal.get(Calendar.DAY_OF_MONTH)
                        when {
                            day <= 7  -> "Week 1"
                            day <= 14 -> "Week 2"
                            day <= 21 -> "Week 3"
                            else      -> "Week 4+"
                        }
                    }
                    .mapValues { it.value.sumOf { e -> e.amount } }
            } else {
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                filteredExpenses
                    .groupBy { monthFormat.format(Date(it.timestamp)) }
                    .mapValues { it.value.sumOf { e -> e.amount } }
            }

            val trendLabel = if (month != null && year != null) "WEEKLY SPENDING TREND" else "MONTHLY SPENDING TREND"
            val trendKeys  = if (month != null && year != null)
                listOf("Week 1", "Week 2", "Week 3", "Week 4+")
            else
                listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

            val categoryDisplayName = category.name
                .replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

            // ── state ────────────────────────────────────────────────────────
            var pageNumber = 1
            var page       = document.startPage(pageInfo)
            var canvas     = page.canvas

            // ── helpers ──────────────────────────────────────────────────────
            fun newPage() {
                // Draw footer first, then watermark ON TOP as the very last layer
                drawCategoryFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
                drawCategoryWatermark(canvas, PAGE_W, PAGE_H, watermarkPaint, isPremium)
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
                c.drawText("MODE",        COL_MODE, y + 17f, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.RIGHT
                c.drawText("AMOUNT",      COL_AMT,  y + 17f, tableHeaderPaint)
                tableHeaderPaint.textAlign = Paint.Align.LEFT
                return y + 26f
            }

            // ────────────────────────────────────────────────────────────────
            // PAGE 1 – HEADER
            // ────────────────────────────────────────────────────────────────
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 100f, bgPaint)

            canvas.drawText("XPENSO", MARGIN, 60f, brandPaint)

            labelPaint.color = Color.WHITE; labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("$categoryDisplayName – Category Report", MARGIN, 80f, labelPaint)

            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: $today", PAGE_W - MARGIN, 80f, labelPaint)

            labelPaint.textAlign = Paint.Align.LEFT; labelPaint.color = COLOR_TEXT_SEC
            canvas.drawText("Period: $periodText", MARGIN, 115f, labelPaint)

            var yPos = 138f

            // ────────────────────────────────────────────────────────────────
            // CATEGORY SUMMARY  (larger card – 3 even rows, more padding)
            // ────────────────────────────────────────────────────────────────
            yPos = sectionTitle(canvas, "CATEGORY SUMMARY", yPos)
            yPos += 8f   // ← breathing room below section title

            // Increased card height: 3 data rows × 36 px + top/bottom padding 20 px each = 148 px
            val summaryH = 148f
            bgPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(
                RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + summaryH),
                10f, 10f, bgPaint
            )

            val c1 = MARGIN + 18f
            val c2 = MARGIN + 185f
            val c3 = MARGIN + 360f

            // Row 1  (top padding 22 px inside card)
            val r1L = yPos + 22f;  val r1V = yPos + 38f
            // Row 2
            val r2L = yPos + 68f;  val r2V = yPos + 84f
            // Row 3
            val r3L = yPos + 114f; val r3V = yPos + 130f

            // row 1
            canvas.drawText("Total Expenditure",     c1, r1L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(totalAmount)}", c1, r1V, valuePaint)
            canvas.drawText("Transactions",           c2, r1L, labelPaint)
            canvas.drawText("${filteredExpenses.size} Entries", c2, r1V, valuePaint)
            canvas.drawText("Share of All Spending",  c3, r1L, labelPaint)
            canvas.drawText(shareStr,                  c3, r1V, valuePaint)

            // row 2
            canvas.drawText("Average Expense",        c1, r2L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(avgExpense)}", c1, r2V, valuePaint)
            canvas.drawText("Highest Expense",         c2, r2L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(highestExpense?.amount ?: 0.0)}", c2, r2V, valuePaint)
            canvas.drawText("Lowest Expense",          c3, r2L, labelPaint)
            canvas.drawText("$currencySymbol ${fmt(lowestExpense?.amount ?: 0.0)}", c3, r2V, valuePaint)

            // row 3
            canvas.drawText("Most Used Mode",          c1, r3L, labelPaint)
            canvas.drawText(mostUsedMode,               c1, r3V, valuePaint)

            yPos += summaryH + 32f  // ← generous gap before next section

            // ────────────────────────────────────────────────────────────────
            // SPENDING TREND
            // ────────────────────────────────────────────────────────────────
            yPos = sectionTitle(canvas, trendLabel, yPos)
            yPos += 8f

            val maxTrendAmt = weeklyMap.maxOfOrNull { it.value } ?: 1.0
            val barStart    = MARGIN + 52f
            val barMaxW     = CONTENT_W - 52f - 110f

            trendKeys.forEach { key ->
                val amt  = weeklyMap[key] ?: 0.0
                val pct  = (amt / maxTrendAmt).toFloat()
                val barW = (pct * barMaxW).coerceAtLeast(if (amt > 0) 2f else 0f)

                canvas.drawText(key, MARGIN, yPos + 2f, labelPaint)

                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(barStart, yPos - 7f, barStart + barMaxW, yPos + 5f), 3f, 3f, bgPaint)
                if (barW > 0f) {
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(barStart, yPos - 7f, barStart + barW, yPos + 5f), 3f, 3f, bgPaint)
                }
                canvas.drawText(
                    if (amt > 0) "$currencySymbol ${fmt(amt)}" else "–",
                    barStart + barMaxW + 8f, yPos + 2f, labelPaint
                )
                yPos += 18f
            }

            yPos += 20f  // gap after trend chart

            // ────────────────────────────────────────────────────────────────
            // PAYMENT MODE BREAKDOWN
            // ────────────────────────────────────────────────────────────────
            yPos = sectionTitle(canvas, "PAYMENT MODE BREAKDOWN", yPos)
            yPos += 8f

            val modeBlockH   = (modeMap.size * 22f) + 20f
            bgPaint.color    = COLOR_BG_LIGHT
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + modeBlockH), 10f, 10f, bgPaint)

            val modeBarStart = MARGIN + 110f
            val modeBarMaxW  = CONTENT_W - 110f - 110f
            val maxModeCount = modeMap.maxOfOrNull { it.value } ?: 1
            var modeY        = yPos + 18f

            modeMap.toList().sortedByDescending { it.second }.forEach { (modeName, count) ->
                val pct      = count.toFloat() / maxModeCount.toFloat()
                val barW     = (pct * modeBarMaxW).coerceAtLeast(2f)
                val percent = (count.toDouble() * 100.0) / filteredExpenses.size.toDouble()

                val percentLabel = if (percent < 1.0 && percent > 0.0)
                    String.format("%.2f%%", percent)
                else
                    "${percent.toInt()}%"

                val pctLabel = "$percentLabel  ($count txns)"

                canvas.drawText(modeName, MARGIN + 12f, modeY, labelPaint)

                bgPaint.color = 0xFFE3F2FD.toInt()
                canvas.drawRoundRect(RectF(modeBarStart, modeY - 7f, modeBarStart + modeBarMaxW, modeY + 5f), 3f, 3f, bgPaint)
                bgPaint.color = COLOR_PRIMARY
                canvas.drawRoundRect(RectF(modeBarStart, modeY - 7f, modeBarStart + barW, modeY + 5f), 3f, 3f, bgPaint)

                canvas.drawText(pctLabel, modeBarStart + modeBarMaxW + 8f, modeY, labelPaint)
                modeY += 22f
            }

            yPos += modeBlockH + 24f

            // ────────────────────────────────────────────────────────────────
            // TRANSACTIONS TABLE
            // Try to start on page 1 if enough room remains (need ≥ 80 px for
            // the section header + table header + at least one row).
            // If not enough room, open a new page.
            // ────────────────────────────────────────────────────────────────
            val tableNeededToStart = 80f   // min space: section title + table header + 1 row
            if (yPos + tableNeededToStart > PAGE_H - 60f) {
                // Not enough room on page 1 → start fresh
                newPage()
                yPos = MARGIN
            }
            // else: continue on page 1

            yPos = sectionTitle(canvas, "ALL TRANSACTIONS  (${filteredExpenses.size} records)", yPos)
            yPos += 4f
            yPos = drawTableHeader(canvas, yPos)

            val tableDateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            sortedExpenses.forEachIndexed { index, expense ->

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

                val dateStr = tableDateFmt.format(Date(expense.timestamp))
                val mode    = when {
                    expense.paymentMode.isBlank()                      -> "–"
                    expense.paymentMode.contains("Card", true)         -> "Card"
                    else                                               -> expense.paymentMode
                }
                val title = if (expense.title.length > 30) expense.title.take(27) + "…" else expense.title

                canvas.drawText(dateStr,                           COL_DATE, yPos + 15f, rowSecPaint)
                canvas.drawText(title,                             COL_DESC, yPos + 15f, rowPaint)
                canvas.drawText(mode,                              COL_MODE, yPos + 15f, rowSecPaint)
                canvas.drawText("$currencySymbol ${fmt(expense.amount)}", COL_AMT, yPos + 15f, amountPaint)

                canvas.drawLine(MARGIN, yPos + 24f, PAGE_W - MARGIN, yPos + 24f, dividerPaint)
                yPos += 24f
            }

            // ── grand total row ───────────────────────────────────────────────
            if (yPos > PAGE_H - 50f) { newPage(); yPos = MARGIN }
            yPos += 4f
            bgPaint.color = COLOR_PRIMARY
            canvas.drawRoundRect(RectF(MARGIN, yPos, PAGE_W - MARGIN, yPos + 26f), 6f, 6f, bgPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("TOTAL  ($categoryDisplayName)", COL_DESC, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$currencySymbol ${fmt(totalAmount)}", COL_AMT, yPos + 17f, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT

            // ── finalize last page ────────────────────────────────────────────
            // footer first, then watermark as the very last draw call so it
            // sits on top of all content and cannot be covered
            drawCategoryFooter(canvas, pageNumber, PAGE_W, PAGE_H, footerPaint)
            drawCategoryWatermark(canvas, PAGE_W, PAGE_H, watermarkPaint, isPremium)
            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            document.close()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── footer ───────────────────────────────────────────────────────────────
    private fun drawCategoryFooter(
        canvas: Canvas, pageNum: Int, width: Int, height: Int, paint: Paint
    ) {
        val linePaint = Paint().apply { color = 0xFFE0E0E0.toInt(); strokeWidth = 0.7f }
        canvas.drawLine(40f, height - 44f, width - 40f, height - 44f, linePaint)
        canvas.drawText(
            "Xpenso  •  Category Report  •  Page $pageNum",
            width / 2f, height - 30f, paint
        )
    }

    // ── watermark (shown on every page for non-premium users) ────────────────
    private fun drawCategoryWatermark(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        paint: Paint,
        isPremium: Boolean
    ) {
        if (isPremium) return   // ← premium users see no watermark

        canvas.save()
        canvas.rotate(-30f, pageWidth / 2f, pageHeight / 2f)
        canvas.drawText("XPENSO", pageWidth / 2f, pageHeight / 2f, paint)
        canvas.restore()
    }

    private fun fmt(value: Double): String = String.format("%,.0f", value)
}