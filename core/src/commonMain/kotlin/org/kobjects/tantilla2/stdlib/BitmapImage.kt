package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed

interface BitmapImage : Typed {
    val width: Int
    val height: Int

    operator fun set(x: Int, y: Int, color: Color)
    operator fun get(x: Int, y: Int): Color

    override val type: Type
        get() = BitmapImageDefinition
}