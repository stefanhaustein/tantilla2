package org.kobjects.tantilla2.console

import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Void
import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser

suspend fun ConsoleLoop(konsole: Konsole, scope: RootScope = RootScope()) {
    val runtimeContext = RuntimeContext(mutableListOf<Any?>())

    scope.defineNative("print", Void, Parameter("text", Str)) {
        konsole.write(it.variables[0].toString())
    }

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