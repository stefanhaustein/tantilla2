package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day09Test : TantillaTest("""
def test_day_09_a():
  let mut hx = 0
  let mut hy = 0
  let mut tx = 0
  let mut ty = 0

  let mut positions = MutableSet[Pair[int,int]]()

  print MOVES_A
  print MOVES_B

  for move in MOVES_A.split("\n"):
    let parts = move.split(" ")
    let dir = parts[0]
    let mut dx = 0
    let mut dy = 0
    if dir == "U":
      dy = -1
    elif dir == "D":
      dy = 1
    elif dir == "L":
      dx = -1
    elif dir == "R":
      dx = 1
    for i in range(0, int(parts[1])):
      hx = hx + dx
      hy = hy + dy
      let abs_x = abs(hx - tx)
      let abs_y = abs(hy - ty)
      let diagonal = abs_x > 0 and abs_y > 0 and (abs_x > 1 or abs_y > 1)
      if diagonal or abs_x > 1:
        tx = tx + (hx - tx) // abs_x
      if diagonal or abs_y > 1:
        ty = ty + (hy - ty) // abs_y
      positions.add(Pair[int,int](tx, ty))

  
  assert len(positions) == 13


def test_day_09_b():
  let str_x = MutableList[int].init(10, 0)
  let str_y = MutableList[int].init(10, 0)

  let mut positions = MutableSet[Pair[int,int]]()

  for move in MOVES_B.split("\n"):
    let parts = move.split(" ")
    let dir = parts[0]
    let mut dx = 0
    let mut dy = 0
    if dir == "U":
      dy = -1
    elif dir == "D":
      dy = 1
    elif dir == "L":
      dx = -1
    elif dir == "R":
      dx = 1
    for i in range(0, int(parts[1])):
      str_x[0] = str_x[0] + dx
      str_y[0] = str_y[0] + dy
      for i in range(1, 10):
        let prev_x = str_x[i-1]
        let prev_y = str_y[i-1]
        let curr_x = str_x[i]
        let curr_y = str_y[i]
        let abs_x = abs(prev_x - curr_x)
        let abs_y = abs(prev_y - curr_y)
        let diagonal = prev_x != curr_x and prev_y != curr_y and (abs_x > 1 or abs_y > 1)
        if diagonal or abs_x > 1:
          str_x[i] = curr_x + (prev_x - curr_x) // abs_x
        if diagonal or abs_y > 1:
          str_y[i] = curr_y + (prev_y - curr_y) // abs_y
      positions.add(Pair[int,int](str_x[9], str_y[9]))

  assert len(positions) == 36


MOVES_B = '''R 5
U 8
L 8
D 3
R 17
D 10
L 25
U 20'''


MOVES_A = '''R 4
U 4
L 3
D 1
R 4
D 1
L 5
R 2'''


""")