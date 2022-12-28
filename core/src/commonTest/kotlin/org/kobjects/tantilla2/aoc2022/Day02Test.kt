package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day02Test : TantillaTest("""
def test_day_02_a():
  let mut val = 0
  for line in STRATEGY.split("\n"):
    if line == "A X":
      val += 1 + 3
    elif line == "A Y":
      val += 2 + 6
    elif line == "A Z":
      val += 3 + 0
    elif line == "B X":
      val += 1 + 0
    elif line == "B Y":
      val += 2 + 3
    elif line == "B Z":
      val += 3 + 6
    elif line == "C X":
      val += 1 + 6
    elif line == "C Y":
      val += 2 + 0
    elif line == "C Z":
      val += 3 + 3

  assert val == 15

def test_day_02_b():
  let mut val = 0
  for line in STRATEGY.split("\n"):
    if line == "A X":
      val += 3 + 0
    elif line == "A Y":
      val += 1 + 3
    elif line == "A Z":
      val += 2 + 6
    elif line == "B X":
      val += 1 + 0
    elif line == "B Y":
      val += 2 + 3
    elif line == "B Z":
      val += 3 + 6
    elif line == "C X":
      val += 2 + 0
    elif line == "C Y":
      val += 3 + 3
    elif line == "C Z":
      val += 1 + 6

  assert val == 12


STRATEGY = '''A Y
B X
C Z'''
""") {

}
