package com.sevennotes.qpets.scenes.animation

import com.sevennotes.qpets.scenes.interfaces.State
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.time.TimeSpan


abstract class AnimationState : State {
  internal abstract val animation: SpriteAnimation
  var sprite: Sprite? = null
}


