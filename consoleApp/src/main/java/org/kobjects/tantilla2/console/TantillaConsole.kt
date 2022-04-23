package org.kobjects.tantilla2.console

import java.io.BufferedReader
import java.io.InputStreamReader

import org.kobjects.tantilla2.parser.Parser
import org.kobjects.tantilla2.core.RootScope
import org.kobjects.tantilla2.core.RuntimeContext

// ./gradlew consoleApp:run -q --console=plain

fun main(args : Array<String>) {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    val parsingContext = RootScope()

    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null) {
            break
        }
        var parsed = Parser.parse(line, parsingContext)
        println("parsed: $parsed")
        println("context: $parsingContext")

        val runtimeContext = RuntimeContext(mutableListOf<Any?>())
        val evaluationResult = parsed.eval(runtimeContext)
        println("eval result: $evaluationResult")
    }
}