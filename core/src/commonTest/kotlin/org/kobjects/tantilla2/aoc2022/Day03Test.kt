package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day03Test : TantillaTest("""
def calculate_priority(s: str) -> int:
  let v = ord(s)
  if  v > 96:
    return v - 96
  return v - 64 + 26

def test_day_03_a():
  let mut score = 0
  for line in RUCKSACKS.split("\n"):
    let mut types1 = 0
    let l = len(line)
    for i in range(0, l):
      let p = calculate_priority(line[i])
      let mask = 1 << (p - 1)
      if i < l/2:
        types1 |= mask
      elif (types1 & mask) != 0:
        score += p
        break

  assert score == 157

def test_day_03_b():
  let mut score = 0
  let mut group = 0
  let mut n = 0
  for line in RUCKSACKS.split("\n"):
    let mut types = 0
    for i in range(0, len(line)):
      let p = calculate_priority(line[i])
      let mask = 1 << (p - 1)
      types |= mask

    if n == 0:
      group = types
    else:
      group &= types

    if n == 2:
      score += 1 + int(math.log2(group))
      n = 0
    else:
      n += 1

  assert score == 70


RUCKSACKS = '''vJrwpWtwJgWrhcsFMMfFFhFp
jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
PmmdzqPrVvPwwTWBwg
wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
ttgJtRGJQctTZtZT
CrZsJsPPZsGzwwsLwLmpwMDw'''    
""")