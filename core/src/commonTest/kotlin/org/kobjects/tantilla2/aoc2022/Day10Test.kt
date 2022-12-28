package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day10Test : TantillaTest("""
def test_day_10_a():
  let mut cycle = 0
  let mut x = 1
  let mut wait_state = False
  let mut next_signal = 20
  let mut signal_str = 0
  let mut lines = INPUT.split("\n")
  let mut index = 0
  let mut v = 0

  while True:
    cycle = cycle + 1

    if cycle == next_signal:
      signal_str = signal_str + cycle * x
      if next_signal <= 180:
        next_signal = next_signal + 40

    if wait_state:
      x = x + v
      wait_state = False
    else:
      if index >= len(lines):
        break
      let cmd = lines[index].split(" ")
      if cmd[0] == "addx":
        v = int(cmd[1])
        wait_state = True
      index = index + 1

  assert  signal_str == 13140

def test_day_10_b():
  let mut cycle = 0
  let mut x = 1
  let mut wait_state = False
  let mut horiz_pos = 1
  let mut lines = INPUT.split("\n")
  let mut index = 0
  let mut output = ""
  let mut v = 0

  while index < len(lines):
    cycle = cycle + 1

    if x >= horiz_pos - 2 and x <= horiz_pos:
      output = output + "!"
    else:
      output = output + "."

    horiz_pos = horiz_pos + 1
    if horiz_pos == 41:
      horiz_pos = 1
      output = output + "\n"

    if wait_state:
      x = x + v
      wait_state = False
    else:
      let cmd = lines[index].split(" ")
      if cmd[0] == "addx":
        v = int(cmd[1])
        wait_state = True
      index = index + 1

  assert output == OUTPUT

OUTPUT = '''!!..!!..!!..!!..!!..!!..!!..!!..!!..!!..
!!!...!!!...!!!...!!!...!!!...!!!...!!!.
!!!!....!!!!....!!!!....!!!!....!!!!....
!!!!!.....!!!!!.....!!!!!.....!!!!!.....
!!!!!!......!!!!!!......!!!!!!......!!!!
!!!!!!!.......!!!!!!!.......!!!!!!!.....
'''


INPUT = '''addx 15
addx -11
addx 6
addx -3
addx 5
addx -1
addx -8
addx 13
addx 4
noop
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx -35
addx 1
addx 24
addx -19
addx 1
addx 16
addx -11
noop
noop
addx 21
addx -15
noop
noop
addx -3
addx 9
addx 1
addx -3
addx 8
addx 1
addx 5
noop
noop
noop
noop
noop
addx -36
noop
addx 1
addx 7
noop
noop
noop
addx 2
addx 6
noop
noop
noop
noop
noop
addx 1
noop
noop
addx 7
addx 1
noop
addx -13
addx 13
addx 7
noop
addx 1
addx -33
noop
noop
noop
addx 2
noop
noop
noop
addx 8
noop
addx -1
addx 2
addx 1
noop
addx 17
addx -9
addx 1
addx 1
addx -3
addx 11
noop
noop
addx 1
noop
addx 1
noop
noop
addx -13
addx -19
addx 1
addx 3
addx 26
addx -30
addx 12
addx -1
addx 3
addx 1
noop
noop
noop
addx -9
addx 18
addx 1
addx 2
noop
noop
addx 9
noop
noop
noop
addx -1
addx 2
addx -37
addx 1
addx 3
noop
addx 15
addx -21
addx 22
addx -6
addx 1
noop
addx 2
addx 1
noop
addx -10
noop
noop
addx 20
addx 1
addx 2
addx 2
addx -6
addx -11
noop
noop
noop'''    
""")