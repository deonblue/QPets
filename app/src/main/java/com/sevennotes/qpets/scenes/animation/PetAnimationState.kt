package com.sevennotes.qpets.scenes.animation

import korlibs.korge.view.SpriteAnimation


class PetAnimation {
  companion object {
    const val IDLE: String = "idle"
    const val DIE: String = "die"
    const val WALK: String = "walk"
    const val WALK_D: String = "walkd"
    const val WALK_U: String = "walku"
    const val LOOKING: String = "looking"
    const val SLEEP: String = "sleep"
    const val PLAY1: String = "play1"
    const val PLAYING: String = "playing"
    const val PLAY2: String = "play2"
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
  ) : AnimationState() {
    override fun onEnter() {
      sprite?.speed = 0.3f
    }
  }

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

  class Play1Animation(
    override val animation: SpriteAnimation,
  ) : AnimationState() {
    override fun onEnter() {
      sprite?.speed = 0.3f
    }

  }

  class PlayingAnimation(
    override val animation: SpriteAnimation,
  ) : AnimationState() {
    override fun onEnter() {
      sprite?.speed = 0.3f
    }

  }

  class Play2Animation(
    override val animation: SpriteAnimation,
  ) : AnimationState() {
    override fun onEnter() {
      sprite?.speed = 0.3f
    }

  }

}

















