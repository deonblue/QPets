package com.sevennotes.qpets.scenes

import android.util.Log
import com.sevennotes.qpets.events.IsUpdatingWindow
import com.sevennotes.qpets.events.PetEvent
import com.sevennotes.qpets.scenes.animation.EffectAnimationStateMachine
import com.sevennotes.qpets.scenes.animation.Effects
import com.sevennotes.qpets.scenes.animation.PetAnimation
import com.sevennotes.qpets.scenes.animation.PetAnimationStateMachine
import com.sevennotes.qpets.scenes.common.Timer
import com.sevennotes.qpets.scenes.statemachine.PetState
import com.sevennotes.qpets.scenes.statemachine.PetStates
import com.sevennotes.qpets.scenes.statemachine.StateImpl
import com.sevennotes.qpets.scenes.statemachine.StateMachine
import com.sevennotes.qpets.scenes.statemachine.UniversalState
import com.sevennotes.qpets.utils.TimeUtils
import com.sevennotes.qpets.viewmodel.WindowEvent
import korlibs.image.format.ASE
import korlibs.image.format.readImageDataContainer
import korlibs.image.format.toProps
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.input.touch
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.sprite
import korlibs.math.geom.Anchor
import korlibs.math.geom.Scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class PetContext(
  var petAnimationStateMachine: PetAnimationStateMachine,
  var effectAnimationStateMachine: EffectAnimationStateMachine,
  var stateMachine: StateMachine,
  var mainScene: PetScene,
) {
  var clickHart: Boolean = false
  fun changeState(state: UniversalState) {
    stateMachine.changeState(state)
  }

  inline fun <reified T> isInState(): Boolean {
    return stateMachine.currentState() is T
  }

  fun launch(block: suspend CoroutineScope.() -> Unit) {
    mainScene.launch(block = block)
  }

}

class PetScene : Scene(), StateMachine.StateListener {

  private lateinit var petContext: PetContext
  private val longTouchTimer = Timer(0.5)
  private val clickHeartTimer = Timer(1.0)
  private var needLongTouch: Boolean = false

  override suspend fun SContainer.sceneInit() {
    val dogAse = resourcesVfs["gfx/dog.ase"].readImageDataContainer(ASE.toProps())
    val effects = resourcesVfs["gfx/effects.ase"].readImageDataContainer(ASE.toProps())
    petContext = PetContext(
      petAnimationStateMachine = PetAnimationStateMachine(dogAse),
      effectAnimationStateMachine = EffectAnimationStateMachine(effects, sceneContainer),
      stateMachine = StateMachine(),
      mainScene = this@PetScene
    )
    initStateMachine()
    EventBus.getDefault().register(this@PetScene)
  }

  private fun initStateMachine() {
    PetState.initStateMachine(petContext)
    petContext.stateMachine.addStateListener(this)
  }

  @Subscribe
  fun onPetEvent(petEvent: PetEvent) {
    if (petEvent !is PetEvent.PetEating && petContext.isInState<PetState.DieState>()) return
    when (petEvent) {
      PetEvent.PetIdle -> {
        petContext.changeState(PetStates.IDLE)
      }

      PetEvent.PetSleep -> {
        petContext.changeState(PetStates.SLEEP)
      }

      PetEvent.PetLooking -> {
        petContext.changeState(PetStates.LOOKING)
      }

      PetEvent.PetEating -> {
        if (
          petContext.isInState<PetState.IdleState>() ||
          petContext.isInState<PetState.WalkState>() ||
          petContext.isInState<PetState.DieState>()
        ) {
          petContext.changeState(PetStates.EATING)
        }
      }

      PetEvent.PetPlaying -> {
        petContext.changeState(PetStates.PLAYING)
      }

      else -> {}
    }
  }

  @Subscribe
  fun onUpdateWindow(event: IsUpdatingWindow) {
    if (event.updating) {
      needLongTouch = false
    }
  }

  override suspend fun SContainer.sceneMain() {

    petContext.petAnimationStateMachine.createSprite(PetAnimation.IDLE) { animation ->
      animation?.let {
        /**
         * 这是狗的主sprite
         */
        sprite(animation, Anchor.BOTTOM_CENTER) {
          x = 50f
          y = 100f
          scale = Scale(2.7f, 2.7f)

          onClick {

            if (petContext.isInState<PetState.DieState>()) return@onClick

            petContext.effectAnimationStateMachine.showEffect(Effects.HEART)
            petContext.changeState(PetStates.PLAYING)
            clickHeartTimer.reset()
            petContext.clickHart = true
          }
        }

      }
    }

    touch {
      this.start.add {
        needLongTouch = true
        longTouchTimer.reset()
      }
      this.end.add {
        needLongTouch = false
      }
    }

    initState()

    addUpdater {

      if (needLongTouch) {
        if (longTouchTimer.stick(it.seconds)) {
          needLongTouch = false
          EventBus.getDefault().post(WindowEvent.MenuShow)
        }
      }

      if (petContext.clickHart) {
        if (clickHeartTimer.stick(it.seconds)) {
          petContext.clickHart = false
        }
      }

      petContext.stateMachine.update(it)
    }

  }

  private fun initState() {
    if (TimeUtils.isNight()) {
      petContext.changeState(PetStates.SLEEP)
    } else {
      petContext.changeState(PetStates.IDLE)
    }
  }

  override suspend fun sceneDestroy() {
    super.sceneDestroy()
    EventBus.getDefault().unregister(this@PetScene)
  }

  override fun beforeStateExit(state: StateImpl) {
    if (state is PetState.EatingState) {
      Log.d("test", "before eating state exit")
    }
  }

}

