package com.sevennotes.qpets.pages

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.sevennotes.qpets.MyPetApplication
import com.sevennotes.qpets.events.IsUpdatingWindow
import com.sevennotes.qpets.events.PetEvent
import com.sevennotes.qpets.events.UpdateWindowEvent
import com.sevennotes.qpets.global.PetGlobalData
import com.sevennotes.qpets.viewmodel.PetViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PetView(
  private val service: LifecycleService,
  private val savedStateOwner: SavedStateRegistryOwner,
  private val viewModelStoreOwner: ViewModelStoreOwner,
) {

  private val savedStateRegistryController = SavedStateRegistryController.create(savedStateOwner)
  private lateinit var windowManager: WindowManager
  private var windowX: Int = 0
  private var windowY: Int = 0
  private var viewSize: Int = 0
  private var goingOut: Boolean = false

  private val orientationReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      Log.d("test", "receive configuration changed, intent: $intent")
      windowX = windowManager.defaultDisplay.width / 2
      windowY = windowManager.defaultDisplay.height / 2
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  val windowLayout: LayoutParams = LayoutParams().apply {
    type = LayoutParams.TYPE_APPLICATION_OVERLAY
    width = LayoutParams.WRAP_CONTENT
    height = LayoutParams.WRAP_CONTENT
    format = PixelFormat.TRANSPARENT
    flags = LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCH_MODAL
  }

  @RequiresApi(Build.VERSION_CODES.O)
  lateinit var view: ComposeView

  @RequiresApi(Build.VERSION_CODES.R)
  fun initView() {
    savedStateRegistryController.performAttach()
    savedStateRegistryController.performRestore(null)
    windowManager = service.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    MyPetApplication.getInstance()
      .registerReceiver(orientationReceiver, IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED))
    windowX = windowManager.defaultDisplay.width / 2
    windowY = windowManager.defaultDisplay.height / 2
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @Subscribe(threadMode = ThreadMode.MAIN)
  fun updateWindowEvent(event: UpdateWindowEvent) {
    updateWindowPosition(event.deltaX, event.deltaY)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun updateWindowPosition(deltaX: Int, deltaY: Int) {
    // 更新宠物位置
    with(windowLayout) {
      val newX = x + deltaX
      val newY = y + deltaY

      val maxXPosition = windowX - viewSize
      val minXPosition = -(windowX - viewSize)

      val maxYPosition = windowY - viewSize
      val minYPosition = -(windowY - viewSize)

      x = newX.coerceIn(minXPosition, maxXPosition)
      y = newY.coerceIn(minYPosition, maxYPosition)

      windowManager.updateViewLayout(view, this)
    }

//    Log.d("test", "update view location: ${windowLayout.x} ${windowLayout.y}")
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun show() {
    try {
      if (goingOut) {
        Toast.makeText(service, "宠物已经添加到主屏幕上了", Toast.LENGTH_SHORT).show()
      } else {
        view = createView()
        windowLayout.x = 0
        windowLayout.y = 0
        windowManager.addView(view, windowLayout)
        goingOut = true
        EventBus.getDefault().post(PetEvent.PetHomeState(false))
        EventBus.getDefault().register(this)
        PetGlobalData.getInstance().outSide = true
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun hide() {
    windowManager.removeView(view)
    goingOut = false
    PetGlobalData.getInstance().outSide = false
    EventBus.getDefault().post(PetEvent.PetHomeState(true))
    EventBus.getDefault().unregister(this)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun createView(): ComposeView {
    return ComposeView(service).apply {

      setViewTreeSavedStateRegistryOwner(savedStateOwner)
      setViewTreeLifecycleOwner(service)
      setViewTreeViewModelStoreOwner(viewModelStoreOwner)

      setBackgroundColor(Color.TRANSPARENT)

      setContent {

        val viewModel: PetViewModel = viewModel()
        val density = LocalDensity.current

        val windowState = viewModel.windowState.collectAsState().value
        LaunchedEffect(Unit) {
          viewModel.windowState.collect {
            with(density) {
              viewSize = it.size.toPx().toInt() / 2
            }
          }
        }

        val viewConfiguration = LocalViewConfiguration.current

        Box(modifier = Modifier
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = {
                EventBus.getDefault().post(IsUpdatingWindow(updating = true))
              },
              onDrag = { _, dragAmount ->
                updateWindowPosition(dragAmount.x.toInt(), dragAmount.y.toInt())
              },
              onDragEnd = {
                EventBus.getDefault().post(IsUpdatingWindow(updating = false))
              },
              onDragCancel = {
                EventBus.getDefault().post(IsUpdatingWindow(updating = false))
              }
            )
          }
        ) {
          GameMain(windowState)
        }
      }
    }
  }

  val savedStateRegistry: SavedStateRegistry
    get() = savedStateRegistryController.savedStateRegistry
}