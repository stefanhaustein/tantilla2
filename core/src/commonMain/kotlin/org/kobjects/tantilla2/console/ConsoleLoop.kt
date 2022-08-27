package org.kobjects.tantilla2.console

import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.Str
import org.kobjects.tantilla2.core.runtime.Void

class ConsoleLoop(
    val konsole: Konsole,
    var scope: UserRootScope = UserRootScope(RootScope),
    var errorCallback: (TantillaRuntimeException?) -> Unit = {}
) {
    var globalRuntimeContext = GlobalRuntimeContext()
    var runtimeContext = LocalRuntimeContext(globalRuntimeContext)

    init {
        declareNatives()
    }

    fun setUserScope(scope: UserRootScope) {
        this.scope = scope;
        runtimeContext = LocalRuntimeContext(globalRuntimeContext)
        declareNatives()
    }

    fun declareNatives() {
        RootScope.defineNativeFunction("print",
            "Print the value of the text parameter to the console.",
            Void, Parameter("text", Str)) {
            konsole.write(it[0].toString())
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
                    konsole.write(if (evaluationResult == null) "Ok" else evaluationResult.toString())
                    errorCallback(null)
                } catch (e: Exception) {
                    val message = e.message ?: e.toString()
                    konsole.write(message)
                    errorCallback(if (e is TantillaRuntimeException) e else TantillaRuntimeException(parsed, message, e))
                }
            } catch (e: Exception) {
                val message = e.message ?: e.toString()
                konsole.write(message)
            }
        }
    }

}