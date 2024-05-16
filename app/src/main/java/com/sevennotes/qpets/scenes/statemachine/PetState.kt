package com.sevennotes.qpets.scenes.statemachine

import android.util.Log
import com.sevennotes.qpets.events.UpdateWindowEvent
import com.sevennotes.qpets.global.PetGlobalData
import com.sevennotes.qpets.scenes.animation.AnimationStateMachine
import com.sevennotes.qpets.scenes.animation.EffectAnimation
import com.sevennotes.qpets.scenes.animation.EffectAnimationStateMachine
import com.sevennotes.qpets.scenes.animation.PetAnimation
import com.sevennotes.qpets.scenes.common.Timer
import com.sevennotes.qpets.utils.TimeUtils
import korlibs.time.TimeSpan
import org.greenrobot.eventbus.EventBus

enum class PetStates : UniversalState {
  IDLE,
  WALK,
  LOOKING,
  SLEEP,
  EATING,
  PLAYING,
}

sealed class PetState(
  val animationStateMachine: AnimationStateMachine,
  val effectAnimationStateMachine: EffectAnimationStateMachine
) : StateImpl() {

  private val hungryTimer = Timer(10.0)

  override fun update(time: TimeSpan) {
    hungryUpdate(time.seconds)
  }

  private fun hungryUpdate(deltaTime: Double) {
    if (hungryTimer.stick(deltaTime)) {
      val random = Math.random()
      if (random < 0.3) {
        PetGlobalData.getInstance().updateHungry(-1)
      }
    }
  }

  companion object {
    fun initStateMachine(
      stateMachine: StateMachine,
      petAnimationStateMachine: AnimationStateMachine,
      effectAnimationStateMachine: EffectAnimationStateMachine
    ) {
      stateMachine.addState(
        PetStates.IDLE,
        IdleState(petAnimationStateMachine, effectAnimationStateMachine)
      )
      stateMachine.addState(
        PetStates.WALK,
        WalkState(petAnimationStateMachine, effectAnimationStateMachine)
      )
      stateMachine.addState(
        PetStates.LOOKING,
        LookingState(petAnimationStateMachine, effectAnimationStateMachine)
      )
      stateMachine.addState(
        PetStates.SLEEP,
        SleepState(petAnimationStateMachine, effectAnimationStateMachine)
      )
      stateMachine.addState(
        PetStates.EATING,
        EatingState(petAnimationStateMachine, effectAnimationStateMachine)
      )
      stateMachine.addState(
        PetStates.PLAYING,
        PlayingState(petAnimationStateMachine, effectAnimationStateMachine)
      )
    }

  }

  class IdleState(
    animationStateMachine: AnimationStateMachine,
    effectAnimationStateMachine: EffectAnimationStateMachine
  ) : PetState(animationStateMachine, effectAnimationStateMachine) {

    private val changePercent = 0.3
    private val gotoSleep = 0.8
    private val timer = Timer(5.0)

    override fun onEnter() {
      animationStateMachine.changeAnimationLooping(PetAnimation.IDLE)
    }

    override fun update(time: TimeSpan) {
      //get a random number between 0 and 1
      super.update(time)
      if (!timer.stick(time.seconds)) return
      val random = Math.random()

      //晚上，或者体力为0, 就回去睡觉
      if (TimeUtils.isNight() || PetGlobalData.getInstance().strength <= 0) {
        if (random <= gotoSleep) {
          stateMachine?.changeState(PetStates.SLEEP)
          return
        }
      }

      if (PetGlobalData.getInstance().hungry > 0) {
        if (random <= changePercent) {
          stateMachine?.changeState(PetStates.WALK)
          return
        }
      }

    }

  }

  class WalkState(
    animationStateMachine: AnimationStateMachine,
    effectAnimationStateMachine: EffectAnimationStateMachine
  ) : PetState(animationStateMachine, effectAnimationStateMachine) {

    enum class Direction {
      LEFT,
      RIGHT
    }

    private val changeIdle = 0.5
    private val changeLooking = 0.1
    private val timer = Timer(1.5)

    //0: left  1: right
    private var walkDirection = Direction.LEFT

    private var flipped = false

    override fun onEnter() {
      if (Math.random() > 0.5) {
        animationStateMachine.flipSprite()
        walkDirection = Direction.RIGHT
        flipped = true
      } else {
        walkDirection = Direction.LEFT
      }
      animationStateMachine.changeAnimationLooping(PetAnimation.WALK)
    }

    override fun onExit() {
      if (flipped) animationStateMachine.flipSprite()
      flipped = false
    }

    override fun update(time: TimeSpan) {
      super.update(time)
      move(time.seconds)
      if (!timer.stick(time.seconds)) return
      tryChangeState()
    }

    private fun move(delta: Double) {
      val x = if (walkDirection == Direction.LEFT) -1 else 1
      EventBus.getDefault().post(UpdateWindowEvent(deltaX = x))
    }

    private fun tryChangeState() {
      val random = Math.random()
      if (PetGlobalData.getInstance().strength >= 0) {
        if (random <= changeLooking) {
          stateMachine?.changeState(PetStates.LOOKING)
          return
        }
      }
      if (random <= changeIdle) {
        stateMachine?.changeState(PetStates.IDLE)
      }
    }

  }

  class LookingState(
    animationStateMachine: AnimationStateMachine,
    effectAnimationStateMachine: EffectAnimationStateMachine
  ) : PetState(animationStateMachine, effectAnimationStateMachine) {

    override fun onEnter() {
      animationStateMachine.translateAnimation(PetAnimation.LOOKING) {
        stateMachine?.changeState(PetStates.IDLE)
      }
    }

    override fun onExit() {
      PetGlobalData.getInstance().updateScore(1)
      PetGlobalData.getInstance().updateStrength(-2)
    }

  }

  class SleepState(
    animationStateMachine: AnimationStateMachine,
    effectAnimationStateMachine: EffectAnimationStateMachine
  ) : PetState(animationStateMachine, effectAnimationStateMachine) {

    private val tiredTimer = Timer(10.0)

    override fun onEnter() {
      animationStateMachine.changeAnimationLooping(PetAnimation.SLEEP)
    }

    override fun update(time: TimeSpan) {
      super.update(time)
      if (tiredTimer.stick(time.seconds)) {
        if (!PetGlobalData.getInstance().isStrengthFull()) {
          PetGlobalData.getInstance().updateStrength(1)
        }
      }
    }

  }

  class EatingState(
    animationStateMachine: AnimationStateMachine,
    effectAnimationStateMachine: EffectAnimationStateMachine
  ) : PetState(animationStateMachine, effectAnimationStateMachine) {
    private val timer = Timer(2.0)
    override fun onEnter() {
      if (PetGlobalData.getInstance().isHungryFull()) {
        //TODO 如果饱了就不吃了，切不吃了动画
        stateMachine?.changeState(PetStates.IDLE)
        return
      }
      animationStateMachine.translateAnimation(PetAnimation.LOOKING) {
//        Log.d("test", "eat finished!")
        stateMachine?.changeState(PetStates.IDLE)
      }
      effectAnimationStateMachine.showEffectOnce(EffectAnimation.FOOD)
    }

    override fun onExit() {
//      Log.d("test", "update hungry")
      PetGlobalData.getInstance().updateHungry(10)
    }
  }

  class PlayingState(
    animationStateMachine: AnimationStateMachine,
    effectAnimationStateMachine: EffectAnimationStateMachine
  ) : PetState(animationStateMachine, effectAnimationStateMachine) {
    override fun onEnter() {
      animationStateMachine.translateAnimation(PetAnimation.LOOKING) {
        stateMachine?.changeState(PetStates.IDLE)
        effectAnimationStateMachine.hide()
      }
      effectAnimationStateMachine.showEffect(EffectAnimation.BALL)
    }
  }

}
