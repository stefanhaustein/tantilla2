package org.kobjects.tantilla2

import org.kobjects.tantilla2.testing.TantillaTest

class MapTest : TantillaTest("""
    def test_mutable():
      let map = MutableMap[int,int]()
      assert(map.size == 1, "size")
""") {

}
