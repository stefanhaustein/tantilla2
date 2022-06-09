package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Typed

interface Pen : Typed {
    var fillColor: Color
    var strokeColor: Color

    fun line(startX: Double, startY: Double, endX: Double, endY: Double)
}