package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed

data class Color(
    val r: Double,
    val g: Double,
    val b: Double,
    val a: Double,
) : Typed {
    fun plus(other: Color) = Color(r + other.r, g + other.g, b + other.b, a + other.a)
    fun scale(scale: Double) = Color(r * scale, g * scale, b * scale, a * scale)
    fun times(other: Color) = Color(r * other.r, g * other.g, b * other.b, a * other.a)
    override val type: Type
        get() = ColorDefinition
}