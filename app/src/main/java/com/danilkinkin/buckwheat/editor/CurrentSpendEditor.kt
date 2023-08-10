package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.EditMode
import com.danilkinkin.buckwheat.data.EditStage
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.runBlocking

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

@Composable
fun CurrentSpendEditor(
    modifier: Modifier = Modifier,
    focusController: FocusController = remember { FocusController() },
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current

    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val mode by spendsViewModel.mode.observeAsState(EditMode.ADD)

    var spentValue by remember { mutableStateOf("0") }
    var stage by remember { mutableStateOf(AnimState.IDLE) }
    var currState by remember { mutableStateOf<AnimState?>(null) }
    var requestFocus by remember { mutableStateOf(false) }
    var hide by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun calculateValues() {
        spentValue = spendsViewModel.rawSpentValue.value!!
        requestFocus = true
    }

    observeLiveData(appViewModel.sheetStates) {
        if (it.isEmpty()) {
            requestFocus = true
            hide = false
        } else {
            hide = spendsViewModel.rawSpentValue.value!! == ""
        }
    }

    observeLiveData(spendsViewModel.dailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.spentFromDailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.stage) {
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

                spendsViewModel.startCreatingSpent()
                spendsViewModel.modifyEditingSpent(0.toBigDecimal())
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

                        spendsViewModel.rawSpentValue.value = fixed
                        spendsViewModel.modifyEditingSpent(converted.join().toBigDecimal())

                        if (fixed === "") {
                            if (mode === EditMode.ADD) runBlocking {
                                spendsViewModel.resetEditingSpent()
                            }
                        }
                    },
                    currency = currency,
                    focusRequester = focusRequester,
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
