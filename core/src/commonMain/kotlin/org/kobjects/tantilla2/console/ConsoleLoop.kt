package org.kobjects.tantilla2.console

import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.UserScope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.Str
import org.kobjects.tantilla2.core.runtime.Void

class ConsoleLoop(
    val konsole: Konsole,
    var scope: UserScope = UserScope(RootScope)
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
        RootScope.defineNative("print",
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
                scope.hasError()
                konsole.write("resolved: $parsed")

                val evaluationResult = parsed.eval(runtimeContext)
                konsole.write("eval result: $evaluationResult")
            } catch (e: Exception) {
                konsole.write(e.toString())
                e.printStackTrace()
            }
        }
    }

}