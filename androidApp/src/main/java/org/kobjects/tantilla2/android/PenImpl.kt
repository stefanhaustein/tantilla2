package org.kobjects.tantilla2.android

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.stdlib.Pen

class PenImpl(override val type: Type, val canvas: Canvas): Pen {
    val linePaint = Paint()

    init {
        linePaint.color = Color.GRAY
    }

    override fun drawLine(startX: Double, startY: Double, endX: Double, endY: Double) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), linePaint)
    }
}