package org.kobjects.tantilla2.android.model

import android.graphics.Canvas
import android.view.Choreographer
import androidx.compose.runtime.MutableState
import org.kobjects.tantilla2.android.stdlib.BitmapImageImpl
import org.kobjects.tantilla2.android.stdlib.PenImpl
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.builtin.F64
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.builtin.Void
import org.kobjects.tantilla2.stdlib.graphics.BitmapImageDefinition
import org.kobjects.tantilla2.stdlib.graphics.PenDefinition

fun defineNatives(bitmap: android.graphics.Bitmap, graphicsUpdateTrigger: MutableState<Int>) {
    RootScope.defineNativeFunction(
        "setPixel",
        "Sets the pixel at the given x/y coordinate to the given 32bit color value in ARGB format.",
        Void,
        Parameter("x", F64),
        Parameter("y", F64),
        Parameter("color", F64)
    ) {
        bitmap.setPixel(
            (it.variables[0] as Double).toInt(),
            (it.variables[1] as Double).toInt(),
            (it.variables[2] as Double).toInt())
    }

    val canvas = Canvas(bitmap)
    val screen = BitmapImageImpl(bitmap, graphicsUpdateTrigger)

    canvas.translate(bitmap.width / 2f, bitmap.height / 2f)
    canvas.scale(1f, -1f)
    val penImpl = PenImpl(PenDefinition, canvas, graphicsUpdateTrigger)
    RootScope.add(
        NativePropertyDefinition(
            RootScope,
            Definition.Kind.STATIC,
            "pen",
            type = PenDefinition,
            getter = {penImpl}
        )
    )
    RootScope.add(
        NativePropertyDefinition(
            RootScope,
            Definition.Kind.STATIC,
            "screen",
            type = BitmapImageDefinition,
            getter = {screen}
        )
    )

    RootScope.defineNativeFunction(
        "requestAnimationFrame",
        "Calls the given function before refreshing the screen.",
        Void,
        Parameter("callback", FunctionType.Impl(Void, emptyList()))
    ) { context ->
        if (!context.globalRuntimeContext.stopRequested) {
            context.globalRuntimeContext.activeThreads++
            Choreographer.getInstance().postFrameCallback {
                try {
                    val fn = context[0] as Callable
                    val functionContext = LocalRuntimeContext(
                        context.globalRuntimeContext,
                        fn.scopeSize,
                        closure = fn.closure
                    )
                    fn.eval(functionContext)
                } finally {
                   if (--context.globalRuntimeContext.activeThreads <= 0) {
                       context.globalRuntimeContext.runStateCallback(context.globalRuntimeContext)
                   }
                }
            }
        }
    }
}