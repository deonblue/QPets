package com.sevennotes.qpets.scenes

import android.util.Log
import com.sevennotes.qpets.events.PetEvent
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.Stage
import korlibs.korge.view.position
import korlibs.math.geom.Scale
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

suspend fun Stage.mainStage() {
  MainStage(this).init()
}

class MainStage(private val stage: Stage) {

  private lateinit var pet: PetScene
  private lateinit var home: HomeScene
  private var abc = 0

  init {
    EventBus.getDefault().register(this)
  }

  suspend fun init() {
    with(stage) {
      injector.mapSingleton { HomeScene() }
      injector.mapSingleton { PetScene() }
      home = sceneContainer().changeTo<HomeScene>()
      pet = sceneContainer().changeTo<PetScene>().apply {
        sceneContainer.position(home.petHomeX, home.petHomeY)
        sceneContainer.scale = Scale(2.0)
      }
    }
  }

  @Subscribe
  fun onEvent(event: PetEvent) {
    if (event is PetEvent.PetHomeState) {
      if (event.state) {
        addPet()
      } else {
        removePet()
      }
    }
  }

  private fun addPet() {
    stage.addChild(pet.sceneContainer)
  }

  private fun removePet() {
    Log.d("test", "remove pet!!!!")
    pet.sceneContainer.removeFromParent()
  }

}