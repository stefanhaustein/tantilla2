package org.kobjects.tantilla2.testing

import org.kobjects.tantilla2.core.GlobalRuntimeContext
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.parser.Parser
import kotlin.test.*


abstract class TantillaTest(val code: String) {


    @Test
    fun runTest() {
        val context = TestSystemAbstraction.createScope()
        Parser.parse(code, context)
        var count = 0
        for (test in context) {
            if (test.name.startsWith("test_") && test is FunctionDefinition) {
                val globalContext = GlobalRuntimeContext(context)
                val localRuntimeContext = LocalRuntimeContext(globalContext, 0)
                count++
                test.eval(localRuntimeContext)
                println("Test passed: ${test.name}")
            }
        }
        if (count == 0) {
            throw IllegalArgumentException("No tests contained in test")
        }
    }

}