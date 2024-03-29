package org.kobjects.tantilla2.aoc2022


import org.kobjects.tantilla2.testing.TantillaTest

class Day04Test : TantillaTest("""
    
def test_day_04_a():
  let mut count = 0
  for line in PAIRS.split("\n"):
    let ranges = line.split(",")
    let range1 = ranges[0].split("-")
    let range2 = ranges[1].split("-")
    let start1 = int(range1[0])
    let end1 = int(range1[1])
    let start2 = int(range2[0])
    let end2 = int(range2[1])
    if ((start1 <= start2 and end1 >= end2) or (start2 <= start1 and end2 >= end1)):
      count += 1

  assert count == 2

def test_day_04_b():
  let mut count = 0
  for line in PAIRS.split("\n"):
    let ranges = line.split(",")
    let range1 = ranges[0].split("-")
    let range2 = ranges[1].split("-")
    let start1 = int(range1[0])
    let end1 = int(range1[1])
    let start2 = int(range2[0])
    let end2 = int(range2[1])
    if start1 <= end2 and end1 >= start2:
      count = count + 1

  assert count == 4


PAIRS = '''2-4,6-8
2-3,4-5
5-7,7-9
2-8,3-7
6-6,4-6
2-6,4-8'''    
    
""")