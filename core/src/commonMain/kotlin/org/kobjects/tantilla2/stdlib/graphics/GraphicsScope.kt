package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.UnitScope
import org.kobjects.tantilla2.core.builtin.RootScope

object GraphicsScope: UnitScope(RootScope, "graphics") {

    fun register() {
        add(PenDefinition)
        add(ColorDefinition)
        add(BitmapImageDefinition)
    }
}