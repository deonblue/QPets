package com.sevennotes.qpets.scenes.statemachine

import com.sevennotes.qpets.scenes.behaviourtree.BehaviourTreeManager
import com.sevennotes.qpets.scenes.common.Timer
import korlibs.time.TimeSpan
import java.lang.Thread.State

class StateMachine {

  private var currentState: StateImpl? = null
  private val states: MutableMap<String, StateImpl> = mutableMapOf()
  private val listeners: MutableList<StateListener> = mutableListOf()
  private var behaviourTreeManager: BehaviourTreeManager<StateMachine>? = null
  private val btTimer: Timer = Timer(1.0)

  fun setBehaviourTreeManager(btm: BehaviourTreeManager<StateMachine>) {
    behaviourTreeManager = btm
  }

  fun addState(stateName: UniversalState, state: StateImpl) {
    state.stateMachine = this
    states[stateName.name] = state
  }

  fun update(time: TimeSpan) {
    if (btTimer.stick(time.seconds)) {
      behaviourTreeManager?.update(this)
    }
    currentState?.update(time)
  }

  fun currentState(): StateImpl? {
    return currentState
  }

  fun changeState(state: UniversalState) {
    changeState(state.name)
  }

  private fun changeState(state: String) {
    val toState = states[state]
    //只有要切换的state和当前state不一致才会去切换
    if (currentState != toState) {
      changeState(states[state])
    }
  }

  fun addStateListener(listener: StateListener) {
    listeners.add(listener)
  }

  private fun changeState(nextState: StateImpl?) {
    currentState?.let { state ->
      listeners.forEach { it.beforeStateExit(state) }
      state.onExit()
    }
    currentState = nextState

    currentState?.let {state ->
      listeners.forEach { it.beforeStateEnter(state) }
      state.onEnter()
    }
  }

  interface StateListener {
    fun beforeStateEnter(state: StateImpl) {}
    fun beforeStateExit(state: StateImpl) {}
  }

}
