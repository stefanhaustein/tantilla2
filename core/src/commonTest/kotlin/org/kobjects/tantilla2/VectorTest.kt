package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VectorTest {
    val VECTOR = """
        struct Vector:
          x: float
          y: float
          z: float

          def times(k: float) -> Vector:
            Vector(k * self.x, k * self.y, k * self.z)

          def minus(v2: Vector) -> Vector:
            Vector(self.x - v2.x, self.y - v2.y, self.z - v2.z)

          def plus(v2: Vector) -> Vector:
            Vector(self.x + v2.x, self.y + v2.y, self.z + v2.z)

          def dot(v2: Vector) -> float:
            self.x * v2.x + self.y * v2.y + self.z * v2.z
   
          def mag() -> float:
            sqrt(self.x * self.x + self.y * self.y + self.z * self.z)
      
          def norm() -> Vector:
            self.times(1/self.mag())
                
        Vector(1, 2, 3).mag()
    """.trimIndent()

    @Test
    fun testVector() {
        val parsingContext = TestSystemAbstraction.createScope()

        val result = Parser.parse(VECTOR, parsingContext)

        assertNotNull(parsingContext["Vector"])

        assertEquals("Vector(1, 2, 3).mag()", result.serializeCode())
    //    assertEquals("", parsingContext.serialize())

        val vectorImpl = parsingContext["Vector"]!!.getValue(null) as Scope
        assertEquals(setOf("x", "y", "z", "times", "minus", "plus", "dot", "mag", "norm"),
            vectorImpl.iterator().asSequence().map { it.name }.toSet())

      //  assertEquals("", parsingContext.toString())

        val runtimeContext = LocalRuntimeContext(GlobalRuntimeContext(parsingContext))
        assertEquals(3.7416573867739413, result.eval(runtimeContext))
    }

}