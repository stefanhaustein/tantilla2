package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType

class GlobalRuntimeContext(
    val userRootScope: UserRootScope,
    // Call with null to clear errors.
    val runStateCallback: (GlobalRuntimeContext) -> Unit
) {
    var stopRequested = false
    var activeThreads = 0
    var exception: TantillaRuntimeException? = null
    val tapListeners = mutableListOf<(Double, Double) -> Unit>()


    fun createException(definition: Definition?, node: Evaluable<LocalRuntimeContext>?, message: String?, cause: Exception? = null) =
        TantillaRuntimeException(
            if (definition == null && node != null) userRootScope.findNode(node) else definition,
            node,
            message,
            cause)

    fun wrapException(e: Exception): TantillaRuntimeException =
        if (e is TantillaRuntimeException) e else createException(null, null, null, e)


    fun onTap(x: Double, y: Double) {
        for (callback in tapListeners) {
            callback(x, y)
        }
    }


    fun run() {
        if (activeThreads != 0) {
            exception = createException(null, null, "Already running.")
            return
        }
        stopRequested = false
        tapListeners.clear()

        val definition = userRootScope["main"]
        if (definition == null) {
            exception = createException(null, null, "main() undefined.")
            runStateCallback(this)
            return
        }
        if (definition.type !is FunctionType) {
            exception = createException(null, null, "main is not a function.")
            runStateCallback(this)
            return
        }
        exception = null
        try {
            val function = definition.getValue(null) as Callable
            userRootScope.initialize(this)
            activeThreads++
            function.eval(LocalRuntimeContext(this, function.scopeSize))
            activeThreads--
            if (activeThreads == 0) {
                runStateCallback(this)
            }
        } catch (e: RuntimeException) {
            activeThreads--
            exception =  wrapException(e)
            runStateCallback(this)
        }
    }


}