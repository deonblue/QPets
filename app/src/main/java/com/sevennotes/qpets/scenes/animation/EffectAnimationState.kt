package com.sevennotes.qpets.scenes.animation

import android.util.Log
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.addUpdater
import korlibs.korge.view.rotation
import korlibs.math.geom.Anchor
import korlibs.math.geom.Angle
import korlibs.math.geom.MatrixTransform
import korlibs.math.geom.Scale
import korlibs.time.TimeSpan

class EffectAnimation {
  companion object {
    const val BALL: String = "ball"
    const val FOOD: String = "food"
  }
}

sealed class EffectAnimationState : AnimationState() {

  class BallAnimation(
    override val animation: SpriteAnimation
  ) : EffectAnimationState() {

    override fun onEnter() {
      sprite?.apply {
        x = 50f
        y = 88f
        anchor = Anchor.CENTER
        scale = Scale(0.2)
      }
    }

    override fun update(timeSpan: TimeSpan) {
      sprite?.apply {
        rotation += Angle.fromRatio(0.01)
      }
    }

    override fun onExit() {
      sprite?.apply {
        rotation = Angle.ZERO
      }
    }

  }

  class FoodAnimation(
    override val animation: SpriteAnimation
  ) : EffectAnimationState() {

    override fun onEnter() {
      sprite?.apply {
        anchor = Anchor.BOTTOM_CENTER
        x = 50f
        y = 105f
        scale = Scale(0.5)
        speed = 0.1f
      }
    }

  }

}