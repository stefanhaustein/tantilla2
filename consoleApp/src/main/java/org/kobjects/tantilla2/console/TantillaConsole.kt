package org.kobjects.tantilla2.console

import java.io.BufferedReader
import java.io.InputStreamReader

import org.kobjects.tantilla2.core.ParsingContext
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.parser.Parser

// ./gradlew consoleApp:run -q --console=plain

fun main(args : Array<String>) {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    val parsingContext = ParsingContext(null)

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