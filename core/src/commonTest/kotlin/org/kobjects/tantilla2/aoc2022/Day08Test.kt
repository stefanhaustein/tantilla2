package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day08Test : TantillaTest("""
def scan_a(lines: List[str], map: MutableList[MutableList[bool]], x0: int, y0: int, dx: int, dy: int):
  let mut visible_height = 0
  let mut x = x0
  let mut y = y0
  while x >= 0 and y >= 0 and x < len(lines[0]) and y < len(lines) and visible_height <= 9:
    let tree_height = int(lines[y][x])
    if tree_height >= visible_height:
      map[y][x] = True
      visible_height = tree_height + 1

    x = x + dx
    y = y + dy

def test_day_08_a():
  let lines = FOREST.split("\n")
  let width = len(lines[0])
  let height = len(lines)
  let map = MutableList[MutableList[bool]].init(height, MutableList[bool].init(width, False))

  for y in range(0, height):
    scan_a(lines, map, 0, y, 1, 0)
    scan_a(lines, map, width - 1, y, -1, 0)

  for x in range(0, width):
    scan_a(lines, map, x, 0, 0, 1)
    scan_a(lines, map, x, height - 1, 0, -1)

  let mut count = 0
  for y in range(0, height):
    for x in range(0, width):
      if map[y][x]:
        count = count + 1

  assert count == 21

def scan_b(lines: List[str], x: int, y: int, dx: int, dy: int) -> int:
  let height = int(lines[y][x])
  let mut cx = x + dx
  let mut cy = y + dy
  let mut score = 0
  while cx >= 0 and cy >= 0 and cx < len(lines[0]) and cy < len(lines):
    score = score + 1
    if int(lines[cy][cx]) >= height:
      break
    cx = cx + dx
    cy = cy + dy

  return score

def test_day_08_b():
  let lines = FOREST.split("\n")
  let width = len(lines[0])
  let height = len(lines)
  let mut best = 0

  for y in range(1, height - 1):
    for x in range(1, width - 1):
      let score = (scan_b(lines, x, y, 0, 1) 
        * scan_b(lines, x, y, 0, -1) 
        * scan_b(lines, x, y, 1, 0) 
        * scan_b(lines, x, y, -1, 0))
      if score > best:
        best = score


  assert best == 8



FOREST = '''30373
25512
65332
33549
35390'''    
""")