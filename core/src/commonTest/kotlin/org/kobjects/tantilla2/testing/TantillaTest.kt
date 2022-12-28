package org.kobjects.tantilla2.testing

import org.kobjects.tantilla2.core.GlobalRuntimeContext
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.parser.Parser
import kotlin.test.*


abstract class TantillaTest(val code: String) {

    @Test
    fun runTest() {
        val testRootScope = TestSystemAbstraction.createScope()
        Parser.parseProgram(code, testRootScope)
        var count = 0

        testRootScope.rebuild()
        val globalContext = GlobalRuntimeContext(testRootScope)
        globalContext.initialize()    /*val startIndex = if (incremental) initializedTo else 0
        globalRuntimeContext.staticVariableValues.setSize(staticFieldDefinitions.size)
        for (index in startIndex until staticFieldDefinitions.size) {
            staticFieldDefinitions[index]?.initialize(globalRuntimeContext.staticVariableValues)
        }
        initializedTo = staticFieldDefinitions.size*/

        for (test in testRootScope) {
            if (test.name.startsWith("test_") && test is FunctionDefinition) {
                val localRuntimeContext = LocalRuntimeContext(globalContext, test)
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