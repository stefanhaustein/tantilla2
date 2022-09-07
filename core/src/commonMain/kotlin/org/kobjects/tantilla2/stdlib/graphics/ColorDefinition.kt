package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.builtin.F64
import org.kobjects.tantilla2.core.builtin.RootScope

object ColorDefinition : NativeStructDefinition(
    GraphicsScope,
    "Color",
    docString = "Color representation with support for RGB and HSL formats.",
    ctorParams = listOf(
        Parameter("r", F64),
        Parameter("g", F64),
        Parameter("b", F64),
        Parameter("a", F64, org.kobjects.greenspun.core.F64.Const(1.0))
    ),
    ctor = {
        Color(it.f64(0), it.f64(1), it.f64(2), it.f64(3))
    }) {

    init {
        defineNativeFunction(
            "rgb",
            "Create a Color instance from the given r, g, b and (optional) alpha values in the range from 0 to 1",
            ColorDefinition,
            Parameter("r", F64),
            Parameter("g", F64),
            Parameter("b", F64),
            Parameter("a", F64, org.kobjects.greenspun.core.F64.Const(1.0)),
        ) {
            Color(it.f64(0), it.f64(1), it.f64(2), it.f64(3))
        }

        defineNativeFunction(
            "hsl",
            "Converts the given hue (degree), saturation (0..1) and light (0..1) and otptional alpha values to a 32 bit ARGB value (as used in setPixel).",
            F64,
            Parameter("h", F64),
            Parameter("s", F64),
            Parameter("l", F64),
            Parameter("a", F64, org.kobjects.greenspun.core.F64.Const(1.0)),
        ) { Color.hsl(it.f64(0), it.f64(1), it.f64(2)) }

        add(NativePropertyDefinition.constant(this,  "BLACK", docString = "Black", value = Color(0.0, 0.0, 0.0, 1.0)))
        add(NativePropertyDefinition.constant(this, "TRANSPARENT", docString = "Transparent", value  = Color(0.0, 0.0, 0.0, 0.0)))
        add(NativePropertyDefinition.constant(this, "WHITE", docString = "White", value = Color(1.0, 1.0, 1.0, 1.0)))
        add(NativePropertyDefinition.constant(this, "GRAY", docString = "50% Gray", value = Color(0.5, 0.5, 0.5, 1.0)))

        defineNativeProperty(
            "r",
            "The red component of the color in a range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it as Color).a }
        )
        defineNativeProperty(
            "g",
            "The green component of the color range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it as Color).a }
        )
        defineNativeProperty(
            "b",
            "The blue component of the color in a range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it as Color).a }
        )
        defineNativeProperty(
            "a",
            "The opacity in a range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it as Color).a }
        )
        defineNativeFunction(
            "plus",
            "Add this color to another color.",
            this,
            Parameter("self", this),
            Parameter("other", this)
        ) {
              (it[0] as Color).plus(it[1] as Color)
        }
        defineNativeFunction(
            "scale",
            "Scale this color by the given factor.",
            this,
            Parameter("self", this),
            Parameter("factor", F64),
        ) {
            (it[0] as Color).scale(it.f64(1))
        }
        defineNativeFunction(
            "times",
            "Multiply the component values of this color and the given other color.",
            this,
            Parameter("self", this),
            Parameter("other", this)
        ) {
            (it[0] as Color).times(it[1] as Color)
        }
    }

}