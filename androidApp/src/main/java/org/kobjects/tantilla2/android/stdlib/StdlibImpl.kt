package org.kobjects.tantilla2.android.model

import android.graphics.Canvas
import androidx.compose.runtime.MutableState
import org.kobjects.tantilla2.android.stdlib.GraphicsSystemImpl
import org.kobjects.tantilla2.android.stdlib.PenImpl
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.scope.SystemRootScope
import org.kobjects.tantilla2.stdlib.graphics.GraphicsScope
import org.kobjects.tantilla2.stdlib.graphics.ScreenDefinition

fun defineNatives(
    systemRootScope: SystemRootScope,
    bitmap: android.graphics.Bitmap,
    graphicsUpdateTrigger: MutableState<Int>
): GraphicsSystemImpl {
    val graphicsSystem = GraphicsSystemImpl(bitmap, graphicsUpdateTrigger)
    val graphicsScope = GraphicsScope(graphicsSystem)

    systemRootScope.add(graphicsScope)

    val canvas = Canvas(bitmap)

    val penImpl = PenImpl(canvas, graphicsUpdateTrigger)
    systemRootScope.add(
        NativePropertyDefinition(
            systemRootScope,
            Definition.Kind.STATIC,
            "pen",
            type = graphicsScope.penDefinition,
            getter = {penImpl}
        )
    )

    systemRootScope.add(ScreenDefinition(graphicsScope))

    return graphicsSystem
}