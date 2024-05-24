package com.sevennotes.qpets.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sevennotes.qpets.MyPetApplication
import com.sevennotes.qpets.events.PetEvent
import com.sevennotes.qpets.service.PetService
import com.sevennotes.qpets.viewmodel.PetViewModel
import com.sevennotes.qpets.ui.theme.QPetsTheme
import com.sevennotes.qpets.viewmodel.MainViewModel
import org.greenrobot.eventbus.EventBus


class MainActivity : ComponentActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      QPetsTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colorScheme.background) {
          Greeting(
            点击释放 = {
              checkPermissionToOpen {
                val intent = Intent(this, PetService::class.java)
                intent.putExtra("action", "start")
                startService(intent)
              }
            },
            点击收回 = {
              checkPermissionToOpen {
                val intent = Intent(this, PetService::class.java)
                intent.putExtra("action", "end")
                startService(intent)
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
fun Greeting(modifier: Modifier = Modifier, 点击释放: () -> Unit, 点击收回: () -> Unit) {
  val context = LocalContext.current
  Column {
    val mainViewModel: MainViewModel = viewModel()
    val uiState = mainViewModel.mainUIState.collectAsState().value
    Text(text = "hungry: ${uiState.hungry}")
    Text(text = "strength: ${uiState.strength}")
    Text(text = "score: ${uiState.score}")
    Text(text = "heart: ${uiState.heart}")
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
      EventBus.getDefault().post(PetEvent.PetLooking)
    }) {
      Text(text = "挖")
    }
    Button(onClick = {
      EventBus.getDefault().post(PetEvent.PetSleep)
    }) {
      Text(text = "睡觉")
    }
    Button(onClick = {
      EventBus.getDefault().post(PetEvent.PetIdle)
    }) {
      Text(text = "起床")
    }
    Button(onClick = {
      EventBus.getDefault().post(PetEvent.PetEating)
    }) {
      Text(text = "吃饭")
    }
    Button(onClick = {
      EventBus.getDefault().post(PetEvent.PetPlaying)
    }) {
      Text(text = "玩")
    }

    CompositionLocalProvider(
      LocalViewModelStoreOwner provides MyPetApplication.getInstance()
    ) {
      val petViewModel: PetViewModel = viewModel()
      Button(onClick = {
        Log.d("test", "change big!!")
        petViewModel.changeSize(5.dp)
      }) {
        Text(text = "变大")
      }

      Button(onClick = {
        Log.d("test", "change small!!")
        petViewModel.changeSize((-5).dp)
      }) {
        Text(text = "变小")
      }
    }

  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  QPetsTheme {
  }
}