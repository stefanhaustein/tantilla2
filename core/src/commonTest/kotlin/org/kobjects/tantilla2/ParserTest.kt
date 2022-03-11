package org.kobjects.tantilla2

import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParserTest {

    val SQUARE = """
        def square(x: float):
          x * x
    """.trimIndent()

    val FIZZ_BUZZ = """
        def fizz_buzz(): 
          var x = 1
          while x <= 20:
            if x % 3 == 0:
              if x % 5 == 0:
                print("FizzBuzz")
              else:
                print("Fizz")
            elif x % 5 == 0:
              print("Buzz")
            else:
              print(x)
    """.trimIndent()



    @Test
    fun testSquare() {
        val context = ParsingContext(null)
        Parser.parse(SQUARE, context)

        val squareImpl = context.definitions["square"]!!

        assertEquals("def square (x: float):\n  x * x", squareImpl.toString())

        val runtimeContext = RuntimeContext(mutableListOf(4.0))
        val result = (squareImpl.value(context)!! as Lambda).eval(runtimeContext)

        assertEquals(16.0, result)

    //    assertEquals("def square (x: float):\n  x * x", squareImpl.toString())
    }

    @Test
    fun testFizzBuzz() {
        val output = mutableListOf<String>()

        val context = ParsingContext(null)

        context.defineValue(
            "print",
            Lambda(
                FunctionType(Void, listOf(Parameter("text", Str))),
            ) { output.add(it.variables[0] as String) } )

        Parser.parse(FIZZ_BUZZ, context)

        val impl = context.definitions["fizz_buzz"]!!

        assertNotNull(impl.value(context))
    }

}