package com.sevennotes.qpets.service

import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.sevennotes.qpets.MyPetApplication
import com.sevennotes.qpets.events.PetEvent
import com.sevennotes.qpets.global.PetGlobalData
import com.sevennotes.qpets.pages.PetView
import org.greenrobot.eventbus.EventBus

class PetService : LifecycleService(), SavedStateRegistryOwner {

  private lateinit var petView: PetView

  @RequiresApi(Build.VERSION_CODES.R)
  override fun onCreate() {
    super.onCreate()
    // 初始化全局数据
    val sp = getSharedPreferences("pet", MODE_PRIVATE)
    val gd = sp.getString("globalData", "")
    if (!TextUtils.isEmpty(gd)) {
      Log.d("test", "load globalData: $gd")
      PetGlobalData.fromJson(gd!!)
    }
    petView = PetView(this, this, MyPetApplication.getInstance()).apply { initView() }
  }

  override fun onDestroy() {
    super.onDestroy()
    val gd = PetGlobalData.toJson()
    Log.d("test", "save globalData: $gd")
    // 保存全局数据
    val sp = getSharedPreferences("pet", MODE_PRIVATE)
    sp.edit {
      putString("globalData", gd)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    intent?.let {
      val extra = intent.getStringExtra("action")
      when(extra) {
        "start" -> {
          if (PetGlobalData.getInstance().outSide) return@let
          PetGlobalData.getInstance().outSide = true
          petView.show()
          //宠物不在家事件
          EventBus.getDefault().post(PetEvent.PetHomeState(false))
        }
        "end" -> {
          if (!PetGlobalData.getInstance().outSide) return@let
          PetGlobalData.getInstance().outSide = false
          petView.hide()
          //宠物回家事件
          EventBus.getDefault().post(PetEvent.PetHomeState(true))
        }
      }
    }
    return super.onStartCommand(intent, flags, startId)
  }

  override val savedStateRegistry: SavedStateRegistry
    get() = petView.savedStateRegistry

}

