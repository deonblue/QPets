package com.sevennotes.qpets.scenes.animation

import korlibs.korge.view.SpriteAnimation


class PetAnimation {
  companion object {
    const val IDLE: String = "idle"
    const val WALK: String = "walk"
    const val LOOKING: String = "looking"
    const val SLEEP: String = "sleep"
  }
}

sealed class PetAnimationState : AnimationState() {

  class IdleAnimation(
    override val animation: SpriteAnimation,
  ) : AnimationState() {
    override fun onEnter() {
      sprite?.speed = 0.3f
    }
  }

  class WalkAnimation(
    override val animation: SpriteAnimation,
  ) : AnimationState() {}

  class LookingAnimation(
    override val animation: SpriteAnimation,
  ) : AnimationState() {
    override fun onEnter() {
      sprite?.speed = 0.3f
    }
  }

  class SleepAnimation(
    override val animation: SpriteAnimation,
  ) : AnimationState() {
    override fun onEnter() {
      sprite?.speed = 0.1f
    }
  }

}