package org.kobjects.tantilla2.console

import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.RootScope
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.parser.Parser

suspend fun ConsoleLoop(konsole: Konsole, scope: RootScope = RootScope()) {
    val runtimeContext = RuntimeContext(mutableListOf<Any?>())

    while (true) {
        print("> ")
        val line = konsole.read()
        var parsed = Parser.parse(line, scope)
        konsole.write("parsed: $parsed")
        println("scope: $scope")

        val evaluationResult = parsed.eval(runtimeContext)
        konsole.write("eval result: $evaluationResult")
    }

}