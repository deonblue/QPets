package com.sevennotes.qpets.scenes.statemachine

import com.sevennotes.qpets.events.UpdateWindowEvent
import com.sevennotes.qpets.global.PetGlobalData
import com.sevennotes.qpets.scenes.PetContext
import com.sevennotes.qpets.scenes.animation.Effects
import com.sevennotes.qpets.scenes.animation.PetAnimation
import com.sevennotes.qpets.scenes.behaviourtree.BTStatus.FAILURE
import com.sevennotes.qpets.scenes.behaviourtree.BTStatus.RUNNING
import com.sevennotes.qpets.scenes.behaviourtree.BTStatus.SUCCESS
import com.sevennotes.qpets.scenes.behaviourtree.BehaviourTreeManager
import com.sevennotes.qpets.scenes.behaviourtree.DecNode
import com.sevennotes.qpets.scenes.behaviourtree.Node
import com.sevennotes.qpets.scenes.behaviourtree.condition
import com.sevennotes.qpets.scenes.behaviourtree.conditionAction
import com.sevennotes.qpets.scenes.behaviourtree.invertConditionAction
import com.sevennotes.qpets.scenes.behaviourtree.selector
import com.sevennotes.qpets.scenes.behaviourtree.sequence
import com.sevennotes.qpets.scenes.common.HEART_TIMER
import com.sevennotes.qpets.scenes.common.HUNGRY_TIMER
import com.sevennotes.qpets.scenes.common.IDLE_TIMER
import com.sevennotes.qpets.scenes.common.IDLE_TO_WALK_RATE
import com.sevennotes.qpets.scenes.common.LOOKING_RATE
import com.sevennotes.qpets.scenes.common.SLEEP_TIMER
import com.sevennotes.qpets.scenes.common.Timer
import com.sevennotes.qpets.scenes.common.WALK_TIMER
import com.sevennotes.qpets.scenes.common.WALK_TO_IDLE_RATE
import com.sevennotes.qpets.utils.RandomUtil
import com.sevennotes.qpets.utils.TimeUtils
import korlibs.time.TimeSpan
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus

enum class PetStates : UniversalState {
  IDLE,
  DIE,
  WALK,
  LOOKING,
  SLEEP,
  EATING,
  PLAYING,
  PLAYING_END,
}

class PetBTManager(context: PetContext) : BehaviourTreeManager<PetContext>(context) {
  private val hungryTimer: Timer = Timer(HUNGRY_TIMER)
  private val heartTimer: Timer = Timer(HEART_TIMER)
  private val idleTimer: Timer = Timer(IDLE_TIMER)
  private val walkTimer: Timer = Timer(WALK_TIMER)
  private val sleepTimer: Timer = Timer(SLEEP_TIMER)
  private val playingTimer: Timer = Timer(2.0)

  override fun createRoot(): Node<PetContext> {
    val root = DecNode<PetContext>().apply {
      beforeChildTick = {
        hungryUpdate()
        heartUpdate()
      }
    }
    root.selector {

      sequence {
        condition {
          //如果当前状态为在外面，则返回true
          PetGlobalData.getInstance().outSide
        }

        //所有这里面放的都是只有在外面才会有的树枝
        selector {
          sequence {
            condition { it.isInState<PetState.IdleState>() }
            selector {
//              condition { PetGlobalData.getInstance().hungry <= 0 }
              conditionAction({
                TimeUtils.isNight() || PetGlobalData.getInstance().strength <= 0
              }) {
                it.changeState(PetStates.SLEEP)
                SUCCESS
              }

              conditionAction({
                idleTimer.stick(1.0) && RandomUtil.random10() < IDLE_TO_WALK_RATE
              }) {
                it.changeState(PetStates.WALK)
                SUCCESS
              }

            }
          }

          sequence {
            condition { it.isInState<PetState.WalkState>() }
            selector {
              conditionAction({ walkTimer.stick(1.0) && RandomUtil.random10() < WALK_TO_IDLE_RATE }) {
                it.changeState(PetStates.IDLE)
                walkTimer.reset()
                SUCCESS
              }
              conditionAction({ RandomUtil.random100() < LOOKING_RATE }) {
                it.changeState(PetStates.LOOKING)
                SUCCESS
              }
            }
          }
        }

      }

      conditionAction({ PetGlobalData.getInstance().hungry <= 0 && !it.isInState<PetState.EatingState>() }) {
        it.changeState(PetStates.DIE)
        SUCCESS
      }

      condition {
        val state = it.isInState<PetState.DieState>()
        return@condition state
      }

      conditionAction({ !it.clickHart && it.isInState<PetState.PlayingState>() }) {
        if (playingTimer.stick(1.0)) {
          PetGlobalData.getInstance().updateHeart(RandomUtil.randomIn(1, 3))
          it.changeState(PetStates.PLAYING_END)
          SUCCESS
        } else {
          FAILURE
        }
      }
      sequence {
        condition { it.isInState<PetState.SleepState>() }
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

  private fun heartUpdate() {
    if (heartTimer.stick(1.0)) {
      if (RandomUtil.random100() < 100 - PetGlobalData.getInstance().hungry) {
        PetGlobalData.getInstance().updateHeart(-RandomUtil.random10())
      }
    }
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
        stateMachine.addState(PetStates.PLAYING_END, PlayingEndState(context))
        stateMachine.addState(PetStates.DIE, DieState(context))
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
      RIGHT,
      UP,
      DOWN,
    }

    private fun randomDirection(): Direction {
      val r = RandomUtil.randomIn(0, 3)
      return when(r) {
        0 -> Direction.LEFT
        1 -> Direction.RIGHT
        2 -> Direction.UP
        3 -> Direction.DOWN
        else -> Direction.LEFT
      }
    }

    //0: left  1: right
    private var walkDirection = Direction.LEFT

    private var flipped = false

    override fun onEnter() {
      walkDirection = randomDirection()
      when(walkDirection) {
        Direction.LEFT -> {
          context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.WALK)
        }
        Direction.RIGHT -> {
          context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.WALK)
          context.petAnimationStateMachine.flipSprite()
          flipped = true
        }
        Direction.UP -> {
          context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.WALK_U)
        }
        Direction.DOWN -> {
          context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.WALK_D)
        }
      }
    }

    override fun onExit() {
      if (flipped) context.petAnimationStateMachine.flipSprite()
      flipped = false
    }

    override fun update(time: TimeSpan) {
      move()
    }

    private fun move() {
      var deltaX = 0
      var deltaY = 0
      when(walkDirection) {
        Direction.LEFT -> deltaX = -1
        Direction.RIGHT -> deltaX = 1
        Direction.UP -> deltaY = -1
        Direction.DOWN -> deltaY = 1
      }
      EventBus.getDefault().post(UpdateWindowEvent(deltaX = deltaX, deltaY = deltaY))
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
      context.petAnimationStateMachine.translateAnimation(PetAnimation.PLAY1) {
        context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.PLAYING)
      }
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

  class PlayingEndState(context: PetContext) : PetState(context) {
    override fun onEnter() {
      context.petAnimationStateMachine.translateAnimation(PetAnimation.PLAY2) {
        stateMachine?.changeState(PetStates.IDLE)
      }
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

  class DieState(context: PetContext) : PetState(context) {
    override fun onEnter() {
      context.petAnimationStateMachine.changeAnimationLooping(PetAnimation.DIE)
    }

  }

}
