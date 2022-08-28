package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.*

object F64 : NativeStructDefinition(RootScope, "float"), Type {

    init {
        defineMethod(
            "abs", "Return the absolute value.",
            F64
        ) { abs(it.f64(0)) }

        defineMethod(
            "int", "Convert to an integer, truncating towards 0.",
            F64,
        ) { it.f64(0).toInt() }

        defineMethod(
            "max", "Returns the maximum of two values.",
            F64, Parameter("other", F64)
        ) { max(it.f64(0), it.f64(1)) }

        defineMethod(
            "min", "Returns the minimum of two values.",
            F64, Parameter("other", F64)
        ) { min(it.f64(0), it.f64(1)) }

        defineMethod(
            "pow", "Calculates the powet ot the given exponent.",
            F64,
            Parameter("exp", F64)
        ) { exp(it.f64(1)  * ln(it.f64(0))) }

        defineNativeFunction(
            "round", "Return the argument, rounded to the next integer.",
            F64, Parameter("x",
                F64
            ), Parameter("exp", F64)
        ) { round(it.f64(0)) }

        defineNativeFunction(
            "sqrt", "Calculates the square root of the argument",
            F64, Parameter("x",
                F64
            )
        ) { sqrt(it.f64(0)) }


    }


}