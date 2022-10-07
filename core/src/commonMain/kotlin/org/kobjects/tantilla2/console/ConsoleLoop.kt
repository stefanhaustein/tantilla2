package org.kobjects.tantilla2.console

import org.kobjects.konsole.Konsole
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.builtin.VoidType
import org.kobjects.tantilla2.stdlib.math.MathScope

class ConsoleLoop(
    val systemAbstraction: SystemAbstraction,
    val konsole: Konsole,
    var endCallback: (GlobalRuntimeContext) -> Unit = {
        if (it.exception != null) {
            konsole.write(it.exception.toString())
        }
    }
) {
    var scope: UserRootScope = UserRootScope(systemAbstraction)
    var globalRuntimeContext = GlobalRuntimeContext(scope, endCallback)
    var runtimeContext = LocalRuntimeContext(globalRuntimeContext)

    init {
        declareNatives()
    }

    fun setUserScope(scope: UserRootScope) {
        this.scope = scope;
        globalRuntimeContext = GlobalRuntimeContext(scope, endCallback)
        runtimeContext = LocalRuntimeContext(globalRuntimeContext)
        declareNatives()
    }

    fun declareNatives() {
        RootScope.add(MathScope)
        RootScope.defineNativeFunction("print",
            "Print the value of the text parameter to the console.",
            VoidType, Parameter("value", AnyType, isVararg = true)) {
            val list = it[0] as List<Any?>
            konsole.write(list.joinToString(" "))
        }
    }

    suspend fun run() {
        while (true) {
            val line = konsole.read()
            globalRuntimeContext.processShellInput(line, endCallback)
        }
    }

}