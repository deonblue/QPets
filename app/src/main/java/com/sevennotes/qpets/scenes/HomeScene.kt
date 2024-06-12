package com.sevennotes.qpets.scenes

import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.image
import korlibs.math.geom.Scale
import kotlin.properties.Delegates


class HomeScene : Scene() {

  private val PET_START_PLACE_X = 700f
  private val PET_START_PLACE_Y = 1530f

  private lateinit var backgroundImg: Bitmap
  private var widthScale by Delegates.notNull<Float>()
  private var heightScale by Delegates.notNull<Float>()

  //宠物初始位置X和Y坐标
  val petHomeX: Float
    get() = PET_START_PLACE_X * widthScale
  val petHomeY: Float
    get() = PET_START_PLACE_Y * heightScale

  override suspend fun SContainer.sceneInit() {
    backgroundImg = resourcesVfs["gfx/background.png"].readBitmap()
  }

  override suspend fun SContainer.sceneMain() {

    val image = image(texture = backgroundImg)
    widthScale = windowBounds.width / image.size.width
    heightScale = windowBounds.height / image.size.height
    image.scale = Scale(widthScale, heightScale)

  }

}