package org.kobjects.tantilla2.stdlib.math

import org.kobjects.tantilla2.core.definition.UnitDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.FloatType
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object MathScope : UnitDefinition(null, "math") {

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
    }

}