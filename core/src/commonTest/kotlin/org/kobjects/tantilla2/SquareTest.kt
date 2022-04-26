package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.RootScope
import kotlin.test.Test
import kotlin.test.assertEquals

class SquareTest {

    val SQUARE = """
        def square(x: float):
          x * x
    """.trimIndent()

    @Test
    fun testSquare() {
        val context = RootScope()
        Parser.parse(SQUARE, context)

        val squareImpl = context.definitions["square"]!!

        // assertEquals("def square (x: float):\n  x * x", squareImpl.toString())

        val runtimeContext = RuntimeContext(mutableListOf(4.0))
        val result = (squareImpl.value()!! as Callable).eval(runtimeContext)

        assertEquals(16.0, result)

    //    assertEquals("def square (x: float):\n  x * x", squareImpl.toString())
    }

}