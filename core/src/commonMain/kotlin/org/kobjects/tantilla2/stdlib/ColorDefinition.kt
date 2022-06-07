package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.NativeClassDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.F64

object ColorDefinition : NativeClassDefinition("Color"), Type {

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
    }

}