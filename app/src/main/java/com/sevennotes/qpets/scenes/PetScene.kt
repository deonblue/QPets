package com.sevennotes.qpets.scenes

import android.util.Log
import com.sevennotes.qpets.events.IsUpdatingWindow
import com.sevennotes.qpets.events.PetEvent
import com.sevennotes.qpets.scenes.animation.EffectAnimation
import com.sevennotes.qpets.scenes.animation.PetAnimation
import com.sevennotes.qpets.scenes.animation.EffectAnimationStateMachine
import com.sevennotes.qpets.scenes.animation.PetAnimationStateMachine
import com.sevennotes.qpets.scenes.common.Timer
import com.sevennotes.qpets.scenes.statemachine.PetState
import com.sevennotes.qpets.scenes.statemachine.PetStates
import com.sevennotes.qpets.scenes.statemachine.StateImpl
import com.sevennotes.qpets.scenes.statemachine.StateMachine
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
import korlibs.korge.view.Sprite
import korlibs.korge.view.addUpdater
import korlibs.korge.view.sprite
import korlibs.math.geom.Anchor
import korlibs.math.geom.Scale
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class PetScene : Scene(), StateMachine.StateListener {

  private lateinit var petAnimationStateMachine: PetAnimationStateMachine
  private lateinit var effectAnimationStateMachine: EffectAnimationStateMachine
  private val stateMachine = StateMachine()
  private var dog: Sprite? = null
  private var w: Sprite? = null
  private val longTouchTimer = Timer(0.5)
  private var needLongTouch: Boolean = false

  override suspend fun SContainer.sceneInit() {
    val dogAse = resourcesVfs["gfx/dog.ase"].readImageDataContainer(ASE.toProps())
    val effects = resourcesVfs["gfx/effects.ase"].readImageDataContainer(ASE.toProps())
    petAnimationStateMachine = PetAnimationStateMachine(dogAse)
    effectAnimationStateMachine = EffectAnimationStateMachine(effects, sceneContainer)
    initStateMachine()
    EventBus.getDefault().register(this@PetScene)
  }

  private fun initStateMachine() {
    PetState.initStateMachine(stateMachine, petAnimationStateMachine, effectAnimationStateMachine)
    stateMachine.addStateListener(this)
  }

  @Subscribe
  fun onPetEvent(petEvent: PetEvent) {
    when (petEvent) {
      PetEvent.PetIdle -> {
        stateMachine.changeState(PetStates.IDLE)
      }

      PetEvent.PetSleep -> {
        stateMachine.changeState(PetStates.SLEEP)
      }

      PetEvent.PetLooking -> {
        stateMachine.changeState(PetStates.LOOKING)
      }

      PetEvent.PetEating -> {
        if (stateMachine.currentState() is PetState.IdleState) {
          stateMachine.changeState(PetStates.EATING)
        }
      }

      PetEvent.PetPlaying -> {
        stateMachine.changeState(PetStates.PLAYING)
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

    petAnimationStateMachine.createSprite(PetAnimation.IDLE) { animation ->
      animation?.let {
        dog = sprite(animation, Anchor.BOTTOM_CENTER) {
          x = 50f
          y = 100f
          scale = Scale(2.7f, 2.7f)

          onClick {
            Log.d("test", "clicked")
          }

        }
        dog
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

    effectAnimationStateMachine.createSprite(EffectAnimation.BALL) { animation ->

      animation?.let {
        w = Sprite(animation, Anchor.CENTER)
        w
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

      stateMachine.update(it)

    }

  }

  private fun initState() {
    if (TimeUtils.isNight()) {
      stateMachine.changeState(PetStates.SLEEP)
    } else {
      stateMachine.changeState(PetStates.IDLE)
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

