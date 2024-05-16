package com.sevennotes.qpets.scenes.animation

import android.util.Log
import korlibs.image.format.ImageDataContainer
import korlibs.korge.scene.SceneContainer
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.addTo
import korlibs.korge.view.addUpdater
import korlibs.time.TimeSpan

class EffectAnimationStateMachine(
  private val data: ImageDataContainer,
  private val container: SceneContainer
) : AnimationStateMachine() {

  init {
    addAnimation(
      EffectAnimation.BALL,
      EffectAnimationState.BallAnimation(animation = data.default.getAnimation("ball"))
    )
    addAnimation(
      EffectAnimation.FOOD,
      EffectAnimationState.FoodAnimation(animation = data.default.getAnimation("food"))
    )
  }

  fun hide() {
    sprite?.stopAnimation()
    sprite?.removeFromParent()
  }

  private fun show() {
    sprite?.addTo(container)
  }

  fun showEffectOnce(animation: String) {
    if (animationStates.containsKey(animation)) {
      show()
      translateAnimation(animation) {
        hide()
      }
    }
  }

  fun showEffect(animation: String, loop: Boolean = false) {
    if (animationStates.containsKey(animation)) {
      show()
      if (loop) {
        changeAnimationLooping(animation)
      } else {
        translateAnimation(animation)
      }
    }
  }

}