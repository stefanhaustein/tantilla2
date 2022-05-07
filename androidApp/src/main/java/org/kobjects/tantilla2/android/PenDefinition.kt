package org.kobjects.tantilla2.android

import android.graphics.Canvas
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.NativeScope

class PenDefinition(parent: Scope) : NativeScope("Pen", parent), Type {

    init {
        defineNative(
            "line",
            "Draws a line between the given coordinates",
            Void,
            Parameter("self", this),
            Parameter("x1", F64),
            Parameter("y1", F64),
            Parameter("x2", F64),
            Parameter("y2", F64)
        ) {
            val pen = it[0] as Pen
            pen.canvas.drawLine(
                it.f64(1).toFloat(),
                it.f64(2).toFloat(),
                it.f64(3).toFloat(),
                it.f64(4).toFloat(), pen.linePaint)
        }
    }

}