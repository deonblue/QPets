package com.sevennotes.qpets.scenes.statemachine

import android.util.Log
import com.sevennotes.qpets.events.UpdateWindowEvent
import com.sevennotes.qpets.global.PetGlobalData
import com.sevennotes.qpets.scenes.PetContext
import com.sevennotes.qpets.scenes.animation.AnimationStateMachine
import com.sevennotes.qpets.scenes.animation.Effects
import com.sevennotes.qpets.scenes.animation.EffectAnimationStateMachine
import com.sevennotes.qpets.scenes.animation.PetAnimation
import com.sevennotes.qpets.scenes.behaviourtree.BTStatus.*
import com.sevennotes.qpets.scenes.behaviourtree.BehaviourTreeManager
import com.sevennotes.qpets.scenes.behaviourtree.DecNode
import com.sevennotes.qpets.scenes.behaviourtree.Node
import com.sevennotes.qpets.scenes.behaviourtree.condition
import com.sevennotes.qpets.scenes.behaviourtree.conditionAction
import com.sevennotes.qpets.scenes.behaviourtree.invertConditionAction
import com.sevennotes.qpets.scenes.behaviourtree.inverter
import com.sevennotes.qpets.scenes.behaviourtree.selector
import com.sevennotes.qpets.scenes.behaviourtree.sequence
import com.sevennotes.qpets.scenes.common.Timer
import com.sevennotes.qpets.utils.RandomUtil
import com.sevennotes.qpets.utils.TimeUtils
import korlibs.time.TimeSpan
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus

enum class PetStates : UniversalState {
  IDLE,
  WALK,
  LOOKING,
  SLEEP,
  EATING,
  PLAYING,
}

class PetBTManager(context: PetContext) : BehaviourTreeManager<PetContext>(context) {
  private val hungryTimer: Timer = Timer(10.0)
  private val idleTimer: Timer = Timer(5.0)
  private val walkTimer: Timer = Timer(3.0)
  private val sleepTimer: Timer = Timer(20.0)
  private val clickHartTimer: Timer = Timer(2.0)
  override fun createRoot(): Node<PetContext> {
    val root = DecNode<PetContext>().apply {
      beforeChildTick = { hungryUpdate() }
    }
    root.selector {

      conditionAction({ it.clickHart }) {
        if (clickHartTimer.stick(1.0)) {
          it.clickHart = false
          PetGlobalData.getInstance().updateHeart(RandomUtil.randomIn(1, 3))
        }
        FAILURE
      }

      sequence {
        condition { it.stateMachine.currentState() is PetState.IdleState }
        selector {

          condition { PetGlobalData.getInstance().hungry <= 0 }

          conditionAction({
            TimeUtils.isNight() || PetGlobalData.getInstance().strength <= 0
          }) {
            it.changeState(PetStates.SLEEP)
            SUCCESS
          }

          conditionAction({
            idleTimer.stick(1.0) && RandomUtil.random10() < 3
          }) {
            it.changeState(PetStates.WALK)
            SUCCESS
          }

        }
      }

      sequence {
        condition { it.stateMachine.currentState() is PetState.WalkState }
        selector {
          conditionAction({ walkTimer.stick(1.0) && RandomUtil.random10() < 8 }) {
            it.changeState(PetStates.IDLE)
            walkTimer.reset()
            SUCCESS
          }
          conditionAction({ RandomUtil.random100() < 8 }) {
            it.changeState(PetStates.LOOKING)
            SUCCESS
          }
        }
      }

      sequence {
        condition { it.stateMachine.currentState() is PetState.SleepState }
        sequence {
          conditionAction({ sleepTimer.stick(1.0) }) {
            PetGlobalData.getInstance().updateStrength(1)
            SUCCESS
          }
          invertConditionAction({ TimeUtils.isNight() }) {
            if (PetGlobalData.getInstance().isStrengthFull()) {
              it.changeState(PetStates.IDLE)
              SUCCESS
            } else {
              RUNNING
            }
          }
        }
      }

    }
    return root
  }

  private fun hungryUpdate() {
    if (hungryTimer.stick(1.0)) {
      if (RandomUtil.random10() < 3) {
        PetGlobalData.getInstance().updateHungry(-1)
      }
    }
  }

}

sealed class PetState(val context: PetContext) : StateImpl() {
  companion object {
    fun initStateMachine(
      context: PetContext,
    ) {
      val petBTManager = PetBTManager(context)
      petBTManager.create()
      with(context) {
        stateMachine.setBehaviourTreeManager(petBTManager)
        stateMachine.addState(PetStates.IDLE, IdleState(context))
        stateMachine.addState(PetStates.WALK, WalkState(context))
        stateMachine.addState(PetStates.LOOKING, LookingState(context))
        stateMachine.addState(PetStates.SLEEP, SleepState(context))
        stateMachine.addState(PetStates.EATING, EatingState(context))
        stateMachine.addState(PetStates.PLAYING, PlayingState(context))
      }
    }

  }

  class IdleState(context: PetContext) : PetState(context) {
    override fun onEnter() {
      context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.IDLE)
    }

  }

  class WalkState(context: PetContext) : PetState(context) {

    enum class Direction {
      LEFT,
      RIGHT
    }

    //0: left  1: right
    private var walkDirection = Direction.LEFT

    private var flipped = false

    override fun onEnter() {
      if (Math.random() > 0.5) {
        context.petAnimationStateMachine.flipSprite()
        walkDirection = Direction.RIGHT
        flipped = true
      } else {
        walkDirection = Direction.LEFT
      }
      context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.WALK)
    }

    override fun onExit() {
      if (flipped) context.petAnimationStateMachine.flipSprite()
      flipped = false
    }

    override fun update(time: TimeSpan) {
      move(time.seconds)
    }

    private fun move(delta: Double) {
      val x = if (walkDirection == Direction.LEFT) -1 else 1
      EventBus.getDefault().post(UpdateWindowEvent(deltaX = x))
    }

  }

  class LookingState(context: PetContext) : PetState(context) {

    override fun onEnter() {
      context.petAnimationStateMachine.translateAnimation(PetAnimation.LOOKING) {
        stateMachine?.changeState(PetStates.IDLE)
      }
    }

    override fun onExit() {
      PetGlobalData.getInstance().updateScore(1)
      PetGlobalData.getInstance().updateStrength(-2)
      context.effectAnimationStateMachine.showEffect(Effects.COIN)
    }

  }

  class SleepState(context: PetContext) : PetState(context) {
    override fun onEnter() {
      context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.SLEEP)
    }
  }

  class EatingState(context: PetContext) : PetState(context) {
    private val timer = Timer(2.0)
    override fun onEnter() {
      if (PetGlobalData.getInstance().isHungryFull()) {
        stateMachine?.changeState(PetStates.IDLE)
        return
      }
      context.petAnimationStateMachine.translateAnimation(PetAnimation.LOOKING) {
        stateMachine?.changeState(PetStates.IDLE)
      }
      context.effectAnimationStateMachine.showEffectOnce(Effects.FOOD)
    }

    override fun onExit() {
      PetGlobalData.getInstance().updateHungry(10)
    }
  }

  class PlayingState(context: PetContext) : PetState(context) {
    override fun onEnter() {
      context.petAnimationStateMachine.translateAnimation(PetAnimation.LOOKING) {
        stateMachine?.changeState(PetStates.IDLE)
        context.effectAnimationStateMachine.hide(Effects.BALL)
      }
      context.effectAnimationStateMachine.showEffect(Effects.BALL)
    }

    override fun onExit() {
      context.launch {
        repeat(6) {
          delay(200)
          context.effectAnimationStateMachine.showEffect(Effects.HEART)
        }
        PetGlobalData.getInstance().updateHeart(10)
      }
    }

  }

}
