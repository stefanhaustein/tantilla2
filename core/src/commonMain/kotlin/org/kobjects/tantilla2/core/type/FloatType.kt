package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.classifier.NativeTypeDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.As
import org.kobjects.tantilla2.core.node.expression.FloatNode
import kotlin.math.*

object FloatType : NativeTypeDefinition(
    null,
    "Float",
    "Floating point number. The constructor is able to parse strings and to convert ints.",
    /*ctorParams = listOf(Parameter("value", AnyType, defaultValueExpression = FloatNode.Const(0.0))),
    ctor = {
        val arg = it[0]
        when (arg) {
            is String -> arg.toDouble()
            is Number -> arg.toDouble()
            else -> throw IllegalArgumentException("Can't convert $arg to a floating point number.")
        }
    }*/
), Type {

    init {
        defineMethod(
            "abs", "Return the absolute value.",
            FloatType
        ) { abs(it.f64(0)) }
        defineMethod(
            "int", "Return the value truncated to the next integer.",
            IntType
        ) { it.f64(0).toLong() }

        defineMethod(
            "max", "Returns the maximum of two values.",
            FloatType, Parameter("other", FloatType)
        ) { max(it.f64(0), it.f64(1)) }

        defineMethod(
            "min", "Returns the minimum of two values.",
            FloatType, Parameter("other", FloatType)
        ) { min(it.f64(0), it.f64(1)) }

        defineMethod(
            "pow", "Calculates the powet ot the given exponent.",
            FloatType,
            Parameter("exp", FloatType)
        ) { exp(it.f64(1)  * ln(it.f64(0))) }

        defineNativeFunction(
            "round", "Return the argument, rounded to the next integer.",
            FloatType, Parameter("x",
                FloatType
            ), Parameter("exp", FloatType)
        ) { round(it.f64(0)) }

        defineNativeFunction(
            "sqrt", "Calculates the square root of the argument",
            FloatType, Parameter("x",
                FloatType
            )
        ) { sqrt(it.f64(0)) }


    }

    override fun isAssignableFrom(type: Type): Boolean {
        return type == FloatType || type == IntType
    }


}