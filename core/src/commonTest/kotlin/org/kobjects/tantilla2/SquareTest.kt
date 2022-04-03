package org.kobjects.tantilla2

import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SquareTest {

    val SQUARE = """
        def square(x: float):
          x * x
    """.trimIndent()

    @Test
    fun testSquare() {
        val context = ParsingContext("", ParsingContext.Kind.ROOT, null)
        Parser.parse(SQUARE, context)

        val squareImpl = context.definitions["square"]!!

        // assertEquals("def square (x: float):\n  x * x", squareImpl.toString())

        val runtimeContext = RuntimeContext(mutableListOf(4.0))
        val result = (squareImpl.value(context)!! as Lambda).eval(runtimeContext)

        assertEquals(16.0, result)

    //    assertEquals("def square (x: float):\n  x * x", squareImpl.toString())
    }

}