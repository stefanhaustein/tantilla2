package org.kobjects.tantilla2.console

import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Void
import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser

class ConsoleLoop(
    val konsole: Konsole,
    var scope: RootScope = RootScope()
) {
    var runtimeContext = RuntimeContext(mutableListOf<Any?>())

    init {
        declareNatives()
    }

    fun setRootScope(scope: RootScope) {
        this.scope = scope;
        runtimeContext = RuntimeContext(mutableListOf<Any?>())
        declareNatives()
    }

    fun declareNatives() {
        scope.defineNative("print",
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
                println("scope: $scope")

                while (runtimeContext.variables.size < scope.locals.size) {
                    runtimeContext.variables.add(null)
                }

                val evaluationResult = parsed.eval(runtimeContext)
                konsole.write("eval result: $evaluationResult")
            } catch (e: Exception) {
                konsole.write(e.toString())
                e.printStackTrace()
            }
        }
    }

}