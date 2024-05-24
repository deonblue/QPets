package com.sevennotes.qpets.events

/**
 * 全局数据更新事件，监听该事件即可获取全局数据更新的通知
 */
sealed class GlobalDataEvent {
  class HungryEvent(val value: Int) : GlobalDataEvent()
  class StrengthEvent(val value: Int) : GlobalDataEvent()
  class ScoreEvent(val value: Int) : GlobalDataEvent()
  class HartEvent(val value: Int) : GlobalDataEvent()
}
