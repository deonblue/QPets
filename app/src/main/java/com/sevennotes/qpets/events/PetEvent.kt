package com.sevennotes.qpets.events

/**
 * 控制宠物行为的事件，触发相应事件会使得宠物触发相应行为
 */
sealed class PetEvent {
  data object PetSleep : PetEvent()
  data object PetIdle : PetEvent()
  data object PetLooking : PetEvent()
  data object PetEating: PetEvent()
  data object PetPlaying: PetEvent()
}