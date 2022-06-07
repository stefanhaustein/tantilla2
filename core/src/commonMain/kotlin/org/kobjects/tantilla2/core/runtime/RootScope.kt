package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.Parameter

object RootScope : Scope(null) {


    override val title: String
        get() = "Root Scope"

    init {

        defineNativeFunction(
            "hsl",
            "Converts the given hue (degree), saturation (0..1) and light (0..1) values to a 32 bit ARGB value (as used in setPixel).",
            org.kobjects.tantilla2.core.runtime.F64, Parameter("h",
                org.kobjects.tantilla2.core.runtime.F64
            ), Parameter("s", org.kobjects.tantilla2.core.runtime.F64), Parameter("l",
                org.kobjects.tantilla2.core.runtime.F64
            )
        ) { hsl(it.f64(0), it.f64(1) , it.f64(2) ) }

        F64.defineNativeFunction(
            "range", "Creates a range from start (inclusive) to end (exclusive)",
            RangeType,
            Parameter("start", F64),
            Parameter("end", F64)
        ) { Range(it.f64(0), it.f64(1)) }


        add(Definition(this, Definition.Kind.SCOPE,"math", resolvedValue = MathScope))

    }



}