package org.kobjects.tantilla2.console

import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.CompilationResults
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.TantillaRuntimeException
import org.kobjects.tantilla2.core.UserScope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.Str
import org.kobjects.tantilla2.core.runtime.Void

class ConsoleLoop(
    val konsole: Konsole,
    var scope: UserScope = UserScope(RootScope),
    var errorListener: (TantillaRuntimeException?) -> Unit = {}
) {
    var runtimeContext = RuntimeContext(mutableListOf<Any?>())

    init {
        declareNatives()
    }

    fun setUserScope(scope: UserScope) {
        this.scope = scope;
        runtimeContext = RuntimeContext(mutableListOf<Any?>())
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
                var parsed = Parser.parse(line, scope)
                konsole.write("parsed: $parsed")
                scope.resolveAll(CompilationResults())
                konsole.write("resolved: $parsed")

                try {
                    val evaluationResult = parsed.eval(runtimeContext)
                    konsole.write("eval result: $evaluationResult")
                    errorListener(null)
                } catch (e: Exception) {
                    val message = e.message ?: e.toString()
                    konsole.write(message)
                    errorListener(if (e is TantillaRuntimeException) e else TantillaRuntimeException(parsed, message, e))
                }
            } catch (e: Exception) {
                val message = e.message ?: e.toString()
                konsole.write(message)
            }
        }
    }

}