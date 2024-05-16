package com.sevennotes.qpets.service

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.sevennotes.qpets.MyPetApplication
import com.sevennotes.qpets.pages.PetView

class PetService() : LifecycleService(), SavedStateRegistryOwner {

  private lateinit var petView: PetView

  @RequiresApi(Build.VERSION_CODES.R)
  override fun onCreate() {
    super.onCreate()
    val viewModel = MyPetApplication.getInstance().viewModelStore
    petView = PetView(this, this, MyPetApplication.getInstance()).apply { initView() }
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    intent?.let {
      val extra = intent.getStringExtra("action")
      when(extra) {
        "start" -> { petView.show() }
        "end" -> { petView.hide() }
      }
    }
    return super.onStartCommand(intent, flags, startId)
  }

  override val savedStateRegistry: SavedStateRegistry
    get() = petView.savedStateRegistry

}

