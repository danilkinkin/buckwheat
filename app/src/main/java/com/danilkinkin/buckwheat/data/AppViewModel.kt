package com.danilkinkin.buckwheat.data

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.base.balloon.BalloonController
import com.danilkinkin.buckwheat.di.SettingsRepository
import com.danilkinkin.buckwheat.di.TUTORS
import com.danilkinkin.buckwheat.effects.ConfettiController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SystemBarState (
    val statusBarColor: Color,
    val statusBarDarkIcons: Boolean,
    val navigationBarDarkIcons: Boolean,
    val navigationBarColor: Color,
)

data class PathState (
    val name: String,
    val args: Map<String, Any?> = emptyMap(),
    val callback: (result: Map<String, Any?>) -> Unit = {},
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    var _snackbarHostState = SnackbarHostState()
        private set
    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration =
            if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
        snackbarResult: (SnackbarResult) -> Unit = {},
    ) {
        viewModelScope.launch {
            _snackbarHostState.currentSnackbarData?.dismiss()

            val result = _snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration,
            )

            snackbarResult(result)
        }
    }

    var confettiController = ConfettiController()
        private set

    var balloonController = BalloonController()
        private set

    var lockSwipeable: MutableState<Boolean> = mutableStateOf(false)

    var showSystemKeyboard: MutableState<Boolean> = mutableStateOf(false)

    var statusBarStack: MutableList<() -> SystemBarState> = emptyList<() -> SystemBarState>().toMutableList()

    var sheetStates: MutableLiveData<Map<String, PathState>> = MutableLiveData(emptyMap())

    var isDebug = settingsRepository.isDebug().asLiveData()

    var showSpentCardByDefault = settingsRepository.isShowSpentCardByDefault().asLiveData()

    fun getTutorialStage(name: TUTORS) = settingsRepository.getTutorialStage(name).asLiveData()

    fun setShowSpentCardByDefault(showByDefault: Boolean) {
        viewModelScope.launch {
            settingsRepository.switchShowSpentCardByDefault(showByDefault)
        }
    }

    fun setIsDebug(debug: Boolean) {
        viewModelScope.launch {
            settingsRepository.switchDebug(debug)
        }
    }

    fun openSheet(state: PathState) {
        sheetStates.value = sheetStates.value!!.plus(Pair(state.name, state))
    }

    fun closeSheet(name: String) {
        sheetStates.value = sheetStates.value!!.minus(name)
    }

    fun passTutorial(name: TUTORS) {
        viewModelScope.launch {
            settingsRepository.passTutorial(name)
        }
    }

    fun activateTutorial(name: TUTORS) {
        viewModelScope.launch {
            settingsRepository.activateTutorial(name)
        }
    }
}