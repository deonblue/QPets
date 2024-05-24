package com.sevennotes.qpets.scenes.animation

import korlibs.image.format.ImageDataContainer
import korlibs.korge.scene.SceneContainer

class EffectAnimationStateMachine(
  private val data: ImageDataContainer,
  private val container: SceneContainer
) {

  private val effects: MutableMap<String, EffectSprite> = mutableMapOf()
  private fun addEffect(name: String, effect: EffectSprite) {
    effect.container = container
    effects[name] = effect
  }

  init {
    addEffect(
      Effects.BALL,
      EffectSprite.Ball(animation = data.default.getAnimation("ball"))
    )
    addEffect(
      Effects.FOOD,
      EffectSprite.Food(animation = data.default.getAnimation("food"))
    )
    addEffect(
      Effects.HEART,
      EffectSprite.Heart(animation = data.default.getAnimation("hart"))
    )
    addEffect(
      Effects.COIN,
      EffectSprite.Coin(animation = data.default.getAnimation("coin"))
    )
  }

  fun hide(effect: String) {
    effects[effect]?.hide()
  }

  private fun show(effect: String, isLooping: Boolean) {
    effects[effect]?.show(isLooping)
  }

  fun showEffectOnce(effect: String) {
    if (effects.containsKey(effect)) {
      show(effect, false)
    }
  }

  fun showEffect(effect: String) {
    if (effects.containsKey(effect)) {
      show(effect, true)
    }
  }

}