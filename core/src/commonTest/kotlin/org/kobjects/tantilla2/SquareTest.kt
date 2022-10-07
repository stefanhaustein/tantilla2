package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.builtin.RootScope
import kotlin.test.Test
import kotlin.test.assertEquals

class SquareTest {

    val SQUARE = """
        def square(x: float):
          x * x
    """.trimIndent()

    @Test
    fun testSquare() {
        val context = UserRootScope(TestSystem)
        Parser.parse(SQUARE, context)

        val squareImpl = context["square"]!!

        // assertEquals("def square (x: float):\n  x * x", squareImpl.toString())

        val runtimeContext = LocalRuntimeContext(GlobalRuntimeContext(context))
        val result = (squareImpl.getValue(null)!! as Callable).eval(runtimeContext)

        assertEquals(16.0, result)

    //    assertEquals("def square (x: float):\n  x * x", squareImpl.toString())
    }

}