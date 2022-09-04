package com.danilkinkin.buckwheat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


enum class ThemeMode { LIGHT, NIGHT, SYSTEM }

class ThemeViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val forceDarkModeKey = stringPreferencesKey("theme")

    val state = MutableLiveData(ThemeMode.SYSTEM)
    fun request() {
        viewModelScope.launch {
            dataStore.data.collectLatest {
                state.value = ThemeMode.valueOf(it[forceDarkModeKey] ?: ThemeMode.SYSTEM.toString())
            }
        }
    }

    fun changeThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit {
                it[forceDarkModeKey] = mode.toString()
            }
        }
    }
}