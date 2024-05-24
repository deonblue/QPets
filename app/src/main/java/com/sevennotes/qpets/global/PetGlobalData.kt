package com.sevennotes.qpets.global

import com.google.gson.Gson
import com.sevennotes.qpets.events.GlobalDataEvent
import org.greenrobot.eventbus.EventBus


class PetGlobalData private constructor() {

  /**
   * 饥饿值 取值范围0-100
   */
  var hungry: Int = 100
    private set

  /**
   * 体力值 取值范围0-10
   */
  var strength: Int = 10
    private set

  /**
   * 货币分数，用于提高宠物属性, 购置物品等等
   */
  var score: Int = 0
    private set

  var heart: Int = 0
    private set

  fun updateHungry(value: Int) {
    //如果value 为负数， 那么表示hungry减少， 但是hungry最少为0. 如果value为正数，那么表示hungry增加，但是hungry最多为100
    synchronized(this) {
      hungry += value

      hungry = when {
        hungry < 0 -> 0
        hungry > 100 -> 100
        else -> hungry
      }
      EventBus.getDefault().post(GlobalDataEvent.HungryEvent(hungry))
    }
  }

  /**
   * 体力值是否已经满了
   */
  fun isStrengthFull(): Boolean {
    return strength >= 10
  }

  /**
   * 饥饿值是否已经满了
   */
  fun isHungryFull(): Boolean {
    return hungry >= 100
  }

  fun updateStrength(value: Int) {
    synchronized(this) {
      strength += value

      strength = when {
        strength < 0 -> 0
        strength > 10 -> 10
        else -> strength
      }
      EventBus.getDefault().post(GlobalDataEvent.StrengthEvent(strength))
    }
  }

  fun updateScore(value: Int) {
    synchronized(this) {
      score += value

      if (this.score < 0) {
        this.score = 0
      }
      EventBus.getDefault().post(GlobalDataEvent.ScoreEvent(score))
    }
  }

  fun updateHeart(value: Int) {
    synchronized(this) {
      heart += value
      heart = when {
        heart < 0 -> 0
        heart > 100 -> 100
        else -> heart
      }
      EventBus.getDefault().post(GlobalDataEvent.HartEvent(heart))
    }
  }

  companion object {
    private var instance: PetGlobalData = PetGlobalData()
    private val gson = Gson()
    fun getInstance(): PetGlobalData {
      return instance
    }

    fun fromJson(json: String) {
      try {
        instance = gson.fromJson(json, PetGlobalData::class.java)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    fun toJson(): String {
      return gson.toJson(instance)
    }

  }

}