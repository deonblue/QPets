package com.sevennotes.qpets.scenes.common

class Timer(
  private val time: Double
) {

  private var currTime = 0.0

  fun reset() {
    currTime = 0.0
  }

  fun stick(delta: Double): Boolean {
    currTime += delta
    return if (currTime >= time) {
      currTime = 0.0
      true
    } else {
      false
    }
  }

}
