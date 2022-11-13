package org.kobjects.tantilla2.stdlib.graphics

interface BitmapImage {
    val width: Int
    val height: Int

    operator fun set(x: Int, y: Int, color: Color)
    operator fun get(x: Int, y: Int): Color

}