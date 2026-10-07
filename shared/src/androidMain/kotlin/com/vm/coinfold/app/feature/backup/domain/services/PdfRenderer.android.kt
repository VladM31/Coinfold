package com.vm.coinfold.app.feature.backup.domain.services

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.vm.coinfold.app.feature.backup.domain.models.ReportDocument
import java.io.ByteArrayOutputStream

actual object PdfRenderer {
    actual val isSupported: Boolean = true

    private const val PAGE_WIDTH = 595 // A4 in points
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val ROW_HEIGHT = 15f

    actual fun render(document: ReportDocument): ByteArray {
        val pdf = PdfDocument()
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8.5f; color = Color.rgb(30, 30, 40) }
        val bold = Paint(text).apply { typeface = Typeface.DEFAULT_BOLD }
        val title = Paint(bold).apply { textSize = 18f; color = Color.rgb(94, 53, 214) }
        val muted = Paint(text).apply { color = Color.rgb(110, 110, 125) }
        val line = Paint().apply { color = Color.rgb(220, 215, 235); strokeWidth = 0.6f }

        val contentWidth = PAGE_WIDTH - 2 * MARGIN
        val totalWeight = document.columns.sumOf { it.weight.toDouble() }.toFloat()
        val widths = document.columns.map { contentWidth * it.weight / totalWeight }

        var pageNumber = 0
        lateinit var page: PdfDocument.Page
        var y = 0f

        fun startPage() {
            pageNumber++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            y = MARGIN
        }

        fun finishPage() {
            page.canvas.drawText(pageNumber.toString(), PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 16f, muted.apply { textAlign = Paint.Align.RIGHT })
            muted.textAlign = Paint.Align.LEFT
            pdf.finishPage(page)
        }

        fun drawRow(cells: List<String>, paint: Paint) {
            var x = MARGIN
            cells.forEachIndexed { index, cell ->
                val width = widths[index] - 6f
                val shown = fit(cell, paint, width)
                val canvas = page.canvas
                if (document.columns[index].alignRight) {
                    canvas.drawText(shown, x + width, y, paint.apply { textAlign = Paint.Align.RIGHT })
                    paint.textAlign = Paint.Align.LEFT
                } else {
                    canvas.drawText(shown, x, y, paint)
                }
                x += widths[index]
            }
        }

        fun drawHeader() {
            drawRow(document.columns.map { it.title }, bold)
            y += 4f
            page.canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, line)
            y += ROW_HEIGHT - 3f
        }

        startPage()
        page.canvas.drawText(document.title, MARGIN, y + 14f, title)
        y += 32f
        page.canvas.drawText(document.subtitle, MARGIN, y, muted)
        y += 20f
        for (summary in document.summaryLines) {
            page.canvas.drawText(summary, MARGIN, y, bold)
            y += ROW_HEIGHT
        }
        y += 10f
        drawHeader()

        for (row in document.rows) {
            if (y > PAGE_HEIGHT - MARGIN - 10f) {
                finishPage()
                startPage()
                drawHeader()
            }
            drawRow(row, text)
            y += ROW_HEIGHT
        }
        finishPage()

        return ByteArrayOutputStream().also { pdf.writeTo(it); pdf.close() }.toByteArray()
    }

    /** Cuts the text with an ellipsis so it fits in [width]. */
    private fun fit(value: String, paint: Paint, width: Float): String {
        if (paint.measureText(value) <= width) return value
        var shown = value
        while (shown.length > 1 && paint.measureText("$shown…") > width) shown = shown.dropLast(1)
        return "$shown…"
    }
}
