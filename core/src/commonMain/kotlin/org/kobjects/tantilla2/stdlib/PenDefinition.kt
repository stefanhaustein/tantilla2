package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.classifier.NativeClassDefinition
import org.kobjects.tantilla2.core.runtime.F64
import org.kobjects.tantilla2.core.runtime.Void

object PenDefinition : NativeClassDefinition("Pen"), Type {

    init {
        defineNativeFunction(
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
            pen.line(it.f64(1), it.f64(2), it.f64(3), it.f64(4))
        }

        defineNativeProperty(
            "fill_color",
        "Color value used for filling shapes",
            ColorDefinition,
            { (it[0] as Pen).fillColor },
            {
                (it[0] as Pen).fillColor = it[1] as Color
                null }
        )

        defineNativeProperty(
            "stroke_color",
            "Color value used for drawing lines shapes",
            ColorDefinition,
            { (it[0] as Pen).strokeColor },
            {
                (it[0] as Pen).strokeColor = it[1] as Color
                null }
        )
    }

}