package org.kobjects.tantilla2.aoc2022


import org.kobjects.tantilla2.testing.TantillaTest



class Day01Test : TantillaTest("""

def test_day_1_a():
  let mut current = 0
  let mut best = 0
  for line in (CALORIES + "\n").split("\n"):
    if line == "":
      if current > best:
        best = current
      current = 0
    else:
      current = current + int(line)

  assert(best == 24000)

def test_day_1_b():
  let mut current = 0
  let all = MutableList[int]()
  for line in (CALORIES + "\n").split("\n"):
    if line == "":
      all.append(current)
      current = 0
    else:
      current = current + int(line)

  all.sort()
  let l = len(all)

  assert (all[l - 3] + all[l -2] + all[l-1] == 45000)


CALORIES = '''1000
2000
3000

4000

5000
6000

7000
8000
9000

10000
'''
""") {

}
