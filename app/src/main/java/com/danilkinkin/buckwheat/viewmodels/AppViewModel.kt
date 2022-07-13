package com.danilkinkin.buckwheat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.danilkinkin.buckwheat.di.DatabaseModule
import com.danilkinkin.buckwheat.entities.Storage

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseModule.getInstance(application)

    private val storage = db.storageDao()

    var isDebug: MutableLiveData<Boolean> = MutableLiveData(try {
        storage.get("isDebug").value.toBoolean()
    } catch (e: Exception) {
        false
    })

    fun setIsDebug(debug: Boolean) {
        storage.set(Storage("isDebug", debug.toString()))

        isDebug.value = debug
    }
}