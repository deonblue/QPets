package com.sevennotes.qpets.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class ProgressBarState(
  progress: Int = 0,
  frontColor: Color = Color.Green,
  backgroundColor: Color = Color.Black,
  val maxProgressValue: Int = 100,
) {
  private var _progress by mutableIntStateOf(progress)
  var progress: Int
    get() = _progress
    set(value) {
      _progress = value.coerceIn(0, maxProgressValue)
      val colorRed = 1 - (value.toFloat() / maxProgressValue.toFloat())
      val colorGreen = value.toFloat() / maxProgressValue.toFloat()
      frontColor = frontColor.copy(green = colorGreen, red = colorRed)
    }
  var frontColor by mutableStateOf(frontColor)
  var backgroundColor by mutableStateOf(backgroundColor)
}

@Composable
fun rememberProgressBarState(
  initialProgress: Int = 0,
  initialFrontColor: Color = Color.Green,
  initialBackgroundColor: Color = Color.Black,
  initMaxProgressValue: Int = 100,
): ProgressBarState {
  val progressBarState = remember {
    ProgressBarState(
      initialProgress,
      initialFrontColor,
      initialBackgroundColor,
      initMaxProgressValue,
    )
  }
  return progressBarState
}

@Composable
fun ProgressBar(
  modifier: Modifier = Modifier,
  progressBarState: ProgressBarState = rememberProgressBarState(),
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color = progressBarState.backgroundColor)
      .width(100.dp)
      .height(10.dp),
  ) {
    val widthPercent =
      progressBarState.progress.toFloat() / progressBarState.maxProgressValue.toFloat()
    Box(
      modifier = Modifier
        .background(color = progressBarState.frontColor)
        .fillMaxWidth(widthPercent)
        .fillMaxHeight(),
    )
    Text(
      modifier = Modifier
        .padding(start = 10.dp)
        .align(Alignment.CenterStart),
      text = "${progressBarState.progress}/${progressBarState.maxProgressValue}",
      color = if (widthPercent < 0.4) Color.White else Color.Black
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewProgressBar() {
  val p = rememberProgressBarState(
    initialBackgroundColor = Color(0.157f, 0.153f, 0.227f, 1.0f),
    initMaxProgressValue = 150,
    initialProgress = 100,
  )
  LaunchedEffect(Unit) {
    while (true) {
      delay(50)
      p.progress -= 1
      if (p.progress <= 0) {
        p.progress = p.maxProgressValue
      }
    }
  }
  Box(
    modifier = Modifier
      .background(Color.White)
      .size(120.dp, 40.dp)
  ) {
    ProgressBar(
      modifier = Modifier
        .align(Alignment.Center)
        .height(20.dp)
        .width(100.dp), progressBarState = p
    )
  }
}