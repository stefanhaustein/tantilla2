package org.kobjects.tantilla2

import org.kobjects.tantilla2.testing.TantillaTest

class TraitTest : TantillaTest("""
  trait Animal:
    def noise() -> str
           
  struct Dog:

  impl Animal for Dog:
    def noise() -> str:
      "Woof"
                        
  def test_trait(): 
    assert (Dog() as Animal.noise() == "Woof") 
""")