package com.sevennotes.qpets.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sevennotes.qpets.R
import com.sevennotes.qpets.events.PetEvent
import com.sevennotes.qpets.scenes.mainStage
import com.sevennotes.qpets.service.PetService
import com.sevennotes.qpets.ui.components.ProgressBar
import com.sevennotes.qpets.ui.components.rememberProgressBarState
import com.sevennotes.qpets.ui.theme.QPetsTheme
import com.sevennotes.qpets.viewmodel.MainUIState
import com.sevennotes.qpets.viewmodel.MainViewModel
import korlibs.image.color.Colors
import korlibs.korge.KorgeConfig
import korlibs.korge.android.KorgeAndroidView
import korlibs.korge.view.Stage
import korlibs.math.geom.Size
import org.greenrobot.eventbus.EventBus


class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val mainViewModel: MainViewModel = viewModel()
      val uiState = mainViewModel.mainUIState.collectAsState(initial = MainUIState()).value
      QPetsTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
          Greeting(
            uiState = uiState,
            点击释放 = {
              checkPermissionToOpen {
                val intent = Intent(this, PetService::class.java)
                intent.putExtra("action", "start")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  startForegroundService(intent)
                } else {
                  startService(intent)
                }
              }
            },
            点击收回 = {
              checkPermissionToOpen {
                val intent = Intent(this, PetService::class.java)
                intent.putExtra("action", "end")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  startForegroundService(intent)
                } else {
                  startService(intent)
                }
              }
            })
        }
      }
    }
  }

  private fun checkPermissionToOpen(block: () -> Unit) {
    var result = false
    try {
      val clazz: Class<*> = Settings::class.java
      val canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
      result = canDrawOverlays.invoke(null, this) as Boolean
    } catch (e: Exception) {
      e.printStackTrace()
    }
    if (result) {
      block()
    } else {
      startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
        data = Uri.parse("package:${packageName}")
      }, 1001)
    }
  }

}

@Composable
fun TextProgress(modifier: Modifier = Modifier, name: String, progress: Int, maxProgress: Int) {
  val progressState = rememberProgressBarState(
    initialProgress = progress,
    initMaxProgressValue = maxProgress,
    initialBackgroundColor = Color.Gray
  )
  LaunchedEffect(key1 = progress) {
    progressState.progress = progress
  }
  Row(modifier = modifier) {
    Text(text = name, color = Color.White)
    ProgressBar(modifier = Modifier.size(100.dp, 20.dp), progressBarState = progressState)
  }
}

@Composable
fun Greeting(
  modifier: Modifier = Modifier,
  uiState: MainUIState,
  点击释放: () -> Unit,
  点击收回: () -> Unit,
) {

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(color = Color.Black)
  ) {
    var width by remember { mutableIntStateOf(0) }
    var height by remember { mutableIntStateOf(0) }
    AndroidView(modifier = Modifier
      .fillMaxSize()
      .onSizeChanged {
        width = it.width
        height = it.height
      },
      factory = {
        KorgeAndroidView(it).apply {
        }
      },
      update = {
        if (width == 0 || height == 0) return@AndroidView
        it.loadModule(
          config = KorgeConfig(
            backgroundColor = Colors.BLACK,
            windowSize = Size(width.toFloat(), height.toFloat()),
            main = Stage::mainStage
          )
        )
      }
    )
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    Spacer(modifier = Modifier.height(20.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(end = 20.dp),
      contentAlignment = Alignment.TopEnd
    ) {
      Row {
        Row(modifier = Modifier.padding(start = 10.dp, top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(modifier = Modifier.size(45.dp), painter = painterResource(id = R.drawable.gameiconscrowncoin), contentDescription = "", tint = Color.Yellow)
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "${uiState.score}", color = Color.Yellow, fontSize = 25.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
          TextProgress(name = "饥饿值: ", progress = uiState.hungry, maxProgress = 100)
          Spacer(modifier = Modifier.height(5.dp))
          TextProgress(name = "体力: ", progress = uiState.strength, maxProgress = 10)
          Spacer(modifier = Modifier.height(5.dp))
          TextProgress(name = "心情: ", progress = uiState.heart, maxProgress = 100)
          Spacer(modifier = Modifier.height(5.dp))
        }
      }
    }
    Spacer(modifier = Modifier.height(20.dp))
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.BottomStart
    ) {
      Column(modifier = Modifier.padding(start = 20.dp, bottom = 20.dp)) {
        Button(onClick = 点击释放) {
          Text(
            text = "释放宠物",
            modifier = modifier
          )
        }
        Button(onClick = 点击收回) {
          Text(text = "收回宠物")
        }
        Button(onClick = {
          EventBus.getDefault().post(PetEvent.PetEating)
        }) {
          Text(text = "吃饭")
        }
//        Button(onClick = {
//          EventBus.getDefault().post(PetEvent.PetIdle)
//        }) {
//          Text(text = "起床")
//        }
//        Button(onClick = {
//          EventBus.getDefault().post(PetEvent.PetPlaying)
//        }) {
//          Text(text = "玩")
//        }
//        Button(onClick = {
//          EventBus.getDefault().post(PetEvent.PetLooking)
//        }) {
//          Text(text = "挖")
//        }
//        Button(onClick = {
//          EventBus.getDefault().post(PetEvent.PetSleep)
//        }) {
//          Text(text = "睡觉")
//        }
      }
    }


//    CompositionLocalProvider(
//      LocalViewModelStoreOwner provides MyPetApplication.getInstance()
//    ) {
//      val petViewModel: PetViewModel = viewModel()
//      Button(onClick = {
//        Log.d("test", "change big!!")
//        petViewModel.changeSize(5.dp)
//      }) {
//        Text(text = "变大")
//      }
//
//      Button(onClick = {
//        Log.d("test", "change small!!")
//        petViewModel.changeSize((-5).dp)
//      }) {
//        Text(text = "变小")
//      }
//    }

  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  QPetsTheme {
    Box(modifier = Modifier.fillMaxSize()) {
      Greeting(uiState = MainUIState(
        hungry = 50,
        strength = 4,
        heart = 100,
      ), 点击释放 = {}, 点击收回 = {})
    }
  }
}