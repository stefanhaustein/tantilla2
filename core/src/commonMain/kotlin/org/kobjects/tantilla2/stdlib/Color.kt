package org.kobjects.tantilla2.stdlib

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

fun clamp255(value: Double) = round(max(min(value, 1.0),0.0) * 255).toInt()

data class Color(
    val r: Double,
    val g: Double,
    val b: Double,
    val a: Double,
) {
    val argb = (clamp255(a) shl 24) or (clamp255(r) shl 16) or (clamp255(g) shl 8) or clamp255(b)
}