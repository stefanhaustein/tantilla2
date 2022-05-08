package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Typed

interface Pen : Typed {
    fun drawLine(startX: Double, startY: Double, endX: Double, endY: Double)
}