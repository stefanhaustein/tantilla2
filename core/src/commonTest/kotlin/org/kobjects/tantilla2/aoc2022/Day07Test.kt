package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day07Test : TantillaTest("""
struct Dir:
  dirs = MutableMap[str, Dir]()
  files = MutableMap[str, int]()

  def scan(totals: MutableList[int]):
    let mut total = 0
    for dir in dirs.values():
      dir.scan(totals)
      total = total + totals[-1]

    for size in files.values():
      total = total + size

    totals.append(total)


def test_day_07_a():
  let root = Dir()
  let path = MutableList[Dir](root)
  for line in LOG.split("\n"):
    let parts = line.split(" ")
    let dir = path[-1]
    if parts[0] == "${'$'}":
      if parts[1] == "cd":
        let name = parts[2]
        if name == "/":
          path.clear()
          path.append(root)
        elif name == "..":
          path.pop()
        else:
          let newDir = dir.dirs[name]
          path.append(newDir)

    elif parts[0] == "dir":
      let name = parts[1]
      if not (name in dir.dirs):
        dir.dirs[name] = Dir()
    else:
      let size = int(parts[0])
      let name = parts[1]
      dir.files[name] = size

  let totals = MutableList[int]()
  root.scan(totals)
  let mut result = 0
  for total in totals:
    if total <= 100000:
      result = result + total
      
  assert result == 95437



def test_day_07_b():
  let root = Dir()
  let path = MutableList[Dir](root)
  for line in LOG.split("\n"):
    let parts = line.split(" ")
    let dir = path[-1]
    if parts[0] == "${'$'}":
      if parts[1] == "cd":
        let name = parts[2]
        if name == "/":
          path.clear()
          path.append(root)
        elif name == "..":
          path.pop()
        else:
          let newDir = dir.dirs[name]
          path.append(newDir)

    elif parts[0] == "dir":
      let name = parts[1]
      if not (name in dir.dirs):
        dir.dirs[name] = Dir()
    else:
      let size = int(parts[0])
      let name = parts[1]
      dir.files[name] = size

  let totals = MutableList[int]()
  root.scan(totals)
  let mut best = totals[-1]
  for total in totals:
    if total >= 8381165 and total < best:
      best = total
  assert best == 24933642


LOG = '''${'$'} cd /
${'$'} ls
dir a
14848514 b.txt
8504156 c.dat
dir d
${'$'} cd a
${'$'} ls
dir e
29116 f
2557 g
62596 h.lst
${'$'} cd e
${'$'} ls
584 i
${'$'} cd ..
${'$'} cd ..
${'$'} cd d
${'$'} ls
4060174 j
8033020 d.log
5626152 d.ext
7214296 k'''
""")