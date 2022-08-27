package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed
import kotlin.math.abs
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

    companion object {
        fun argb(argb: Int): Color = Color(
            ((argb shr 16) and 255) / 255.0,
            ((argb shr 8) and 255) / 255.0,
            ((argb) and 255) / 255.0,
            ((argb shr 24) and 255) / 255.0,
        )

        fun hsl(h: Double, s: Double, l: Double, a: Double = 1.0): Color {
            val c = (1.0 - abs(2*l - 1)) * s
            val h2 = h/60
            val x = c * (1.0 - abs(h2 % 2 - 1))
            val r: Double
            var g: Double
            val b: Double
            if (h2 < 1) {
                r = c
                g = x
                b = 0.0
            } else if (h2 < 2) {
                r = x
                g = c
                b = 0.0
            } else if (h2 < 3) {
                r = 0.0
                g = c
                b = x
            } else if (h2 < 4) {
                r = 0.0
                g = x
                b = c
            } else if (h2 < 5) {
                r = x
                g = 0.0
                b = c
            } else {
                r = c
                g = 0.0
                b = x
            }
            val m = l - c/2
            return Color(r + m, g + m, b + m, a)
        }
    }
}