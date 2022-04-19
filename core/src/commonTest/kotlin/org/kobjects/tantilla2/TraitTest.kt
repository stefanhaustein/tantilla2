package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.Serializer.serialize
import org.kobjects.tantilla2.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class TraitTest {
    val TEST = """
        trait Animal:
           def noise(self) -> String
           
        class Dog:
        
        impl Animal for Dog:
            def noise(self) -> String:
              return "Woof"
              
        Dog() as Animal.noise()
    """.trimIndent()

    @Test
    fun testTrait() {
        val parsingContext = ParsingContext("", ParsingContext.Kind.ROOT, null)

        val result = Parser.parse(TEST, parsingContext)

        assertEquals(setOf("Animal", "Dog", "Animal Dog"), parsingContext.definitions.keys)

        assertEquals("mag(Vector(1.0, 2.0, 3.0))", result.serialize())
        //    assertEquals("", parsingContext.serialize())

        val vectorImpl = parsingContext.definitions["Vector"]!!.value() as ParsingContext
        assertEquals(setOf("x", "y", "z", "times", "minus", "plus", "dot", "mag", "norm"), vectorImpl.definitions.keys)

        //  assertEquals("", parsingContext.toString())

        val runtimeContext = RuntimeContext(mutableListOf())
        assertEquals(3.3, result.eval(runtimeContext))
    }


}