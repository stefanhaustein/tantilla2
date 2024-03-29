package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest





class Day05Test : TantillaTest("""
def test_day_05_a():
  let lines = INPUT.split("\n")
  let split_index = lines.index("")
  let stacks = MutableList[MutableList[str]]()
  let stack_count = (len(lines[split_index - 1]) + 3) // 4

  for i in range(0, stack_count):
    stacks.append(MutableList[str]())

  for i in range(0, split_index - 1):
    let line = lines[i]
    for j in range(0, (len(line) + 3) // 4):
      let s = line[j * 4 + 1]
      if s != " ":
        stacks[j].insert(0, s)

  for i in range(split_index + 1, len(lines)):
    let parts = lines[i].split(" ")
    let count = int(parts[1])
    let source = int(parts[3]) - 1
    let dest = int(parts[5]) - 1
    for j in range(0, count):
      stacks[dest].append(stacks[source].pop())

  let results = MutableList[str]()
  for stack in stacks:
    results.append(stack[-1])

  assert "".join(results) == "CMZ"

def test_day_05_b():
  let lines = INPUT.split("\n")
  let split_index = lines.index("")
  let stacks = MutableList[MutableList[str]]()
  let stack_count = (len(lines[split_index - 1]) + 3) // 4

  for i in range(0, stack_count):
    stacks.append(MutableList[str]())

  for i in range(0, split_index - 1):
    let line = lines[i]
    for j in range(0, (len(line) + 3) // 4):
      let s = line[j * 4 + 1]
      if s != " ":
        stacks[j].insert(0, s)

  for i in range(split_index + 1, len(lines)):
    let parts = lines[i].split(" ")
    let count = int(parts[1])
    let source = int(parts[3]) - 1
    let dest = int(parts[5]) - 1
    let ip = len(stacks[dest])
    for j in range(0, count):
      stacks[dest].insert(ip, stacks[source].pop())

  let results = MutableList[str]()
  for stack in stacks:
    results.append(stack[-1])

  assert "".join(results) == "MCD"



INPUT = '''    [D]
[N] [C]
[Z] [M] [P]
 1   2   3

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2'''
""")