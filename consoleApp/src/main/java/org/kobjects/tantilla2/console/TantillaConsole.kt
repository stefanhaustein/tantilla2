package org.kobjects.tantilla2.console

import java.io.BufferedReader
import java.io.InputStreamReader

import org.kobjects.tantilla2.core.GlobalRuntimeContext
import org.kobjects.tantilla2.system.Lock
import org.kobjects.tantilla2.system.SystemAbstraction
import org.kobjects.tantilla2.system.ThreadHandle
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.definition.SystemRootScope

// ./gradlew consoleApp:run -q --console=plain

fun main(args : Array<String>) {

    val systemAbstraction = object : SystemAbstraction {
        override fun write(s: String) {
            println(s)
        }

        override fun launch(task: (ThreadHandle) -> Unit): ThreadHandle {
            val handle = object : ThreadHandle {
                override fun cancel() {
                }
            }
            task(handle)
            return handle
        }

        override fun createLock(): Lock {
            return object : Lock {
                override fun guard(task: () -> Unit) {
                    task()
                }
            }
        }

        override fun input() = throw UnsupportedOperationException()

    }

    val systemRootScope = SystemRootScope(systemAbstraction)
    val userScope = UserRootScope(systemRootScope)
    val globalRuntimeContext = GlobalRuntimeContext(userScope)

    val reader = BufferedReader(InputStreamReader(System.`in`))

    while (true) {
        val line = reader.readLine()
        globalRuntimeContext.processShellInput(line)
    }
}

