package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed

interface Pen : Typed {
    var fillColor: Color
    var strokeColor: Color

    fun drawLine(startX: Double, startY: Double, endX: Double, endY: Double)

    override val type: Type
        get() = PenDefinition
}