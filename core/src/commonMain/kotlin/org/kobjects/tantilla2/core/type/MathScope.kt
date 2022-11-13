package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.definition.UnitDefinition
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.ceil
import kotlin.math.floor

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
    }

}