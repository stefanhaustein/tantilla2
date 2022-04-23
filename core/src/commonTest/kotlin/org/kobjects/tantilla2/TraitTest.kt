package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class TraitTest {
    val TEST = """
        trait Animal:
           def noise(self) -> str
           
        class Dog:
        
        impl Animal for Dog:
            def noise(self) -> str:
              "Woof"
              
        Dog() as Animal.noise()
    """.trimIndent()

    @Test
    fun testTrait() {
        val parsingContext = RootScope()

        val result = Parser.parse(TEST, parsingContext)

        assertEquals(setOf("Animal", "Dog", "Animal for Dog"), parsingContext.definitions.keys)

        // assertEquals("mag(Vector(1.0, 2.0, 3.0))", result.serialize())
        //    assertEquals("", parsingContext.serialize())

        val animal = parsingContext.definitions["Animal"]!!.value() as Scope
        assertEquals(setOf("noise"), animal.definitions.keys)

        //  assertEquals("", parsingContext.toString())

        val runtimeContext = RuntimeContext(mutableListOf())
        assertEquals("Woof", result.eval(runtimeContext))
    }


}