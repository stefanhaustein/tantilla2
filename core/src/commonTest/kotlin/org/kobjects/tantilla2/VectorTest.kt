package org.kobjects.tantilla2

import org.kobjects.tantilla2.testing.TantillaTest

class StructTest : TantillaTest("""
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
                
        def test_vector():
          assert (Vector(1, 2, 3).mag() == 3.7416573867739413)
    """)
