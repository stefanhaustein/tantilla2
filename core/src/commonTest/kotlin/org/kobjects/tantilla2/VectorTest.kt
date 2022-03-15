package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.Lambda
import org.kobjects.tantilla2.core.ParsingContext
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class VectorTest {
    val VECTOR = """
        class Vector:
          x: float
          y: float
          z: float

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
    """.trimIndent()

    @Test
    fun testVector() {
        val context = ParsingContext("", ParsingContext.Kind.ROOT, null)
        Parser.parse(VECTOR, context)

        val vectorImpl = context.definitions["Vector"]!!

    }

}