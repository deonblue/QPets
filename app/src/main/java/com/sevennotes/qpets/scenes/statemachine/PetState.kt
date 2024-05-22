package com.sevennotes.qpets.scenes.statemachine

import android.util.Log
import com.sevennotes.qpets.events.UpdateWindowEvent
import com.sevennotes.qpets.global.PetGlobalData
import com.sevennotes.qpets.scenes.animation.AnimationStateMachine
import com.sevennotes.qpets.scenes.animation.EffectAnimation
import com.sevennotes.qpets.scenes.animation.EffectAnimationStateMachine
import com.sevennotes.qpets.scenes.animation.PetAnimation
import com.sevennotes.qpets.scenes.behaviourtree.BTStatus.*
import com.sevennotes.qpets.scenes.behaviourtree.BehaviourTreeManager
import com.sevennotes.qpets.scenes.behaviourtree.DecNode
import com.sevennotes.qpets.scenes.behaviourtree.Node
import com.sevennotes.qpets.scenes.behaviourtree.action
import com.sevennotes.qpets.scenes.behaviourtree.condition
import com.sevennotes.qpets.scenes.behaviourtree.conditionAction
import com.sevennotes.qpets.scenes.behaviourtree.invertConditionAction
import com.sevennotes.qpets.scenes.behaviourtree.selector
import com.sevennotes.qpets.scenes.behaviourtree.sequence
import com.sevennotes.qpets.scenes.common.Timer
import com.sevennotes.qpets.utils.RandomUtil
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

class PetBTManager : BehaviourTreeManager<StateMachine>() {
  private val hungryTimer: Timer = Timer(10.0)
  private val idleTimer: Timer = Timer(5.0)
  private val walkTimer: Timer = Timer(3.0)
  private val sleepTimer: Timer = Timer(20.0)
  override fun createRoot(): Node<StateMachine> {
    val root = DecNode<StateMachine>().apply {
      beforeChildTick = { hungryUpdate() }
    }
    root.selector {

      sequence {
        condition { it.currentState() is PetState.IdleState }
        selector {

          condition { PetGlobalData.getInstance().hungry <= 0  }

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
        condition { it.currentState() is PetState.WalkState }
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
        condition { it.currentState() is PetState.SleepState }
        sequence {
          conditionAction({ sleepTimer.stick(1.0) }) {
            PetGlobalData.getInstance().updateStrength(1)
            SUCCESS
          }
          invertConditionAction({ TimeUtils.isNight() })  {
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

sealed class PetState(
  val animationStateMachine: AnimationStateMachine,
  val effectAnimationStateMachine: EffectAnimationStateMachine
) : StateImpl() {
  companion object {
    fun initStateMachine(
      stateMachine: StateMachine,
      petAnimationStateMachine: AnimationStateMachine,
      effectAnimationStateMachine: EffectAnimationStateMachine
    ) {
      val petBTManager = PetBTManager()
      petBTManager.create()
      stateMachine.setBehaviourTreeManager(petBTManager)
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
    override fun onEnter() {
      animationStateMachine.changeAnimationLooping(PetAnimation.IDLE)
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
      move(time.seconds)
    }

    private fun move(delta: Double) {
      val x = if (walkDirection == Direction.LEFT) -1 else 1
      EventBus.getDefault().post(UpdateWindowEvent(deltaX = x))
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
    override fun onEnter() {
      animationStateMachine.changeAnimationLooping(PetAnimation.SLEEP)
    }
  }

  class EatingState(
    animationStateMachine: AnimationStateMachine,
    effectAnimationStateMachine: EffectAnimationStateMachine
  ) : PetState(animationStateMachine, effectAnimationStateMachine) {
    private val timer = Timer(2.0)
    override fun onEnter() {
      if (PetGlobalData.getInstance().isHungryFull()) {
        stateMachine?.changeState(PetStates.IDLE)
        return
      }
      animationStateMachine.translateAnimation(PetAnimation.LOOKING) {
        stateMachine?.changeState(PetStates.IDLE)
      }
      effectAnimationStateMachine.showEffectOnce(EffectAnimation.FOOD)
    }

    override fun onExit() {
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
