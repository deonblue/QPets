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
}
