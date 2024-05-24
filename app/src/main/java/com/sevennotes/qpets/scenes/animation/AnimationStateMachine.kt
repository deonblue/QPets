package com.sevennotes.qpets.scenes.animation

import android.util.Log
import androidx.compose.animation.core.keyframes
import korlibs.datastructure.identityHashCode
import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.ImageDataContainer
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.addUpdater
import korlibs.time.TimeSpan

open class AnimationStateMachine {

  protected val animationStates: MutableMap<String, AnimationState> = mutableMapOf()
  protected var currentAnimation: AnimationState? = null
  protected var sprite: Sprite? = null

  //水平翻转sprite
  fun flipSprite() {
    sprite?.let {
      val currScaleX = it.scaleX
      it.scaleX = -currScaleX
    }
  }

  fun addAnimation(name: String, animation: AnimationState) {
    animationStates[name] = animation
  }

  fun changeAnimationLooping(animation: String) {
    exchangeAnimation(animationStates[animation], isLooping = true)
  }

  fun translateAnimation(
    animation: String,
    onFinished: () -> Unit = {}
  ) {
    exchangeAnimation(animationStates[animation])
    sprite?.onAnimationCompleted?.once { completeAnimation ->
      if (completeAnimation.identityHashCode() == currentAnimation?.animation.identityHashCode()) {
        onFinished()
      }
    }
  }

  private fun exchangeAnimation(
    animationState: AnimationState?,
    isLooping: Boolean = false
  ) {
    sprite?.let {
      it.stopAnimation()
      it.setFrame(0)
      currentAnimation?.onExit()
      currentAnimation = animationState
//      Log.d("test", "currentAnimation: $currentAnimation onEnter!")
      currentAnimation?.onEnter()
      if (isLooping) {
        it.playAnimationLooped(currentAnimation?.animation)
      } else {
        it.playAnimation(
          spriteAnimation = currentAnimation?.animation,
        )
      }
    }
  }

  open fun createSprite(
    defaultAnimation: String,
    callback: (SpriteAnimation?) -> Sprite?
  ) {
    animationStates[defaultAnimation]?.animation?.let { animation ->
      sprite = callback(animation)
      animationStates.forEach { (_, u) ->
        u.sprite = sprite
      }
      sprite?.addUpdater {
        currentAnimation?.update(it)
      }
    } ?: {
      sprite = callback(null)
    }
  }

}