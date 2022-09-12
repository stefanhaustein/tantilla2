package org.kobjects.tantilla2.console

import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.builtin.VoidType
import org.kobjects.tantilla2.stdlib.math.MathScope

class ConsoleLoop(
    val konsole: Konsole,
    var scope: UserRootScope = UserRootScope(RootScope),
    var endCallback: (GlobalRuntimeContext) -> Unit = {
        if (it.exception != null) {
            konsole.write(it.exception.toString())
        }
    }
) {
    var globalRuntimeContext = GlobalRuntimeContext(scope, endCallback)
    var runtimeContext = LocalRuntimeContext(globalRuntimeContext)

    init {
        declareNatives()
    }

    fun setUserScope(scope: UserRootScope) {
        this.scope = scope;
        globalRuntimeContext = GlobalRuntimeContext(scope, endCallback)
        runtimeContext = LocalRuntimeContext(globalRuntimeContext)
        declareNatives()
    }

    fun declareNatives() {
        RootScope.add(MathScope)
        RootScope.defineNativeFunction("print",
            "Print the value of the text parameter to the console.",
            VoidType, Parameter("value", AnyType, isVararg = true)) {
            val list = it[0] as List<Any?>
            konsole.write(list.joinToString(" "))
        }
    }

    suspend fun run() {
        while (true) {
            val line = konsole.read()
            try {
                var parsed = Parser.parseShellInput(line, scope)
                println("parsed: $parsed")
                scope.resolveAll(CompilationResults())
                println("resolved: $parsed")

                try {
                    val evaluationResult = parsed.eval(runtimeContext)
                    konsole.write(if (evaluationResult == null || evaluationResult == Unit) "Ok" else evaluationResult.toString())
                    endCallback(globalRuntimeContext)
                } catch (e: Exception) {
                    val message = e.message ?: e.toString()
                    konsole.write(message)
                    globalRuntimeContext.exception = if (e is TantillaRuntimeException) e else globalRuntimeContext.createException(null, parsed, message, e)
                    endCallback(globalRuntimeContext)
                }
            } catch (e: Exception) {
                val message = e.message ?: e.toString()
                konsole.write(message)
            }
        }
    }

}