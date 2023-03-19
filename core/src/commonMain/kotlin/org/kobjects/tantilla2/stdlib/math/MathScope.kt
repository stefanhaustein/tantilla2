package org.kobjects.tantilla2.stdlib.math

import org.kobjects.tantilla2.core.scope.UnitScope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.FloatNode
import org.kobjects.tantilla2.core.type.FloatType
import kotlin.math.*

object MathScope : UnitScope(null, "math") {

    init {
        defineNativeFunction(
            "floor",
            "Rounds the argument down to the nearest integer.",
            FloatType,
            Parameter("x", FloatType)) {
                floor(it.f64(0))
            }
        defineNativeFunction(
            "ceil",
            "Rounds the argument up to the nearest integer.",
            FloatType,
            Parameter("x", FloatType)) {
            ceil(it.f64(0))
        }
        defineNativeFunction(
            "sin",
            "Computes the sine of the argument.",
            FloatType,
            Parameter("x", FloatType)) {
            sin(it.f64(0))
        }
        defineNativeFunction(
            "cos",
            "Computes the cosine of the argument.",
            FloatType,
            Parameter("x", FloatType)) {
            cos(it.f64(0))
        }
        defineNativeFunction(
            "log2",
            "Computes the base 2 logarithm argument.",
            FloatType,
            Parameter("x", FloatType)) {
            log2(it.f64(0))
        }
        defineNativeFunction(
            "log10",
            "Computes the base 10 logarithm argument.",
            FloatType,
            Parameter("x", FloatType)) {
            log10(it.f64(0))
        }
        defineNativeFunction(
            "log",
            "Computes the logarithm of the argument.",
            FloatType,
            Parameter("x", FloatType),
            Parameter("base", FloatType, FloatNode.Const(E))
        ) {
            log(it.f64(0), it.f64(1))
        }
    }

}