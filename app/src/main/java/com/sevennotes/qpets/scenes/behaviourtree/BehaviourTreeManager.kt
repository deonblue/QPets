package com.sevennotes.qpets.scenes.behaviourtree

abstract class BehaviourTreeManager<T>(private val context: T) {

  private var root: Node<T>? = null
  abstract fun createRoot(): Node<T>

  fun create() {
    root = createRoot()
  }

  fun update() {
    root?.tick(context)
  }
}