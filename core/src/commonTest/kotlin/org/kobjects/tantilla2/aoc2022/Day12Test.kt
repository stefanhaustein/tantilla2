package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day12Test : TantillaTest("""
MAP = RAW_MAP.split("\n")

candidates = MutableList[Candidate]()

def find(c: Str) -> Pair[Int, Int]:
  for y in range(0, len(MAP)):
    let row = MAP[y]
    for x in range(0, len(row)):
      if row[x] == c:
        return Pair[Int, Int](x, y)

  Pair[Int, Int](-1, -1)

struct Candidate:
  x: Int
  y: Int
  elevation: Int
  distance: Int



def check(candidate: Candidate, distances: List[MutableList[Int]], target: Str) -> Int:
  let x = candidate.x
  let y = candidate.y
  let distance = candidate.distance

  if MAP[y][x] == target:
    return distance

  if distance < distances[y][x]:
    distances[y][x] = distance
    let elevation = candidate.elevation
    add_candidate(x + 1, y, elevation, distance)
    add_candidate(x - 1, y, elevation, distance)
    add_candidate(x, y + 1, elevation, distance)
    add_candidate(x, y - 1, elevation, distance)

  return -1

def add_candidate(x: Int, y: Int, elevation: Int, distance: Int):
  if x >= 0 and y >= 0 and y < len(MAP) and x < len(MAP[0]):
    let mut candidate_elevation = ord(MAP[y][x])
    if candidate_elevation == ord("S"):
      candidate_elevation = ord("a")
    if elevation - candidate_elevation <= 1:
      candidates.append(Candidate(x, y, candidate_elevation, distance + 1))

def find_distance(target: Str) -> Int:
  let distances = List[MutableList[Int]].init(len(MAP)): MutableList[Int].init(len(MAP[${'$'}0])): 10000
  let end = find("E")

  candidates.clear()
  candidates.append(Candidate(end.a, end.b, ord("z"), 0))

  while len(candidates) > 0:
    let candidate = candidates.pop(0)
    let distance = check(candidate, distances, target)
    if distance != -1:
      return distance

  return -1

def test_day_12_a():
  assert find_distance("S") == 31
  
def test_day_12_b():
  assert find_distance("a") == 29
  


RAW_MAP = '''Sabqponm
abcryxxl
accszExk
acctuvwj
abdefghi'''


""")