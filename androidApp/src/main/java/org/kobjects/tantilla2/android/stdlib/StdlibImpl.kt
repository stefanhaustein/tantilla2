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

fun defineNatives(
    bitmap: android.graphics.Bitmap,
    graphicsUpdateTrigger: MutableState<Int>
): GraphicsSystemImpl {
    val graphicsSystem = GraphicsSystemImpl(bitmap, graphicsUpdateTrigger)
    val graphicsScope = GraphicsScope(graphicsSystem)

    RootScope.add(graphicsScope)

    val canvas = Canvas(bitmap)

    val penImpl = PenImpl(canvas, graphicsUpdateTrigger)
    RootScope.add(
        NativePropertyDefinition(
            RootScope,
            Definition.Kind.STATIC,
            "pen",
            type = graphicsScope.penDefinition,
            getter = {penImpl}
        )
    )
    return graphicsSystem
}