package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.builtin.F64
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.builtin.Void

object PenDefinition : NativeStructDefinition(GraphicsScope, "Pen"), Type {

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

        defineNativeFunction(
            "rect",
            "Draws a rectangle starting at x,y with the given size.",
            Void,
            Parameter("self", this),
            Parameter("x", F64),
            Parameter("y", F64),
            Parameter("width", F64),
            Parameter("height", F64)
        ) {
            val pen = it[0] as Pen
            pen.rect(it.f64(1), it.f64(2), it.f64(3), it.f64(4))
        }

        defineNativeProperty(
            "fill_color",
        "Color value used for filling shapes",
            ColorDefinition,
            { (it as Pen).fillColor },
            { self, value -> (self as Pen).fillColor = value as Color }
        )

        defineNativeProperty(
            "stroke_color",
            "Color value used for drawing lines shapes",
            ColorDefinition,
            { (it as Pen).strokeColor },
            { self, value -> (self as Pen).strokeColor = value as Color }
        )
    }

}