package com.paperink.pdf.data

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.hypot

data class InkPoint(val x: Float, val y: Float)

data class InkObject(
    val id: String,
    val kind: String,
    val points: List<InkPoint>,
    val color: Int,
    val width: Float,
    val alpha: Float = 1f,
    val note: String = ""
) {
    fun toJson(): String = JSONObject().apply {
        put("id", id); put("kind", kind); put("color", color); put("width", width)
        put("alpha", alpha); put("note", note)
        put("points", JSONArray().also { array -> points.forEach { p ->
            array.put(JSONObject().put("x", p.x.toDouble()).put("y", p.y.toDouble()))
        } })
    }.toString()

    companion object {
        fun fromJson(raw: String): InkObject {
            val json = JSONObject(raw)
            val pointsJson = json.getJSONArray("points")
            val points = (0 until pointsJson.length()).map { i ->
                val p = pointsJson.getJSONObject(i)
                InkPoint(p.getDouble("x").toFloat(), p.getDouble("y").toFloat())
            }
            return InkObject(
                id = json.getString("id"), kind = json.getString("kind"), points = points,
                color = json.getInt("color"), width = json.getDouble("width").toFloat(),
                alpha = json.optDouble("alpha", 1.0).toFloat(), note = json.optString("note")
            )
        }
    }
}

object AnnotationGeometry {
    fun normalized(x: Float, y: Float) = InkPoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))

    fun hitTest(stroke: InkObject, point: InkPoint, tolerance: Float): Boolean {
        if (stroke.points.isEmpty()) return false
        if (stroke.points.size == 1) {
            val p = stroke.points.first()
            return hypot((p.x - point.x).toDouble(), (p.y - point.y).toDouble()) <= tolerance
        }
        return stroke.points.zipWithNext().any { (a, b) ->
            distanceToSegment(point, a, b) <= tolerance
        }
    }

    private fun distanceToSegment(p: InkPoint, a: InkPoint, b: InkPoint): Double {
        val dx = (b.x - a.x).toDouble()
        val dy = (b.y - a.y).toDouble()
        if (dx == 0.0 && dy == 0.0) return hypot((p.x-a.x).toDouble(), (p.y-a.y).toDouble())
        val t = (((p.x-a.x)*dx + (p.y-a.y)*dy) / (dx*dx + dy*dy)).coerceIn(0.0, 1.0)
        return hypot(p.x - (a.x + t*dx).toFloat().toDouble(), p.y - (a.y + t*dy).toFloat().toDouble())
    }
}
