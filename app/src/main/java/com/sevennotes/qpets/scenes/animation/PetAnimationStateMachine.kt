package com.sevennotes.qpets.scenes.animation

import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.ImageDataContainer

class PetAnimationStateMachine(private val data: ImageDataContainer) : AnimationStateMachine() {

  init {
    addAnimation(
      PetAnimation.DIE,
      PetAnimationState.IdleAnimation(animation = data.default.getAnimation("die"))
    )
    addAnimation(
      PetAnimation.IDLE,
      PetAnimationState.IdleAnimation(animation = data.default.getAnimation("idle"))
    )
    addAnimation(
      PetAnimation.WALK,
      PetAnimationState.WalkAnimation(animation = data.default.getAnimation("walk"))
    )
    addAnimation(
      PetAnimation.WALK_D,
      PetAnimationState.WalkAnimation(animation = data.default.getAnimation("walkd"))
    )
    addAnimation(
      PetAnimation.WALK_U,
      PetAnimationState.WalkAnimation(animation = data.default.getAnimation("walku"))
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
    addAnimation(
      PetAnimation.PLAY1,
      PetAnimationState.Play1Animation(animation = data.default.getAnimation("play") {
        val newList = mutableListOf<BmpSlice>()
        for (n in 0..2) {
          newList.add(it[n].copy())
        }
        return@getAnimation newList
      })
    )
    addAnimation(
      PetAnimation.PLAYING,
      PetAnimationState.PlayingAnimation(animation = data.default.getAnimation("play") {
        val newList = mutableListOf<BmpSlice>()
        for (n in 3..7) {
          newList.add(it[n].copy())
        }
        return@getAnimation newList
      })
    )
    addAnimation(
      PetAnimation.PLAY2,
      PetAnimationState.Play2Animation(animation = data.default.getAnimation("play") {
        val newList = mutableListOf<BmpSlice>()
        for (n in 8..9) {
          newList.add(it[n].copy())
        }
        return@getAnimation newList
      })
    )
  }

}