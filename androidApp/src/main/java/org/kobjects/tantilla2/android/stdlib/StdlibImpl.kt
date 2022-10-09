package org.kobjects.tantilla2.android.model

import android.graphics.Canvas
import androidx.compose.runtime.MutableState
import org.kobjects.tantilla2.android.stdlib.BitmapImageImpl
import org.kobjects.tantilla2.android.stdlib.GraphicsSystemImpl
import org.kobjects.tantilla2.android.stdlib.PenImpl
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.stdlib.graphics.GraphicsScope
import org.kobjects.tantilla2.stdlib.graphics.ScreenDefinition

fun defineNatives(
    rootScope: RootScope,
    bitmap: android.graphics.Bitmap,
    graphicsUpdateTrigger: MutableState<Int>
): GraphicsSystemImpl {
    val graphicsSystem = GraphicsSystemImpl(bitmap, graphicsUpdateTrigger)
    val graphicsScope = GraphicsScope(graphicsSystem)

    rootScope.add(graphicsScope)

    val canvas = Canvas(bitmap)

    val penImpl = PenImpl(canvas, graphicsUpdateTrigger)
    rootScope.add(
        NativePropertyDefinition(
            rootScope,
            Definition.Kind.STATIC,
            "pen",
            type = graphicsScope.penDefinition,
            getter = {penImpl}
        )
    )

    rootScope.add(ScreenDefinition(graphicsScope))

    return graphicsSystem
}