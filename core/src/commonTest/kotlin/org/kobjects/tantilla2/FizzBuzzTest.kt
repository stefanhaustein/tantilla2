package org.kobjects.tantilla2

import org.kobjects.tantilla2.testing.TantillaTest

class FizzBuzzTest : TantillaTest("""
     def fizz_buzz(x: int) -> str: 
            if x % 3 == 0:
              if x % 5 == 0:
                return "FizzBuzz"
              else:
                return "Fizz"
            elif x % 5 == 0:
              return "Buzz"
            else:
              return str(x)
              
     def test_fizz_buzz():
       assert(fizz_buzz(1) == "1")
       assert(fizz_buzz(2) == "2")
       assert(fizz_buzz(3) == "Fizz")
       assert(fizz_buzz(4) == "4")
       assert(fizz_buzz(5) == "Buzz")
       assert(fizz_buzz(6) == "Fizz")
       assert(fizz_buzz(7) == "7")
       assert(fizz_buzz(8) == "8")
       assert(fizz_buzz(9) == "Fizz")
       assert(fizz_buzz(10) == "Buzz")       
       assert(fizz_buzz(11) == "11")
       assert(fizz_buzz(12) == "Fizz")
       assert(fizz_buzz(13) == "13")
       assert(fizz_buzz(14) == "14")
       assert(fizz_buzz(15) == "FizzBuzz")
       assert(fizz_buzz(16) == "16")
       assert(fizz_buzz(17) == "17")
       assert(fizz_buzz(18) == "Fizz")
       assert(fizz_buzz(19) == "19")
       
               
""")