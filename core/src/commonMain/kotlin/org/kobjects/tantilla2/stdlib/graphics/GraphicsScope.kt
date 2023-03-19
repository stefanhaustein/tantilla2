package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.scope.UnitScope

class GraphicsScope(val graphicsSystem: GraphicsSystem): UnitScope(null, "graphics") {

    // Doesn't depend on others
    val colorDefinition = ColorDefinition(this)

    val penDefinition = PenDefinition(this)

    val bitmapImageDefinition = BitmapImageDefinition(this)

    init {
        add(penDefinition)
        add(colorDefinition)
        add(bitmapImageDefinition)

    }
}