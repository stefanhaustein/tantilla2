package org.kobjects.tantilla2.console

import java.io.BufferedReader
import java.io.InputStreamReader

import org.kobjects.tantilla2.core.GlobalRuntimeContext
import org.kobjects.tantilla2.core.system.Lock
import org.kobjects.tantilla2.core.system.SystemAbstraction
import org.kobjects.tantilla2.core.system.ThreadHandle
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

        override fun input(label: String?): String {
            if (label != null) {
                println(label)
            }
            return readln()
        }

    }

    val systemRootScope = SystemRootScope(systemAbstraction)
    val userScope = UserRootScope(systemRootScope)
    val globalRuntimeContext = GlobalRuntimeContext(userScope)

    while (true) {
        val line = readLine() ?: break
        globalRuntimeContext.processShellInput(line)
    }
}

