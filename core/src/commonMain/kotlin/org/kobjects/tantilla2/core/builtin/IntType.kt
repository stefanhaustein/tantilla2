package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.AnyType
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.max
import kotlin.math.min

object IntType : NativeStructDefinition(
    null,
    "int",
    "64 bit signed integer.",
    ctorParams = listOf(Parameter("value", AnyType, defaultValueExpression = org.kobjects.greenspun.core.I64.Const(0))),
    ctor = {
        val arg = it[0]
        when (arg) {
            is String -> arg.toInt()
            is Number -> arg.toInt()
            else -> throw IllegalArgumentException("Can't convert $arg to an integer.")
        }
    }
) {
    init {
        defineMethod(
            "bin", "Convert an integer to a binary string prefixed with \"0b\".",
            StrType
        ) { it.i64(0).toString(2) }

        defineMethod(
            "chr", "Return the string representing the character with the given Unicode code point.",
            StrType
        ) { it.i64(0).toInt().toChar() }

        defineMethod(
            "hex", "Convert an integer to a hexadeximal string prefixed with \"0x\".",
            StrType
        ) { it.i64(0).toString(2) }

        defineMethod(
            "max", "Returns the maximum of two values.",
            IntType, Parameter("other", IntType)
        ) { max(it.i64(0), it.i64(1)) }

        defineMethod(
            "min", "Returns the minimum of two values.",
            IntType,
            Parameter("other", IntType)
        ) { min(it.i64(0), it.i64(1)) }

        defineMethod(
            "oct", "Convert an integer to an octal string prefixed with \"0o\".",
            StrType,
        ) { (it.i64(0).toString(8)) }

        defineNativeFunction(
            "range", "Creates a range from start (inclusive) to end (exclusive)",
            RangeType,
            Parameter("start", IntType),
            Parameter("end", IntType)
        ) { Range(it.i64(0), it.i64(1)) }
    }



}