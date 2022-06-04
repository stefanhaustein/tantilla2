package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.ceil
import kotlin.math.floor

object MathScope : Scope(RootScope), Typed {
    override val title: String
        get() = "math"

    init {
        defineNative(
            "floor",
            "Rounds the argument down to the nearest integer.",
            F64,
            Parameter("x", F64)) {
                floor(it.f64(0))
            }
        defineNative(
            "ceil",
            "Rounds the argument up to the nearest integer.",
            F64,
            Parameter("x", F64)) {
            ceil(it.f64(0))
        }
    }

    override val type = ScopeType(this)

}