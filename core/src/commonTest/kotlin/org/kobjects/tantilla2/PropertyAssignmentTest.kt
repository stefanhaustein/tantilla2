package org.kobjects.tantilla2

import org.kobjects.tantilla2.testing.TantillaTest

class PropertyAssignmentTest : TantillaTest("""

struct TestStruct:
  mut _x = 0
  
  def x() -> Int:
    _x
    
  def set_x(new_x: Int):
    _x = new_x
  
def test_properties():
  let ts = TestStruct()
  assert ts.x == 0
  
  ts.set_x(42)
  
  assert ts.x == 42
  
  ts.x = 555
  
  assert ts.x == 555
  
  
""") {

}
