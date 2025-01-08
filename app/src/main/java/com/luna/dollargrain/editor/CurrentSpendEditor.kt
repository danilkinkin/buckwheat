package com.luna.dollargrain.editor

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.util.fixedNumberString
import com.luna.dollargrain.util.join
import com.luna.dollargrain.util.observeLiveData
import com.luna.dollargrain.util.tryConvertStringToNumber
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

class FocusController {
    var onFocus: MutableState<(() -> Unit)?> = mutableStateOf(null)
    var onBlur: MutableState<(() -> Unit)?> = mutableStateOf(null)

    fun focus() {
        onFocus.value?.let { it() }
    }

    fun blur() {
        onBlur.value?.let { it() }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CurrentSpendEditor(
    modifier: Modifier = Modifier,
    focusController: FocusController = remember { FocusController() },
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current

    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val mode by editorViewModel.mode.observeAsState(EditMode.ADD)

    var spentValue by remember { mutableStateOf("0") }
    var stage by remember { mutableStateOf(AnimState.IDLE) }
    var currState by remember { mutableStateOf<AnimState?>(null) }
    var requestFocus by remember { mutableStateOf(false) }
    var hide by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun calculateValues() {
        spentValue = editorViewModel.rawSpentValue.value!!
        requestFocus = true
    }

    observeLiveData(appViewModel.sheetStates) {
        if (it.isEmpty()) {
            requestFocus = true
            hide = false
        } else {
            hide = editorViewModel.rawSpentValue.value!! == ""
        }
    }

    observeLiveData(spendsViewModel.dailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.spentFromDailyBudget) {
        calculateValues()
    }

    observeLiveData(editorViewModel.stage) {
        when (it) {
            EditStage.IDLE -> {
                if (currState === AnimState.EDITING) {
                    stage = AnimState.RESET
                }
                calculateValues()
            }
            EditStage.CREATING_SPENT -> {
                calculateValues()

                stage = AnimState.EDITING
            }
            EditStage.EDIT_SPENT -> {
                calculateValues()

                stage = AnimState.EDITING
            }
            EditStage.COMMITTING_SPENT -> {
                stage = AnimState.COMMIT
            }
        }

        currState = stage
    }

    LaunchedEffect(focusController) {
        focusController.onFocus.value = {
            if (currState != AnimState.EDITING) {
                requestFocus = true
                calculateValues()

                editorViewModel.startCreatingSpent()
                editorViewModel.modifyEditingSpent(BigDecimal.ZERO)
            }
        }
        focusController.onBlur.value = {
            focusManager.clearFocus()
        }
    }

    BoxWithConstraints(modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (!hide) {
                EditableTextWithLabel(
                    value = spentValue,
                    onChangeValue = {
                        val fixed = fixedNumberString(it)
                        val converted = tryConvertStringToNumber(fixed)

                        editorViewModel.rawSpentValue.value = fixed
                        editorViewModel.modifyEditingSpent(converted.join().toBigDecimal())

                        if (fixed === "") {
                            if (mode === EditMode.ADD) runBlocking {
                                editorViewModel.resetEditingSpent()
                            }
                        }
                    },
                    currency = currency,
                    focusRequester = focusRequester,
                    showSystemKeyboard = false
                )

                DisposableEffect(requestFocus) {
                    focusRequester.requestFocus()
                    requestFocus = false

                    onDispose {}
                }
            }
        }
    }
}
