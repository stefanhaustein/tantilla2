package org.kobjects.tantilla2

import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.Serializer.serialize
import org.kobjects.tantilla2.parser.Parser
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class VectorTest {
    val VECTOR = """
        class Vector:
          let x: float
          let y: float
          let z: float

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
        val parsingContext = ParsingContext("", ParsingContext.Kind.ROOT, null)

        parsingContext.defineValue(
            "sqrt",
            NativeFunction(
                FunctionType(false, F64, listOf(Parameter("x", F64)))
            ) { sqrt(it.variables[0] as Double ) })

        val result = Parser.parse(VECTOR, parsingContext)

        assertEquals(setOf("Vector", "sqrt"), parsingContext.definitions.keys)

        assertEquals("mag(Vector(1.0, 2.0, 3.0))", result.serialize())
    //    assertEquals("", parsingContext.serialize())

        val vectorImpl = parsingContext.definitions["Vector"]!!.value() as ParsingContext
        assertEquals(setOf("x", "y", "z", "times", "minus", "plus", "dot", "mag", "norm"), vectorImpl.definitions.keys)

      //  assertEquals("", parsingContext.toString())

        val runtimeContext = RuntimeContext(mutableListOf())
        assertEquals(3.3, result.eval(runtimeContext))
    }

}