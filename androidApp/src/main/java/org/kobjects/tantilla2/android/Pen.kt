package org.kobjects.tantilla2.android

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Pen(val canvas: Canvas) {
    val linePaint = Paint()

    init {
        linePaint.color = Color.GRAY
    }
}