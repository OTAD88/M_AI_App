package com.paperink.pdf.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnotationGeometryTest {
    @Test fun normalizedCoordinatesStayWithinPageBounds() {
        assertEquals(InkPoint(0f, 1f), AnnotationGeometry.normalized(-.2f, 1.7f))
    }

    @Test fun eraserSplitsAFreehandStrokeInsteadOfCoveringPdfContent() {
        val points = (1..9).map { InkPoint(it / 10f, .5f) }
        val stroke = InkObject("stroke", "PEN", points, 0xff000000.toInt(), .01f)
        val fragments = AnnotationGeometry.erase(stroke, listOf(InkPoint(.5f, .5f)), .06f)
        assertEquals(2, fragments.size)
        assertEquals(listOf(points.first(), points[1], points[2]), fragments.first().points)
        assertEquals(listOf(points[6], points[7], points[8]), fragments.last().points)
    }

    @Test fun hitTestFindsStrokeNearSegment() {
        val stroke = InkObject("s", "PEN", listOf(InkPoint(.1f,.5f), InkPoint(.9f,.5f)), 0xff000000.toInt(), .01f)
        assertTrue(AnnotationGeometry.hitTest(stroke, InkPoint(.4f,.51f), .02f))
        assertFalse(AnnotationGeometry.hitTest(stroke, InkPoint(.4f,.8f), .02f))
    }
}
