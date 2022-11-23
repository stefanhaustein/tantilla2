package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class SquareTest() {

    val SQUARE = """
        def square(x: float):
          x * x
    """.trimIndent()

    @Test
    fun testSquare() {
        val context = TestSystemAbstraction.createScope()
        Parser.parse(SQUARE, context)

        val squareImpl = context["square"] as FunctionDefinition

        // assertEquals("def square (x: float):\n  x * x", squareImpl.toString())

        val globalContext = GlobalRuntimeContext(context)
        val runtimeContext = LocalRuntimeContext(globalContext, 1, initializer =  { 4 })
        val result = squareImpl.eval(runtimeContext)

        assertEquals(16.0, result)

    //    assertEquals("def square (x: float):\n  x * x", squareImpl.toString())
    }

}