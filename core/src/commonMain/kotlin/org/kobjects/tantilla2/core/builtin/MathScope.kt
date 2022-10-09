package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.builtin.FloatType
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.ceil
import kotlin.math.floor

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
    }

}