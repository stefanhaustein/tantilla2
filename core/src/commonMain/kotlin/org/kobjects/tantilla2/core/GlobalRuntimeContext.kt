package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.control.TantillaRuntimeException
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.ContextOwner
import org.kobjects.tantilla2.core.scope.UserRootScope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LambdaScope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.system.ThreadHandle

class GlobalRuntimeContext(
    val userRootScope: UserRootScope,
    // Call with null to clear errors.

) {
    var initializedTo = 0
    var stopRequested = false
    var activeThreads = mutableSetOf<ThreadHandle>()
    var exception: TantillaRuntimeException? = null
    val tapListeners = mutableListOf<(Double, Double) -> Unit>()
    val staticVariableValues = LocalRuntimeContext(this, userRootScope)


    fun createException(definition: Definition?, node: Node?, message: String?, cause: Throwable? = null) =
        TantillaRuntimeException(
            if (definition == null && node != null) userRootScope.findNode(node) else definition,
            node,
            message,
            cause)

    fun ensureTantillaRuntimeException(e: Exception, definition: Definition? = null, node: Node? = null): TantillaRuntimeException =
        if (e is TantillaRuntimeException) e else createException(definition, node, null, e)


    fun onTap(x: Double, y: Double) {
        for (callback in tapListeners) {
            callback(x, y)
        }
    }


    fun run(calledFromCode: Boolean = false) {
        if (activeThreads.isNotEmpty() && !calledFromCode) {
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

        if (userRootScope.definitionsWithErrors.contains(definition)) {
            exception = createException(definition, null, "Code contains errors.")
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
            if (calledFromCode) {
                initialize()
                function.eval(LocalRuntimeContext(this, function))
            } else {
                launch {
                    this.initialize()
                    function.eval(LocalRuntimeContext(this, function))
                }
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
            exception = ensureTantillaRuntimeException(e, definition)
        }
    }

    private fun launch(task: () -> Unit) {
        userRootScope.parentScope.systemAbstraction.launch {
            activeThreads.add(it)
            try {
                task()
            } catch (e: Exception) {
                e.printStackTrace()
                exception = ensureTantillaRuntimeException(e)
            } finally {
                activeThreads.remove(it)
                if (activeThreads.isEmpty()) {
                    userRootScope.parentScope.runStateCallback(this)
                }
            }
        }
    }


    fun processShellInput(line: String) {
        try {
            val localScope = LambdaScope(userRootScope)
            val parsed = Parser.parseShellInput(line, localScope, userRootScope)
            println("parsed: $parsed")
            userRootScope.rebuild()
            println("resolved: $parsed")
            initialize(incremental = true)

            val runtimeContext = LocalRuntimeContext(this, object : ContextOwner {
                override val dynamicScopeSize: Int
                    get() = localScope.locals.size

            })

            val evaluationResult = parsed.eval(runtimeContext)
            userRootScope.parentScope.systemAbstraction.write(if (evaluationResult == null || evaluationResult == Unit) "Ok" else evaluationResult.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            val message = e.message ?: e.toString()
            userRootScope.parentScope.systemAbstraction.write(message)
        }
    }


    fun initialize(incremental: Boolean = false) {
        val startIndex = if (incremental) initializedTo else 0
        staticVariableValues.setSize(userRootScope.staticFieldDefinitions.size)
        for (index in startIndex until userRootScope.staticFieldDefinitions.size) {
            userRootScope.staticFieldDefinitions[index]?.initialize(staticVariableValues)
        }
        initializedTo = userRootScope.staticFieldDefinitions.size
    }

    fun requestStop() {
        stopRequested = true
        for (handler in activeThreads) {
            handler.cancel()
        }
    }

}