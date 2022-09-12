package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed

interface BitmapImage {
    val width: Int
    val height: Int

    operator fun set(x: Int, y: Int, color: Color)
    operator fun get(x: Int, y: Int): Color

}