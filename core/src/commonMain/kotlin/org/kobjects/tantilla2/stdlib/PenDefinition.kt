package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.classifier.NativeScope

class PenDefinition(parent: Scope) : NativeScope("Pen", parent), Type {

    init {
        defineNative(
            "line",
            "Draws a line between the given coordinates",
            Type.Void,
            Parameter("self", this),
            Parameter("x1", Type.F64),
            Parameter("y1", Type.F64),
            Parameter("x2", Type.F64),
            Parameter("y2", Type.F64)
        ) {
            val pen = it[0] as Pen
            pen.drawLine(it.f64(1), it.f64(2), it.f64(3), it.f64(4))
        }
    }

}