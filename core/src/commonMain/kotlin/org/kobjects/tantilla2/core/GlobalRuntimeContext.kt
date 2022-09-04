package org.kobjects.tantilla2.core

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


    fun run() {
        if (activeThreads != 0) {
            exception = TantillaRuntimeException(null, null, "Already running.")
            return
        }
        stopRequested = false

        val definition = userRootScope["main"]
        if (definition == null) {
            exception = TantillaRuntimeException(null, null, "main() undefined.")
            runStateCallback(this)
            return
        }
        if (definition.type !is FunctionType) {
            exception = TantillaRuntimeException(null, null, "main is not a function.")
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
        } catch (e: TantillaRuntimeException) {
            activeThreads--
            exception = e
            runStateCallback(this)
        } catch (e: RuntimeException) {
            activeThreads--
            exception =  TantillaRuntimeException(definition, null, e.message ?: e.toString(), e)
            runStateCallback(this)
        }
    }

}