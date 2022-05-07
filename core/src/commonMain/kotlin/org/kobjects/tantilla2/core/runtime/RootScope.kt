package org.kobjects.tantilla2.core.runtime

import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.*

class RootScope : Scope(null) {


    override val title: String
        get() = "Root Scope"

    init {
        defineNative(
            "abs", "Return the absolute value of the argument.",
            F64, Parameter("x", F64)
        ) { abs(it.f64(0)) }

        defineNative(
            "bin", "Convert an integer to a binary string prefixed with \"0b\".",
            Str, Parameter("x", F64)
        ) { it.f64(0).toInt().toString(2) }

        defineNative(
            "chr", "Return the string representing the character with the given Unicode code point.",
            Str, Parameter("x", F64)
        ) { it.f64(0).toInt().toChar() }

        defineNative(
            "hex", "Convert an integer to a hexadeximal string prefixed with \"0x\".",
            Str, Parameter("x", F64)
        ) { it.f64(0).toInt().toString(2) }

        defineNative(
            "int", "Convert the given number to an integer, truncating towards 0.",
            F64, Parameter("x", F64)
        ) { it.f64(0).toInt() }

        defineNative(
            "len", "Returns the length of the given String.",
            F64, Parameter("s", Str)
        ) { it.str(0).length }

        defineNative(
            "max", "Returns the maximum of two values.",
            F64, Parameter("a", F64), Parameter("b", F64)
        ) { max(it.f64(0), it.f64(1)) }

        defineNative(
            "min", "Returns the minimum of two values.",
            F64, Parameter("a", F64), Parameter("b", F64)
        ) { min(it.f64(0), it.f64(1)) }

        defineNative(
            "oct", "Convert an integer to an octal string prefixed with \"0o\".",
            Str, Parameter("x", F64)
        ) { (it.f64(0) .toInt().toString(8)) }

        defineNative(
            "pow", "Calculates base to the power exp.",
            F64, Parameter("base", F64), Parameter("exp", F64)
        ) { exp(it.f64(1)  * ln(it.f64(0))) }

        defineNative(
            "round", "Return the argument, rounded to the next integer.",
            F64, Parameter("x", F64), Parameter("exp", F64)
        ) { round(it.f64(0)) }

        defineNative(
            "str", "Converts the given number to a string.",
            Str, Parameter("x", F64)
        ) { it.str(0) }

        defineNative(
            "sqrt", "Calculates the square root of the argument",
            F64, Parameter("x", F64)
            ) { sqrt(it.f64(0)) }

        defineNative(
            "range", "Creates a range from start (inclusive) to end (exclusive)",
            F64, Parameter("start", F64), Parameter("end", F64)
        ) { Range(it.f64(0) , it.f64(1 )) }

        defineNative(
            "hsl",
            "Converts the given hue (degree), saturation (0..1) and light (0..1) values to a 32 bit ARGB value (as used in setPixel).",
            F64, Parameter("h", F64), Parameter("s", F64), Parameter("l", F64)
        ) { hsl(it.f64(0), it.f64(1) , it.f64(2) ).toDouble() }
    }



}