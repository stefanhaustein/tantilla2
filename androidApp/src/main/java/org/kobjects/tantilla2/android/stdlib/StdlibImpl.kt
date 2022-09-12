package org.kobjects.tantilla2.android.model

import android.graphics.Canvas
import android.view.Choreographer
import androidx.compose.runtime.MutableState
import org.kobjects.tantilla2.android.stdlib.BitmapImageImpl
import org.kobjects.tantilla2.android.stdlib.GraphicsSystemImpl
import org.kobjects.tantilla2.android.stdlib.PenImpl
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.builtin.FloatType
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.builtin.VoidType
import org.kobjects.tantilla2.stdlib.graphics.BitmapImageDefinition
import org.kobjects.tantilla2.stdlib.graphics.GraphicsScope
import org.kobjects.tantilla2.stdlib.graphics.GraphicsSystem
import org.kobjects.tantilla2.stdlib.graphics.PenDefinition

fun defineNatives(
    bitmap: android.graphics.Bitmap,
    graphicsUpdateTrigger: MutableState<Int>
): GraphicsSystemImpl {
    val graphicsSystem = GraphicsSystemImpl(bitmap, graphicsUpdateTrigger)
    val graphicsScope = GraphicsScope(graphicsSystem)

    RootScope.add(graphicsScope)

    val canvas = Canvas(bitmap)
    val screen = BitmapImageImpl(bitmap, graphicsUpdateTrigger)

    canvas.translate(bitmap.width / 2f, bitmap.height / 2f)
    canvas.scale(1f, -1f)
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