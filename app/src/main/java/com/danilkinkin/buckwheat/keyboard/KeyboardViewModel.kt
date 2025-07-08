package com.danilkinkin.buckwheat.keyboard

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.text.input.EditCommand
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import javax.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var state: TextFieldValue? = null
    var editCommandDispatcher: ((List<EditCommand>) -> Unit)? = null
    var manualDispatcher: ((action: KeyboardAction, value: Int?) -> Unit)? = { _, _ -> }
    var textFiledIsFocus: Boolean = false

    var editorInfo: EditorInfo? = null
    var inputConnection: InputConnection? = null

    @OptIn(ExperimentalComposeUiApi::class)
    private val platformTextInputService = PlatformTextInputInterceptor { request, nextHandler ->
        EditorInfo().also {
            inputConnection = request.createInputConnection(it)
            editorInfo = it
        }
        try {
            awaitCancellation()
        } finally {
            inputConnection = null
            editorInfo = null
        }
    }

    fun executeAction(action: KeyboardAction, value: Int? = null) {
        if (manualDispatcher !== null) {
            manualDispatcher!!(action, value)
        }

        if (editCommandDispatcher === null) return

        if (action === KeyboardAction.PUT_NUMBER) {
            inputConnection?.commitText(value.toString(), value.toString().length)
        } else if (action === KeyboardAction.REMOVE_LAST) {
            inputConnection?.deleteSurroundingText(1, 0)
        } else if (action === KeyboardAction.SET_DOT) {
            inputConnection?.commitText(".", 1)
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    val keyboardService = platformTextInputService
}