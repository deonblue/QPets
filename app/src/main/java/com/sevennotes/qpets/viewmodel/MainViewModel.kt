package com.sevennotes.qpets.viewmodel

import androidx.lifecycle.ViewModel
import com.sevennotes.qpets.events.GlobalDataEvent
import com.sevennotes.qpets.global.PetGlobalData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

data class MainUIState(
  val hungry: Int = 0,
  val strength: Int = 0,
  val score: Int = 0,
  val heart: Int = 0,
)

class MainViewModel : ViewModel() {

  private val _mainUIState = MutableStateFlow(MainUIState(
    hungry = PetGlobalData.getInstance().hungry,
    strength = PetGlobalData.getInstance().strength,
    score = PetGlobalData.getInstance().score,
    heart = PetGlobalData.getInstance().heart,
  ))
  val mainUIState = _mainUIState.asStateFlow()

  init {
    EventBus.getDefault().register(this)
  }

  @Subscribe
  fun globalDataUpdate(event: GlobalDataEvent) {
    when (event) {
      is GlobalDataEvent.HungryEvent -> {
        _mainUIState.update { it.copy(hungry = event.value) }
      }
      is GlobalDataEvent.ScoreEvent -> {
        _mainUIState.update { it.copy(score = event.value) }
      }
      is GlobalDataEvent.StrengthEvent -> {
        _mainUIState.update { it.copy(strength = event.value) }
      }
      is GlobalDataEvent.HartEvent -> {
        _mainUIState.update { it.copy(heart = event.value) }
      }
      else -> {}
    }
  }

  override fun onCleared() {
    EventBus.getDefault().unregister(this)
  }

}