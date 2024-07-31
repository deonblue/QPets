package com.sevennotes.qpets.pages

import android.graphics.PixelFormat
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.viewinterop.AndroidView
import com.sevennotes.qpets.R
import com.sevennotes.qpets.events.PetEvent
import com.sevennotes.qpets.scenes.PetScene
import com.sevennotes.qpets.viewmodel.WindowState
import korlibs.image.color.Colors
import korlibs.korge.KorgeConfig
import korlibs.korge.android.KorgeAndroidView
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size
import org.greenrobot.eventbus.EventBus

@Composable
fun GameMain(
  windowState: WindowState
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    AndroidView(modifier = Modifier
      .size(windowState.size)
      .background(color = androidx.compose.ui.graphics.Color.Blue),
      factory = {
        KorgeAndroidView(it).apply {
          loadModule(config = KorgeConfig(
            backgroundColor = Colors.TRANSPARENT,
            windowSize = Size(100, 100),
            main = {
              injector.mapSingleton { PetScene() }
              val sceneContainer = sceneContainer()
              sceneContainer.changeTo<PetScene>()
            }
          ))
          mGLView?.holder?.setFormat(PixelFormat.TRANSPARENT)
        }
      },
      update = {
        Log.d("test", "update!!")
      }
    )
    if (windowState.showMenu) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        IconButton(
          modifier = Modifier.size(windowState.size / 3),
          onClick = {
            EventBus.getDefault().post(PetEvent.PetEating)
          }
        ) {
          Box {
            Image(
              bitmap = ImageBitmap.imageResource(id = R.drawable.rice),
              null
            )
          }
        }
//        Spacer(modifier = Modifier.width(windowState.size / 10))
//        IconButton(
//          modifier = Modifier.size(windowState.size / 3),
//          onClick = {
//          EventBus.getDefault().post(PetEvent.PetPlaying)
//        }) {
//          Box(modifier = Modifier.offset(y = 3.dp)) {
//            Image(
//              bitmap = ImageBitmap.imageResource(id = R.drawable.maoxian2),
//              null
//            )
//          }
//        }
      }
    }
  }
}
