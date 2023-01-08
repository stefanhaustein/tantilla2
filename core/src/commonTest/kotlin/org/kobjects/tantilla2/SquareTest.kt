package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.testing.TestSystemAbstraction
import kotlin.test.Test
import kotlin.test.assertEquals

class SquareTest() {

    val SQUARE = """
        def square(x: float):
          x * x
    """.trimIndent()

    @Test
    fun testSquare() {
        val userRootScope = TestSystemAbstraction.createScope()
        Parser.parseProgram(SQUARE, userRootScope)

        val squareImpl = userRootScope["square"] as FunctionDefinition

        // assertEquals("def square (x: float):\n  x * x", squareImpl.toString())

        val globalContext = GlobalRuntimeContext(userRootScope)
        globalContext.initialize()
        val runtimeContext = LocalRuntimeContext(globalContext, squareImpl, initializer =  { 4 })

        val result = squareImpl.eval(runtimeContext)

        assertEquals(16.0, result)

    //    assertEquals("def square (x: float):\n  x * x", squareImpl.toString())
    }

}