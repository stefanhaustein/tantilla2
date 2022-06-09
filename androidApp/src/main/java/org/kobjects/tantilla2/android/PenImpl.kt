package org.kobjects.tantilla2.android

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.stdlib.Pen

class PenImpl(override val type: Type, val canvas: Canvas): Pen {
    val strokePaint = Paint()
    val fillPaint = Paint()

    init {
        strokePaint.color = Color.GRAY
        fillPaint.color = Color.TRANSPARENT
    }

    override var fillColor = org.kobjects.tantilla2.stdlib.Color(0.0, 0.0, 0.0, 0.0)
        set(value) {
            field = value
            fillPaint.color = value.argb
        }

    override var strokeColor = org.kobjects.tantilla2.stdlib.Color(0.5, 0.5, 0.5, 1.0)
        set(value) {
            field = value
            strokePaint.color = value.argb
        }

    override fun line(startX: Double, startY: Double, endX: Double, endY: Double) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), strokePaint)
    }
}