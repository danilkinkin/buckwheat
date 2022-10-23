package com.danilkinkin.buckwheat.keyboard

import androidx.compose.ui.text.input.*
import androidx.lifecycle.*
import com.danilkinkin.buckwheat.data.SpendsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var state: TextFieldValue? = null
    var editCommandDispatcher: ((List<EditCommand>) -> Unit)? = null
    var manualDispatcher: ((action: SpendsViewModel.Action, value: Int?) -> Unit)? = { _, _ -> }
    var textFiledIsFocus: Boolean = false

    private val platformTextInputService = object : PlatformTextInputService {
        override fun hideSoftwareKeyboard() { }

        override fun showSoftwareKeyboard() { }

        override fun startInput(
            value: TextFieldValue,
            imeOptions: ImeOptions,
            onEditCommand: (List<EditCommand>) -> Unit,
            onImeActionPerformed: (ImeAction) -> Unit
        ) {
            state = value
            editCommandDispatcher = onEditCommand
            textFiledIsFocus = true
        }

        override fun stopInput() {
            textFiledIsFocus = false
            editCommandDispatcher = null
        }

        override fun updateState(oldValue: TextFieldValue?, newValue: TextFieldValue) {
            state = newValue
        }
    }

    fun executeAction(action: SpendsViewModel.Action, value: Int? = null) {
        if (manualDispatcher !== null) {
            manualDispatcher!!(action, value)
        }

        if (editCommandDispatcher === null) return

        if (action === SpendsViewModel.Action.PUT_NUMBER) {
            editCommandDispatcher!!(listOf(
                CommitTextCommand(value.toString(), value.toString().length)
            ))
        } else if (action === SpendsViewModel.Action.REMOVE_LAST) {
            editCommandDispatcher!!(listOf(
                BackspaceCommand()
            ))
        } else if (action === SpendsViewModel.Action.SET_DOT) {
            editCommandDispatcher!!(listOf(
                CommitTextCommand(".", 1)
            ))
        }
    }

    val keyboardService = TextInputService(platformTextInputService)
}