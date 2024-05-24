package com.sevennotes.qpets.utils

object RandomUtil {
  /**
   * 生成一个0到1000之间的随机数
   */
  fun random1000(): Int {
    return (Math.random() * 1001).toInt()
  }

  /**
   * 生成一个0到100之间的随机数
   */
  fun random100(): Int {
    return (Math.random() * 101).toInt()
  }

  /**
   * 生成一个0到10之间的随机数
   */
  fun random10(): Int {
    return (Math.random() * 11).toInt()
  }

  /**
   * 生成从from到to之间的一个随机数
   */
  fun randomIn(from: Int, to: Int): Int {
    return (Math.random() * (to - from + 1) + from).toInt()
  }
}

fun main() {
  var count0 = 0
  var count1 = 0
  var count2 = 0
  var count3 = 0
  for (i in 0..100) {
    val r = RandomUtil.randomIn(0, 3)
    when(r) {
      0 -> count0++
      1 -> count1++
      2 -> count2++
      3 -> count3++
      else -> {
        throw Exception("not permit number! $r")
      }
    }
    println(r)
  }
  println("0: $count0, 1: $count1, 2: $count2, 3: $count3")
}
