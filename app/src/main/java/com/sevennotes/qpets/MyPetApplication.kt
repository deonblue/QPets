package com.sevennotes.qpets

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class MyPetApplication : Application(), ViewModelStoreOwner {

  private lateinit var myViewModelStore: ViewModelStore

  override fun onCreate() {
    super.onCreate()
    instance = this
    myViewModelStore = ViewModelStore()
  }

  override val viewModelStore: ViewModelStore
    get() = myViewModelStore

  companion object {
    private lateinit var instance: MyPetApplication
    fun getInstance() = instance
  }

}