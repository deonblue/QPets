package com.sevennotes.qpets.scenes.interfaces

import korlibs.time.TimeSpan

interface State {
  fun onEnter() {}
  fun onExit() {}
  fun update(time: TimeSpan) {}
}