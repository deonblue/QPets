package com.sevennotes.qpets.events

/**
 * 这个事件是用来更新窗口位置的事件
 */
class UpdateWindowEvent(val deltaX: Int = 0, val deltaY: Int = 0)

/**
 * 如果窗口位置正在进行手动调整(不包括游戏逻辑使得它自动调整），那么这个事件将会被触发
 */
class IsUpdatingWindow(val updating: Boolean)
