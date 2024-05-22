package com.sevennotes.qpets.scenes.behaviourtree

import com.sevennotes.qpets.scenes.behaviourtree.BTStatus.*

enum class BTStatus {
  SUCCESS,
  FAILURE,
  RUNNING,
}

interface Node<T> {
  fun tick(context: T): BTStatus
  var parent: Node<T>?
}

abstract class BTNode<T>() : Node<T> {

  protected val children: MutableList<Node<T>> = mutableListOf()
  override var parent: Node<T>? = null

  open fun addChild(child: Node<T>) {
    child.parent = this
    children.add(child)
  }

  operator fun invoke(next: BTNode<T>.() -> Unit) {
    next()
  }

}

class DecNode<T>() : BTNode<T>() {

  var beforeChildTick: (context: T) -> Unit = {}
  var afterChildTick: (context: T) -> Unit = {}

  /**
   * DecNode only have one child
   */
  override fun addChild(child: Node<T>) {
    children.clear()
    child.parent = this
    children.add(child)
  }

  override fun tick(context: T): BTStatus {
    beforeChildTick(context)
    val res = children.getOrNull(0)?.tick(context) ?: FAILURE
    afterChildTick(context)
    return res
  }

}

//反转node
class InvertNode<T>() : BTNode<T>() {
  override fun addChild(child: Node<T>) {
    children.clear()
    child.parent = this
    children.add(child)
  }
  override fun tick(context: T): BTStatus {
    val res = children.getOrNull(0)?.tick(context) ?: FAILURE
    return when (res) {
      SUCCESS -> FAILURE
      FAILURE -> SUCCESS
      RUNNING -> RUNNING
    }
  }

}

abstract class LeafBTNode<T> : Node<T> {

  override var parent: Node<T>? = null
  abstract fun update(context: T): BTStatus

  override fun tick(context: T): BTStatus {
    return update(context)
  }
}

class SequenceNode<T>() : BTNode<T>() {
  override fun tick(context: T): BTStatus {
    for (child in children) {
      val status = child.tick(context)
      if (status != SUCCESS) {
        return status
      }
    }
    return SUCCESS
  }

}

class SelectorNode<T>() : BTNode<T>() {
  override fun tick(context: T): BTStatus {
    for (child in children) {
      val status = child.tick(context)
      if (status != FAILURE) {
        return status
      }
    }
    return FAILURE
  }

}

class ConditionNode<T>(private val condition: (time: T) -> Boolean) : LeafBTNode<T>() {
  override fun update(context: T): BTStatus {
    return if (condition(context)) SUCCESS else FAILURE
  }

}

class ActionNode<T>(private val action: (context: T) -> BTStatus) : LeafBTNode<T>() {
  override fun update(context: T): BTStatus {
    return action(context)
  }

}

inline fun <T> BTNode<T>.sequence(next: BTNode<T>.() -> Unit) {
  val sequenceNode = SequenceNode<T>()
  sequenceNode.next()
  addChild(sequenceNode)
}

inline fun <T> BTNode<T>.selector(next: BTNode<T>.() -> Unit = {}) {
  val selectorNode = SelectorNode<T>()
  selectorNode.next()
  addChild(selectorNode)
}

inline fun <T> BTNode<T>.inverter(next: BTNode<T>.() -> Unit = {}) {
  val invertNode = InvertNode<T>()
  invertNode.next()
  addChild(invertNode)
}

fun <T> BTNode<T>.decorator(
  beforeTick: (T) -> Unit = {},
  afterTick: (T) -> Unit = {},
  next: BTNode<T>.() -> Unit = {}
) {
  val decNode = DecNode<T>()
  decNode.beforeChildTick = beforeTick
  decNode.afterChildTick = afterTick
  addChild(decNode)
}

fun <T> BTNode<T>.condition(condition: (context: T) -> Boolean) {
  addChild(ConditionNode(condition))
}

fun <T> BTNode<T>.action(action: (context: T) -> BTStatus) {
  addChild(ActionNode(action))
}

fun <T> BTNode<T>.conditionAction(
  condition: (context: T) -> Boolean,
  action: (context: T) -> BTStatus,
) {
  sequence {
    condition(condition)
    action(action)
  }
}

fun <T> BTNode<T>.invertConditionAction(
  condition: (context: T) -> Boolean,
  action: (context: T) -> BTStatus,
) {
  sequence {
    inverter {
      condition(condition)
    }
    action(action)
  }
}










