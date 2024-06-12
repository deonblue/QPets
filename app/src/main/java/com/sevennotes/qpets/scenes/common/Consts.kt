package com.sevennotes.qpets.scenes.common

//间隔多少秒计算一次饥饿值
const val HUNGRY_TIMER = 10.0
//间隔多少秒计算一次心情值
const val HEART_TIMER = 10.0
//idle时,间隔多少秒尝试切换状态
const val IDLE_TIMER = 5.0
//walk时，间隔多少秒尝试切换状态
const val WALK_TIMER = 3.0
//睡觉多少秒恢复一次体力
const val SLEEP_TIMER = 10.0
//idle切换walk的概率(x/10)
const val IDLE_TO_WALK_RATE = 3
//walk切换回idle的概率(x/10)
const val WALK_TO_IDLE_RATE = 8
//walk时挖到宝的概率(x/100)
const val LOOKING_RATE = 8