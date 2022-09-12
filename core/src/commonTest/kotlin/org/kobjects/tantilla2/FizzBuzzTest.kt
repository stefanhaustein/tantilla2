package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.builtin.StrType
import org.kobjects.tantilla2.core.builtin.VoidType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FizzBuzzTest {

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
              print(str(x))
              
            x = x + 1
    """.trimIndent()


    @Test
    fun testFizzBuzz() {
        val output = mutableListOf<String>()

        val context = UserRootScope(RootScope)

        context.defineNativeFunction(
            "print", "internal",
            VoidType,
            Parameter("text", StrType)) { try {
                output.add(it.variables[0].toString())
            } catch (e: Exception) {
                throw RuntimeException("Issue with context $it", e)
            } }

        Parser.parse(FIZZ_BUZZ, context)

        val impl = context["fizz_buzz"]!!

        assertNotNull(impl.value())

        // assertEquals("", impl.toString())

        val runtimeContext = LocalRuntimeContext(mutableListOf(null))
        (impl.value()!! as Callable).eval(runtimeContext)

        assertEquals(listOf(
            "1.0", "2.0", "Fizz", "4.0", "Buzz",
            "Fizz", "7.0", "8.0", "Fizz", "Buzz",
            "11.0", "Fizz", "13.0", "14.0", "FizzBuzz",
            "16.0", "17.0", "Fizz", "19.0", "Buzz"), output)
    }

}