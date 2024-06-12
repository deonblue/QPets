package com.sevennotes.qpets.global

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.gson.Gson
import com.sevennotes.qpets.MyPetApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class PetGlobalData private constructor(
  private val dataStore: DataStore<Preferences>
) {

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

  /**
   * 心情
   */
  var heart: Int = 0
    private set

  var outSide: Boolean = false

  private val hungryKey = intPreferencesKey("hungry")
  private val strengthKey = intPreferencesKey("strength")
  private val scoreKey = intPreferencesKey("score")
  private val heartKey = intPreferencesKey("heart")

  val hungryFlow: Flow<Int> = dataStore.data.map { preference ->
    hungry = preference[hungryKey] ?: hungry
    hungry
  }
  val strengthFlow: Flow<Int> = dataStore.data.map { preference ->
    strength = preference[strengthKey] ?: strength
    strength
  }
  val scoreFlow: Flow<Int> = dataStore.data.map { preference ->
    score = preference[scoreKey] ?: score
    score
  }
  val heartFlow: Flow<Int> = dataStore.data.map { preference ->
    heart = preference[heartKey] ?: heart
    heart
  }

  //更新属性的通用方法
  private fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
    MyPetApplication.getInstance().scope.launch {
      dataStore.edit { preferences ->
        preferences[key] = value
      }
    }
  }

  fun updateHungry(value: Int) {
    updatePreference(hungryKey, (hungry + value).coerceIn(0, 100))
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
    updatePreference(strengthKey, (strength + value).coerceIn(0, 10))
  }

  fun updateScore(value: Int) {
    var newScore = score + value

    if (newScore < 0) {
      newScore = 0
    }
    updatePreference(scoreKey, newScore)
  }

  fun updateHeart(value: Int) {
    updatePreference(heartKey, (heart + value).coerceIn(0, 100))
  }

  companion object {
    private var instance: PetGlobalData? = null
    private val gson = Gson()
    fun getInstance(): PetGlobalData {
      if (instance == null) {
        instance = PetGlobalData(
          PreferenceDataStoreFactory.create {
            File(MyPetApplication.getInstance().filesDir, "pet_data.preferences_pb").apply {
              if (!exists()) {
                createNewFile()
              }
            }
          }
        )
      }
      return instance!!
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