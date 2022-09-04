package com.danilkinkin.buckwheat.data

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.danilkinkin.buckwheat.data.entities.Storage
import com.danilkinkin.buckwheat.di.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SystemBarState (
    val statusBarColor: Color,
    val statusBarDarkIcons: Boolean,
    val navigationBarDarkIcons: Boolean,
    val navigationBarColor: Color,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val db: DatabaseRepository,
) : ViewModel() {
    private val storage = db.storageDao()

    var statusBarStack: MutableList<SystemBarState> = emptyList<SystemBarState>().toMutableList()

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