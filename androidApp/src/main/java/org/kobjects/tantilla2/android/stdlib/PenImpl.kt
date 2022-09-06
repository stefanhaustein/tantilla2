package org.kobjects.tantilla2.android.stdlib

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.MutableState
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.stdlib.graphics.Pen

class PenImpl(override val type: Type, val canvas: Canvas, val updateTrigger: MutableState<Int>):
    Pen {
    val strokePaint = Paint()
    val fillPaint = Paint()

    init {
        strokePaint.color = Color.GRAY
        strokePaint.style = Paint.Style.STROKE

        fillPaint.color = Color.TRANSPARENT
        fillPaint.style = Paint.Style.FILL
    }

    override var fillColor = org.kobjects.tantilla2.stdlib.graphics.Color(0.0, 0.0, 0.0, 0.0)
        set(value) {
            field = value
            fillPaint.color = value.argb
        }

    override var strokeColor = org.kobjects.tantilla2.stdlib.graphics.Color(0.5, 0.5, 0.5, 1.0)
        set(value) {
            field = value
            strokePaint.color = value.argb
        }

    override fun line(startX: Double, startY: Double, endX: Double, endY: Double) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), strokePaint)
        updateTrigger.value++
    }

    override fun rect(x: Double, y: Double, width: Double, height: Double) {
        if (fillPaint.color shr 24 != 0) {
            canvas.drawRect(x.toFloat(), y.toFloat(), (x+width).toFloat(), (y+height).toFloat(), fillPaint)
        }

        if (strokePaint.color shr 24 != 0) {
            canvas.drawRect(x.toFloat(), y.toFloat(), (x+width).toFloat(), (y+height).toFloat(), strokePaint)
        }
        updateTrigger.value++
    }

}