package org.kobjects.tantilla2.console

import java.io.BufferedReader
import java.io.InputStreamReader

import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.konsole.KonsoleImpl
import org.kobjects.tantilla2.core.GlobalRuntimeContext
import org.kobjects.tantilla2.core.Lock
import org.kobjects.tantilla2.core.SystemAbstraction
import org.kobjects.tantilla2.core.UserRootScope
import org.kobjects.tantilla2.core.builtin.RootScope

// ./gradlew consoleApp:run -q --console=plain

fun main(args : Array<String>) {

    val systemAbstraction = object : SystemAbstraction {
        override fun write(s: String) {
            println(s)
        }

        override fun launch(task: () -> Unit) {
            task()
        }

        override fun createLock(): Lock {
            return object : Lock {
                override fun guard(task: () -> Unit) {
                    task()
                }
            }
        }
    }

    val rootScope = RootScope(systemAbstraction)
    val userScope = UserRootScope(rootScope)
    val globalRuntimeContext = GlobalRuntimeContext(userScope)

    val reader = BufferedReader(InputStreamReader(System.`in`))

    while (true) {
        val line = reader.readLine()
        globalRuntimeContext.processShellInput(line)
    }
}

