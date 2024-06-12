package com.sevennotes.qpets.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevennotes.qpets.events.GlobalDataEvent
import com.sevennotes.qpets.global.PetGlobalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
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

  val mainUIState = flow {
    emit(MainUIState())
  }.combine(PetGlobalData.getInstance().hungryFlow) { state, hungry ->
    state.copy(hungry = hungry)
  }.combine(PetGlobalData.getInstance().strengthFlow) { state, strength ->
    state.copy(strength = strength)
  }.combine(PetGlobalData.getInstance().scoreFlow) { state, score ->
    state.copy(score = score)
  }.combine(PetGlobalData.getInstance().heartFlow) { state, heart ->
    state.copy(heart = heart)
  }

}