package com.sevennotes.qpets.utils

import java.util.Calendar

class TimeUtils {

  companion object {

    fun isNight(): Boolean {
      val calendar = Calendar.getInstance()
      val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
      return currentHour !in 8..22
    }

  }

}