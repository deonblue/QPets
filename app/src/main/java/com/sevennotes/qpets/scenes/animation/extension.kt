package com.sevennotes.qpets.scenes.animation

import korlibs.datastructure.toFastList
import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.ImageData
import korlibs.korge.view.SpriteAnimation

fun ImageData.getAnimation(name: String): SpriteAnimation {
  val slices = animationsByName[name]?.frames?.map { it.slice }?.toFastList()
  return SpriteAnimation(slices ?: emptyList())
}

fun ImageData.getAnimation(name: String, getSlices: (List<BmpSlice>) -> List<BmpSlice>): SpriteAnimation {
  val slices = animationsByName[name]?.frames?.map { it.slice }?.toFastList()
  val customSlices = getSlices(slices ?: emptyList())
  return SpriteAnimation(customSlices)
}

