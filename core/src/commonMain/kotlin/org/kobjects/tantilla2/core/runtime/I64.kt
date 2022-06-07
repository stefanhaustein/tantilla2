package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.classifier.NativeClassDefinition
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.max
import kotlin.math.min

object I64 : NativeClassDefinition("int") {

    init {
        defineMethod(
            "bin", "Convert an integer to a binary string prefixed with \"0b\".",
            Str
        ) { it.i64(0).toString(2) }

        defineMethod(
            "chr", "Return the string representing the character with the given Unicode code point.",
            Str
        ) { it.i64(0).toInt().toChar() }

        defineMethod(
            "hex", "Convert an integer to a hexadeximal string prefixed with \"0x\".",
            Str
        ) { it.i64(0).toString(2) }

        defineNativeFunction(
            "max", "Returns the maximum of two values.",
            I64, Parameter("other", I64)
        ) { max(it.i64(0), it.i64(1)) }

        defineNativeFunction(
            "min", "Returns the minimum of two values.",
            I64,
            Parameter("other", I64)
        ) { min(it.i64(0), it.i64(1)) }

        defineMethod(
            "oct", "Convert an integer to an octal string prefixed with \"0o\".",
            Str,
        ) { (it.i64(0).toString(8)) }

        defineMethod(
            "str",
            "Converts the given number to a string.",
            Str
        ) { it.i64(0).toString() }


    }



}