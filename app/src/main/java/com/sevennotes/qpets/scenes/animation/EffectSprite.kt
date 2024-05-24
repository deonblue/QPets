package com.sevennotes.qpets.scenes.animation

import com.sevennotes.qpets.utils.RandomUtil
import korlibs.io.async.launch
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.SceneContainer
import korlibs.korge.tween.delay
import korlibs.korge.tween.tween
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.addTo
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Anchor
import korlibs.math.geom.Angle
import korlibs.math.geom.Scale
import korlibs.time.milliseconds
import korlibs.korge.tween.get
import korlibs.math.interpolation.Easing

class Effects {
  companion object {
    const val BALL: String = "ball"
    const val FOOD: String = "food"
    const val HEART: String = "heart"
    const val COIN: String = "coin"
  }
}

sealed class EffectSprite {

  var container: SceneContainer? = null
  abstract val sprite: Sprite
  open fun show(isLooping: Boolean) {
    sprite.apply {
      if (isLooping) {
        playAnimationLooped()
      } else {
        playAnimation()
      }
      onAnimationCompleted.add {
        if (!isLooping) {
          hide()
        }
      }
      container?.let { addTo(it) }
    }
  }

  open fun hide() {
    sprite.removeFromParent()
  }

  class Ball(
    animation: SpriteAnimation
  ) : EffectSprite() {
    override val sprite = Sprite(initialAnimation = animation).apply {
      x = 50f
      y = 88f
      anchor = Anchor.CENTER
      scale = Scale(0.2)
      addUpdater {
        rotation += Angle.fromRatio(0.01)
      }
    }
  }

  class Food(
    animation: SpriteAnimation
  ) : EffectSprite() {
    override val sprite = Sprite(initialAnimation = animation).apply {
      anchor = Anchor.BOTTOM_CENTER
      x = 50f
      y = 105f
      scale = Scale(0.5)
      speed = 0.1f
    }
  }

  class Heart(
    val animation: SpriteAnimation
  ) : EffectSprite() {

    override val sprite: Sprite
      get() = Sprite(initialAnimation = animation).apply {
        anchor = Anchor.CENTER
        x = 50f + RandomUtil.random10() - 5
        y = 70f + RandomUtil.random10() - 5
        scale = Scale(0.3)
      }

    override fun show(isLooping: Boolean) {
      sprite.apply {
        container?.let {
          it.launch {
            addTo(it)
            tween(
              this::alpha[0.2].delay(50.milliseconds),
              this::y[10.0],
              easing = Easing.EASE_IN_OUT_QUAD,
              time = 1000.milliseconds
            )
            removeFromParent()
          }
        }
      }
    }
  }

  class Coin(
    val animation: SpriteAnimation,
  ) : EffectSprite() {
    override val sprite: Sprite
      get() = Sprite(initialAnimation = animation).apply {
        anchor = Anchor.CENTER
        x = 50f + RandomUtil.random10() - 5
        y = 70f + RandomUtil.random10() - 5
        scale = Scale(0.3)
      }

    override fun show(isLooping: Boolean) {
      sprite.apply {
        container?.let {
          it.launchImmediately {
            addTo(it)
            val job = it.launchImmediately {
              repeat(5) {
                tween(this::scaleX[-0.3,0.3], time = 150.milliseconds)
                tween(this::scaleX[0.3,-0.3], time = 150.milliseconds)
              }
            }
            tween(
              this::alpha[0.2].delay(50.milliseconds),
              this::y[10.0],
              easing = Easing.EASE_IN_OUT_QUAD,
              time = 1000.milliseconds
            )
            job.cancel()
            removeFromParent()
          }
        }
      }
    }
  }

}

