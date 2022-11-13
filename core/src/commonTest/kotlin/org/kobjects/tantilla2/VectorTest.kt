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
        class Vector:
          val x: float
          val y: float
          val z: float

          def times(self, k: float) -> Vector:
            Vector(k * self.x, k * self.y, k * self.z)

          def minus(self, v2: Vector) -> Vector:
            Vector(self.x - v2.x, self.y - v2.y, self.z - v2.z)

          def plus(self, v2: Vector) -> Vector:
            Vector(self.x + v2.x, self.y + v2.y, self.z + v2.z)

          def dot(self, v2: Vector) -> float:
            self.x * v2.x + self.y * v2.y + self.z * v2.z
   
          def mag(self) -> float:
            sqrt(self.x * self.x + self.y * self.y + self.z * self.z)
      
          def norm(self) -> Vector:
            self.times(1/self.mag())
                
        Vector(1, 2, 3).mag()
    """.trimIndent()

    @Test
    fun testVector() {
        val parsingContext = UserRootScope(TestSystem)

        val result = Parser.parse(VECTOR, parsingContext)

        assertNotNull(parsingContext["Vector"])

        assertEquals("mag(Vector(1.0, 2.0, 3.0))", result.serializeCode())
    //    assertEquals("", parsingContext.serialize())

        val vectorImpl = parsingContext["Vector"]!!.getValue(null) as Scope
        assertEquals(setOf("x", "y", "z", "times", "minus", "plus", "dot", "mag", "norm"),
            vectorImpl.iterator().asSequence().map { it.name }.toSet())

      //  assertEquals("", parsingContext.toString())

        val runtimeContext = LocalRuntimeContext(GlobalRuntimeContext(parsingContext))
        assertEquals(3.7416573867739413, result.eval(runtimeContext))
    }

}