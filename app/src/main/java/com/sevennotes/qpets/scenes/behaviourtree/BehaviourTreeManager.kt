package com.sevennotes.qpets.scenes.behaviourtree

abstract class BehaviourTreeManager<T> {

  private var root: Node<T>? = null
  abstract fun createRoot(): Node<T>

  fun create() {
    root = createRoot()
  }

  fun update(context: T) {
    root?.tick(context)
  }
}