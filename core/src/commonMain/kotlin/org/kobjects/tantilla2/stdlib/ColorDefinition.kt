package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.NativeClassDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.F64

object ColorDefinition : NativeClassDefinition(
    "Color",
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
            "Create a Color instance from the given r, g, b and alpha values",
            ColorDefinition,
            Parameter("r", F64),
            Parameter("g", F64),
            Parameter("b", F64),
            Parameter("a", F64, org.kobjects.greenspun.core.F64.Const(1.0)),
        ) {
            Color(it.f64(0), it.f64(1), it.f64(2), it.f64(3))
        }

        add(Definition(this, Definition.Kind.STATIC_VARIABLE, "BLACK", mutable = false, docString = "Black", resolvedValue = Color(0.0, 0.0, 0.0, 1.0)))
        add(Definition(this, Definition.Kind.STATIC_VARIABLE, "TRANSPARENT", mutable = false, docString = "Black", resolvedValue = Color(0.0, 0.0, 0.0, 0.0)))
        add(Definition(this, Definition.Kind.STATIC_VARIABLE, "WHITE", mutable = false, docString = "White", resolvedValue = Color(1.0, 1.0, 1.0, 1.0)))
        add(Definition(this, Definition.Kind.STATIC_VARIABLE, "GRAY", mutable = false, docString = "50% Gray", resolvedValue = Color(0.5, 0.5, 0.5, 1.0)))

        defineNativeProperty(
            "r",
            "The red component of the color in a range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it[0] as Color).a }
        )
        defineNativeProperty(
            "g",
            "The green component of the color range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it[0] as Color).a }
        )
        defineNativeProperty(
            "b",
            "The blue component of the color in a range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it[0] as Color).a }
        )
        defineNativeProperty(
            "a",
            "The opacity in a range from 0 (fully transparent) to 1 (fully opaque)",
            F64,
            { (it[0] as Color).a }
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