package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.type.StrType
import org.kobjects.tantilla2.core.type.VoidType

class PenDefinition(graphicsScope: GraphicsScope) : NativeStructDefinition(graphicsScope, "Pen"),
    Type {

    init {
        defineNativeFunction(
            "line",
            "Draws a line between the given coordinates",
            VoidType,
            Parameter("self", this),
            Parameter("x1", FloatType),
            Parameter("y1", FloatType),
            Parameter("x2", FloatType),
            Parameter("y2", FloatType)
        ) {
            val pen = it[0] as Pen
            pen.line(it.f64(1), it.f64(2), it.f64(3), it.f64(4))
        }

        defineNativeFunction(
            "rect",
            "Draws a rectangle starting at x,y with the given size.",
            VoidType,
            Parameter("self", this),
            Parameter("x", FloatType),
            Parameter("y", FloatType),
            Parameter("width", FloatType),
            Parameter("height", FloatType)
        ) {
            val pen = it[0] as Pen
            pen.rect(it.f64(1), it.f64(2), it.f64(3), it.f64(4))
        }

        defineNativeFunction(
            "circle",
            "Draws a circle with the given center (cx, cy) and radius (r).",
            VoidType,
            Parameter("self", this),
            Parameter("cx", FloatType),
            Parameter("cy", FloatType),
            Parameter("r", FloatType)
        ) {
            val pen = it[0] as Pen
            pen.circle(it.f64(1), it.f64(2), it.f64(3))
        }

        defineNativeFunction(
            "text",
            "Draws text at the given baseline position.",
            VoidType,
            Parameter("self", this),
            Parameter("x", FloatType),
            Parameter("y", FloatType),
            Parameter("text", StrType)
        ) {
            val pen = it[0] as Pen
            pen.text(it.f64(1), it.f64(2), it.str(3))
        }

        defineNativeProperty(
            "fill_color",
        "Color value used for filling shapes",
            graphicsScope.colorDefinition,
            { (it as Pen).fillColor },
            { self, value -> (self as Pen).fillColor = value as Color }
        )

        defineNativeProperty(
            "stroke_color",
            "Color value used for drawing lines shapes",
            graphicsScope.colorDefinition,
            { (it as Pen).strokeColor },
            { self, value -> (self as Pen).strokeColor = value as Color }
        )
    }

}