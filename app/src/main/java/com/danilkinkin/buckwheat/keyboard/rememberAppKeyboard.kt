package com.danilkinkin.buckwheat.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextInputService
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun rememberAppKeyboard(
    keyboardViewModel: KeyboardViewModel = hiltViewModel(),
    manualDispatcher: ((action: KeyboardAction, value: Int?) -> Unit) = { _, _ -> },
): TextInputService {
    keyboardViewModel.manualDispatcher = manualDispatcher

    return keyboardViewModel.keyboardService
}

@Composable
fun rememberAppKeyboardDispatcher(
    keyboardViewModel: KeyboardViewModel = hiltViewModel(),
    fallbackDispatcher: ((action: KeyboardAction, value: Int?) -> Unit) = { _, _ -> },
): ((action: KeyboardAction, value: Int?) -> Unit) {
    return { action, value ->
        if (keyboardViewModel.textFiledIsFocus) {
            keyboardViewModel.executeAction(action = action, value = value)
        } else {
            fallbackDispatcher(action, value)
        }
    }
}
