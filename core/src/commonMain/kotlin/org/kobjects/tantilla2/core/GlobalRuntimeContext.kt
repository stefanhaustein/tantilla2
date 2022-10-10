package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.parser.Parser

class GlobalRuntimeContext(
    val userRootScope: UserRootScope,
    // Call with null to clear errors.

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
            userRootScope.parentScope.runStateCallback(this)
            return
        }

        if (definition.errors.isNotEmpty()) {
            exception = wrapException(definition.errors[0])
            userRootScope.parentScope.runStateCallback(this)
            return
        }

        if (definition.type !is FunctionType) {
            exception = createException(null, null, "main is not a function.")
            userRootScope.parentScope.runStateCallback(this)
            return
        }
        exception = null
        try {
            val function = definition.getValue(null) as Callable
            launch {
                userRootScope.initialize(this)
                function.eval(LocalRuntimeContext(this, function.scopeSize))
            }
        } catch (e: RuntimeException) {
            exception = wrapException(e)
        }
    }

    private fun launch(task: () -> Unit) {
        userRootScope.parentScope.systemAbstraction.launch {
            activeThreads++
            try {
                task()
            } catch (e: Exception) {
                exception = wrapException(e)
            } finally {
                activeThreads--
                if (activeThreads == 0) {
                    userRootScope.parentScope.runStateCallback(this)
                }
            }
        }
    }


    fun processShellInput(line: String) {
        try {
            var parsed = Parser.parseShellInput(line, userRootScope)
            println("parsed: $parsed")
            userRootScope.resolveAll(CompilationResults())
            println("resolved: $parsed")

            val runtimeContext = LocalRuntimeContext(this)
            launch {
                try {
                    val evaluationResult = parsed.eval(runtimeContext)
                    userRootScope.parentScope.systemAbstraction.write(if (evaluationResult == null || evaluationResult == Unit) "Ok" else evaluationResult.toString())
                } catch (e: Exception) {
                    val message = e.message ?: e.toString()
                    userRootScope.parentScope.systemAbstraction.write(message)
                    exception = if (e is TantillaRuntimeException) e else createException(
                        null,
                        parsed,
                        message,
                        e
                    )
                }
            }
        } catch (e: Exception) {
            val message = e.message ?: e.toString()
            userRootScope.parentScope.systemAbstraction.write(message)
        }
    }


}