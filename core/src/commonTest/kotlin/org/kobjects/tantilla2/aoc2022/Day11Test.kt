package org.kobjects.tantilla2.aoc2022

import org.kobjects.tantilla2.testing.TantillaTest

class Day11Test : TantillaTest("""

struct Monkey:
  items: MutableList[int]
  operation: (int) -> int
  divisible: int
  if_true: int
  if_false: int
  mut count: int = 0

  def process(monkeys: List[Monkey], mod: Int = 0):
    for item in items:
      count += 1
      let mut level = operation(item)
      if mod == 0:
        level //= 3
      else:
        level %= mod
        
      if level % divisible == 0:
        monkeys[if_true].items.append(level)
      else:
        monkeys[if_false].items.append(level)

    items.clear()

def parseOperation(op: List[Str]) -> (Int) -> Int:
   let mut operation = lambda(x: int) -> int: x * x
   let operand2 = op[2]
   print(op)
   if op[1] == "+":
      operation@ = ${'$'}0 + int(operand2)
   elif op[2] != "old":
      operation@ = ${'$'}0 * int(operand2)
   return operation@

def parse(data: str) -> List[Monkey]:
  let result = MutableList[Monkey]()
  let mut index = 0
  let lines = data.split("\n")
  while index < len(lines):
    let raw_items = lines[index + 1].split(":")[1].split(",")
    let items = MutableList[int].init(len(raw_items), int(raw_items[${'$'}0].strip()))
    let raw_operation = lines[index + 2].split("=")[1].strip().split(" ")
    let test = int(lines[index + 3].split(" ")[-1])
    let if_true = int(lines[index + 4].split(" ")[-1])
    let if_false = int(lines[index + 5].split(" ")[-1])
    let monkey = Monkey(items, parseOperation(raw_operation), test, if_true, if_false)
    result.append(monkey)
    index += 7
  return result

def monkey_business(monkeys: List[Monkey]) -> Int:
  let mut most_active = 0
  let mut most_active_2 = 0

  for m in monkeys:
    if m.count > most_active:
      most_active = m.count

  for m in monkeys:
    if m.count > most_active_2 and m.count < most_active:
      most_active_2 = m.count

  return most_active * most_active_2
  
def test_day_11_a():
  let monkeys = parse(DATA)

  for i in range(0, 20):
    for m in monkeys:
      m.process(monkeys)

  assert monkey_business(monkeys) == 10605

def test_day_11_b():
  let mut mod = 1
  let monkeys = parse(DATA)
  
  for m in monkeys:
    mod *= m.divisible

  for i in range(0, 10000):
    for m in monkeys:
      m.process(monkeys, mod)

  assert monkey_business(monkeys) == 2713310158


DATA = '''Monkey 0:
  Starting items: 79, 98
  Operation: new = old * 19
  Test: divisible by 23
    If true: throw to monkey 2
    If false: throw to monkey 3

Monkey 1:
  Starting items: 54, 65, 75, 74
  Operation: new = old + 6
  Test: divisible by 19
    If true: throw to monkey 2
    If false: throw to monkey 0

Monkey 2:
  Starting items: 79, 60, 97
  Operation: new = old * old
  Test: divisible by 13
    If true: throw to monkey 1
    If false: throw to monkey 3

Monkey 3:
  Starting items: 74
  Operation: new = old + 3
  Test: divisible by 17
    If true: throw to monkey 0
    If false: throw to monkey 1'''
""")