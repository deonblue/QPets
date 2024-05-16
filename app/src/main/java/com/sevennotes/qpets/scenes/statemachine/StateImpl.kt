package com.sevennotes.qpets.scenes.statemachine

import com.sevennotes.qpets.scenes.interfaces.State
import korlibs.time.TimeSpan


interface UniversalState {
  var name: String
}

open class StateImpl : State {

  var stateMachine: StateMachine? = null

}
