package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.*
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
        calculateValues()
    }

    LaunchedEffect(focusController) {
        focusController.onFocus.value = {
            val stage = editorViewModel.stage.value

            // Tapping the editor while an amount is already being entered must not reset it.
            if (stage !== EditStage.EDIT_SPENT && stage !== EditStage.CREATING_SPENT) {
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

    Box(modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            // Top-anchored: the editor's bottom edge moves when the tag row grows or the IME
            // replaces the keypad, so a centred amount drifted. A fixed strip under the toolbar
            // keeps the number still while the rest of the layout settles.
            contentAlignment = Alignment.TopEnd,
        ) {
            if (!hide) {
                EditableTextWithLabel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    value = spentValue,
                    onChangeValue = {
                        val fixed = fixedNumberString(it)
                        val converted = tryConvertStringToNumber(fixed)

                        editorViewModel.rawSpentValue.value = fixed
                        editorViewModel.modifyEditingSpent(converted.join().toBigDecimal())

                        if (fixed == "") {
                            if (mode === EditMode.ADD) {
                                editorViewModel.resetEditingSpent()
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
