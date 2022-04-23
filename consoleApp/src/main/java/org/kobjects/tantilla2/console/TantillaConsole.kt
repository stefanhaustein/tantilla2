package org.kobjects.tantilla2.console

import java.io.BufferedReader
import java.io.InputStreamReader

import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.konsole.KonsoleImpl

// ./gradlew consoleApp:run -q --console=plain

suspend fun main(args : Array<String>) {
    val reader = BufferedReader(InputStreamReader(System.`in`))

    val konsole = KonsoleImpl();
    konsole.writeFunction = { println(it) }
    konsole.readFunction = { it(reader.readLine()) }

    ConsoleLoop(konsole)
}

