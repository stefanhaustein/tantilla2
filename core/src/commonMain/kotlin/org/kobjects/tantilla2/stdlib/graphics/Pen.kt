package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed

interface Pen {
    var fillColor: Color
    var strokeColor: Color

    fun line(startX: Double, startY: Double, endX: Double, endY: Double)
    fun rect(x: Double, y: Double, width: Double, height: Double)
}