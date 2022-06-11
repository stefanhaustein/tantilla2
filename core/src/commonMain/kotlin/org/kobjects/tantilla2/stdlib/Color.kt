package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

fun clamp255(value: Double) = round(max(min(value, 1.0),0.0) * 255).toInt()

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
    val argb = (clamp255(a) shl 24) or (clamp255(r) shl 16) or (clamp255(g) shl 8) or clamp255(b)
}