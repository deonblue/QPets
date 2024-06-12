package com.sevennotes.qpets.viewmodel

import android.graphics.Point
import android.util.Log
import android.util.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sevennotes.qpets.pages.PetView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

data class WindowState(
  val size: Dp = 80.dp,
  val showMenu: Boolean = false,
)

sealed class WindowEvent {
  data class ChangeSize(val size: Dp) : WindowEvent()
  data object MenuShow : WindowEvent()
}

class PetViewModel : ViewModel() {

  private val _windowState = MutableStateFlow(WindowState())
  val windowState = _windowState.asStateFlow()

  private val _windowPosition = MutableStateFlow(Point(0, 0))
  val windowPosition = _windowPosition.asStateFlow()

  init {
    EventBus.getDefault().register(this)
  }

  fun updateWindowPosition(deltaX: Int, deltaY: Int) {

  }

  override fun onCleared() {
    super.onCleared()
    EventBus.getDefault().unregister(this)
  }

  @Subscribe
  fun onEvent(windowEvent: WindowEvent) {
    when (windowEvent) {
      is WindowEvent.ChangeSize -> { changeSize(windowEvent.size) }
      is WindowEvent.MenuShow -> { menuShow() }
    }
  }

  fun changeSize(value: Dp) {
    _windowState.update {
      val newSize = it.size + value
      it.copy(size = newSize)
    }
  }

  fun menuShow() {
    _windowState.update {
      it.copy(showMenu = !it.showMenu)
    }
  }

}