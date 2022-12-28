package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day06Test : TantillaTest("""

def test_day_06_a():
  let mut count = 1
  for i in range(1, len(SIGNAL)):
    for j in range(0, count):
      if SIGNAL[i - j - 1] == SIGNAL[i]:
        count = j
        break

    count = count + 1

    if count == 4:
      assert i + 1 == 7
      break
      
      
def test_day_06_b():
  let mut count = 1
  for i in range(1, len(SIGNAL)):
    for j in range(0, count):
      if SIGNAL[i - j - 1] == SIGNAL[i]:
        count = j
        break

    count = count + 1

    if count == 14:
      assert i + 1 == 19
      break


SIGNAL = '''mjqjpqmgbljsphdztnvjfqwrcgsmlb'''

""")