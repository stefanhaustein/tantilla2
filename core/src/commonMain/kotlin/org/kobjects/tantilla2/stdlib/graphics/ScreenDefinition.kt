package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.UnitScope
import org.kobjects.tantilla2.core.builtin.FloatType
import org.kobjects.tantilla2.core.builtin.IntType
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.builtin.VoidType
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter

class ScreenDefinition(graphicsScope: GraphicsScope) : UnitScope(RootScope, "screen") {

    init {
        add(
            NativePropertyDefinition(
                this,
                Definition.Kind.STATIC,
                "image",
                docString = "Refers to the screen image bitmap.",
                type = graphicsScope.bitmapImageDefinition,
                getter = {graphicsScope.graphicsSystem.image()}
            )
        )

        add(NativePropertyDefinition(
            this,
            Definition.Kind.STATIC,
            "width",
            "The full width of the screen in pixels.",
            IntType,
            getter = { graphicsScope.graphicsSystem.screenWidth.toLong() }))

        add(NativePropertyDefinition(
            this,
            Definition.Kind.STATIC,
            "height",
            "The full height of the screen in pixels.",
            IntType,
            getter = { graphicsScope.graphicsSystem.screenHeight.toLong() }))


        defineNativeFunction(
            "add_tap_listener",
            "Called when the user taps on the screen.",
            VoidType,
            Parameter("callback", FunctionType.Impl(VoidType, listOf(Parameter("x", FloatType), Parameter("y", FloatType))))
        ) { context ->
            context.globalRuntimeContext.tapListeners.add { x, y ->
                context.globalRuntimeContext.activeThreads++
                try {
                    val fn = context[0] as Callable
                    val functionContext = LocalRuntimeContext(
                        context.globalRuntimeContext,
                        fn.scopeSize,
                        closure = fn.closure,
                        initializer = { when (it) {
                            0 -> x
                            1 -> y
                            else -> throw IllegalArgumentException()
                        } }
                    )
                    fn.eval(functionContext)
                } finally {
                    if (--context.globalRuntimeContext.activeThreads <= 0) {
                        context.globalRuntimeContext.runStateCallback(context.globalRuntimeContext)
                    }
                }

            }
        }


        defineNativeFunction(
            "request_animation_frame",
            "Calls the given function before refreshing the screen.",
            VoidType,
            Parameter("callback", FunctionType.Impl(VoidType, emptyList()))
        ) { context ->
            if (!context.globalRuntimeContext.stopRequested) {
                context.globalRuntimeContext.activeThreads++
                graphicsScope.graphicsSystem.requestAnimationFrame {
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

}