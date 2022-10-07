package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.builtin.RootScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TraitTest {
    val TEST = """
        trait Animal:
           def noise(self) -> str
           
        struct Dog:
        
        impl Animal for Dog:
            def noise(self) -> str:
              "Woof"
              
        Dog() as Animal.noise()
    """.trimIndent()

    @Test
    fun testTrait() {
        val parsingContext = UserRootScope(TestSystem)

        val result = Parser.parse(TEST, parsingContext)

        // assertEquals(setOf("Animal", "Dog", "Animal for Dog"), parsingContext.definitions.keys)

        // assertEquals("mag(Vector(1.0, 2.0, 3.0))", result.serialize())
        //    assertEquals("", parsingContext.serialize())

        val animal = parsingContext["Animal"]!!.getValue(null) as Scope
        assertEquals(setOf("noise"),
            animal.iterator().asSequence().map { it.name }.toSet())

        //  assertEquals("", parsingContext.toString())

        assertTrue(parsingContext.errors.isEmpty())

        val runtimeContext = LocalRuntimeContext(GlobalRuntimeContext(parsingContext))
        assertEquals("Woof", result.eval(runtimeContext))
    }


}