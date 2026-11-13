package com.paperink.pdf.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.graphics.pdf.PdfRenderer
import org.json.JSONObject
import java.io.OutputStream
import kotlin.math.max

object PdfEngine {
    fun pageCount(resolver: ContentResolver, uri: Uri): Int =
        resolver.openFileDescriptor(uri, "r")!!.use { pfd -> PdfRenderer(pfd).use { it.pageCount } }

    fun renderPage(resolver: ContentResolver, uri: Uri, index: Int, maxDimension: Int = 2400): Bitmap =
        resolver.openFileDescriptor(uri, "r")!!.use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                renderer.openPage(index).use { page ->
                    val factor = minOf(3f, maxDimension.toFloat() / max(page.width, page.height))
                    val width = (page.width * factor).toInt().coerceAtLeast(1)
                    val height = (page.height * factor).toInt().coerceAtLeast(1)
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        val matrix = Matrix().apply { setScale(factor, factor) }
                        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }

    fun export(
        resolver: ContentResolver,
        source: Uri,
        destination: Uri,
        annotations: List<AnnotationRow>
    ) {
        val grouped = annotations.groupBy { it.pageIndex }
        val document = PdfDocument()
        try {
            val count = pageCount(resolver, source)
            for (index in 0 until count) {
                val bitmap = renderPage(resolver, source, index, 3000)
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = document.startPage(pageInfo)
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                grouped[index].orEmpty().forEach { row -> drawAnnotation(page.canvas, bitmap.width, bitmap.height, row.payload) }
                document.finishPage(page)
                bitmap.recycle()
            }
            resolver.openOutputStream(destination, "w")!!.use { out -> document.writeTo(out) }
        } finally {
            document.close()
        }
    }

    private fun drawAnnotation(canvas: Canvas, width: Int, height: Int, raw: String) {
        val json = JSONObject(raw)
        val points = json.getJSONArray("points")
        if (points.length() == 0) return
        val kind = json.getString("kind")
        val color = json.getInt("color")
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = json.optDouble("width", 0.004).toFloat() * width
            alpha = (json.optDouble("alpha", 1.0) * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        fun x(i: Int) = points.getJSONObject(i).getDouble("x").toFloat() * width
        fun y(i: Int) = points.getJSONObject(i).getDouble("y").toFloat() * height
        when (kind) {
            "MARKER" -> {
                val cx = x(0); val cy = y(0)
                val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; alpha = 235; style = Paint.Style.FILL }
                canvas.drawRoundRect(RectF(cx, cy, cx + width * .055f, cy + height * .038f), 5f, 5f, fill)
                canvas.drawRect(cx, cy, cx + width * .055f, cy + height * .007f, Paint().apply { this.color = android.graphics.Color.WHITE; alpha = 100 })
            }
            "RECTANGLE" -> if (points.length() >= 2) canvas.drawRect(x(0), y(0), x(1), y(1), paint)
            "CIRCLE" -> if (points.length() >= 2) {
                val left = minOf(x(0), x(1)); val top = minOf(y(0), y(1))
                canvas.drawOval(RectF(left, top, maxOf(x(0), x(1)), maxOf(y(0), y(1))), paint)
            }
            "LINE", "ARROW" -> if (points.length() >= 2) {
                canvas.drawLine(x(0), y(0), x(1), y(1), paint)
                if (kind == "ARROW") {
                    val angle = kotlin.math.atan2(y(1)-y(0), x(1)-x(0))
                    val head = paint.strokeWidth * 4
                    val path = Path().apply {
                        moveTo(x(1), y(1))
                        lineTo(x(1)-head*kotlin.math.cos(angle-.45f), y(1)-head*kotlin.math.sin(angle-.45f))
                        moveTo(x(1), y(1))
                        lineTo(x(1)-head*kotlin.math.cos(angle+.45f), y(1)-head*kotlin.math.sin(angle+.45f))
                    }
                    canvas.drawPath(path, paint)
                }
            }
            else -> {
                val path = Path().apply { moveTo(x(0), y(0)); for (i in 1 until points.length()) lineTo(x(i), y(i)) }
                if (kind == "HIGHLIGHT") { paint.style = Paint.Style.STROKE; paint.strokeWidth *= 4f }
                canvas.drawPath(path, paint)
            }
        }
    }
}
