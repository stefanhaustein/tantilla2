package org.kobjects.tantilla2.core.runtime

import kotlin.math.abs

fun hsl(h: Double, s: Double, l: Double, a: Double = 1.0): Int {
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
    val r255 = ((r + m) * 255).toInt()
    val g255 = ((g + m) * 255).toInt()
    val b255 = ((b + m) * 255).toInt()
    val a255 = (a * 255).toInt()
    return (a255 shl 24) or (r255 shl 16) or (g255 shl 8) or b255
}