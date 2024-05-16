package com.sevennotes.qpets.scenes.animation

import android.util.Log
import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.ImageDataContainer

class PetAnimationStateMachine(private val data: ImageDataContainer) : AnimationStateMachine() {

  init {
    addAnimation(
      PetAnimation.IDLE,
      PetAnimationState.IdleAnimation(animation = data.default.getAnimation("idle"))
    )
    addAnimation(
      PetAnimation.WALK,
      PetAnimationState.WalkAnimation(animation = data.default.getAnimation("walk"))
    )
    addAnimation(
      PetAnimation.LOOKING,
      PetAnimationState.LookingAnimation(animation = data.default.getAnimation("find") {
        val newList = mutableListOf<BmpSlice>()
        for (_n in 0 until 3) {
          for (i in 0.. 3) {
            newList.add(it[i].copy())
          }
        }
        for (i in 4 .. 6) {
          newList.add(it[i].copy())
        }
        return@getAnimation newList
      })
    )
    addAnimation(
      PetAnimation.SLEEP,
      PetAnimationState.SleepAnimation(animation = data.default.getAnimation("sleep"))
    )
  }


}