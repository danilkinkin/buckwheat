package com.luna.dollargrain.keyboard

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.data.entities.TransactionType
import com.luna.dollargrain.di.TUTORS
import com.luna.dollargrain.editor.EditMode
import com.luna.dollargrain.editor.EditStage
import com.luna.dollargrain.editor.EditorViewModel
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.getFloatDivider
import com.luna.dollargrain.util.join
import com.luna.dollargrain.util.tryConvertStringToNumber
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

val BUTTON_GAP = 6.dp

enum class KeyboardAction { PUT_NUMBER, SET_DOT, REMOVE_LAST }

@Composable
fun Keyboard(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mode by editorViewModel.mode.observeAsState(EditMode.ADD)
    val currentRawSpent by editorViewModel.rawSpentValue.observeAsState("")
    var debugProgress by remember { mutableStateOf(0) }
    val dispatch = rememberAppKeyboardDispatcher { action, value ->
        var isMutate = true
        var newValue = editorViewModel.rawSpentValue.value ?: ""

        when (action) {
            KeyboardAction.PUT_NUMBER -> {
                newValue += value
            }

            KeyboardAction.SET_DOT -> {
                newValue += "."
            }

            KeyboardAction.REMOVE_LAST -> {
                newValue = newValue.dropLast(1)
                Log.d("mode", mode.toString())
                Log.d("newValue", "'${newValue}'")

                if (newValue == "") {
                    if (mode === EditMode.ADD) runBlocking {
                        editorViewModel.resetEditingSpent()

                        isMutate = false
                    }
                }
            }
        }

        if (isMutate) runBlocking {
            editorViewModel.rawSpentValue.value =
                tryConvertStringToNumber(newValue).join(third = false)

            if (editorViewModel.stage.value === EditStage.IDLE) editorViewModel.startCreatingSpent()
            editorViewModel.modifyEditingSpent(editorViewModel.rawSpentValue.value!!.toBigDecimal())
        } else if (newValue == "") {
            editorViewModel.rawSpentValue.value = newValue
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .weight(1F)
        ) {
            for (i in 7..9) {
                KeyboardButton(
                    modifier = Modifier
                        .weight(1F)
                        .padding(BUTTON_GAP),
                    type = KeyboardButtonType.DEFAULT,
                    text = i.toString(),
                    onClick = {
                        dispatch(KeyboardAction.PUT_NUMBER, i)
                        debugProgress = 0
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )
            }
            KeyboardButton(
                modifier = Modifier
                    .weight(1F)
                    .padding(BUTTON_GAP),
                type = KeyboardButtonType.SECONDARY,
                icon = painterResource(R.drawable.ic_backspace),
                onClick = {
                    dispatch(KeyboardAction.REMOVE_LAST, null)
                    debugProgress = 0
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onLongClick = {
                    debugProgress = 0
                    if (mode === EditMode.ADD) {
                        editorViewModel.resetEditingSpent()
                    } else {
                        editorViewModel.rawSpentValue.value =
                            tryConvertStringToNumber("0").join(third = false)

                        if (editorViewModel.stage.value === EditStage.IDLE) editorViewModel.startCreatingSpent()
                        editorViewModel.modifyEditingSpent(editorViewModel.rawSpentValue.value!!.toBigDecimal())
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
        }
        Row(
            Modifier
                .fillMaxSize()
                .weight(3F)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .weight(3F)
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .weight(1F)
                ) {
                    for (i in 4..6) {
                        KeyboardButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(BUTTON_GAP),
                            type = KeyboardButtonType.DEFAULT,
                            text = i.toString(),
                            onClick = {
                                dispatch(KeyboardAction.PUT_NUMBER, i)
                                debugProgress = 0
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxSize()
                        .weight(1F)
                ) {
                    for (i in 1..3) {
                        KeyboardButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(BUTTON_GAP),
                            type = KeyboardButtonType.DEFAULT,
                            text = i.toString(),
                            onClick = {
                                dispatch(KeyboardAction.PUT_NUMBER, i)
                                debugProgress = 0
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxSize()
                        .weight(1F)
                ) {
                    KeyboardButton(
                        modifier = Modifier
                            .weight(2F)
                            .padding(BUTTON_GAP),
                        type = KeyboardButtonType.DEFAULT,
                        text = "0",
                        onClick = {
                            dispatch(KeyboardAction.PUT_NUMBER, 0)
                            debugProgress += 1
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    )
                    KeyboardButton(
                        modifier = Modifier
                            .weight(1F)
                            .padding(BUTTON_GAP),
                        type = KeyboardButtonType.DEFAULT,
                        text = getFloatDivider(),
                        onClick = {
                            dispatch(KeyboardAction.SET_DOT, null)
                            debugProgress = if (debugProgress == 8) -1 else 0
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .weight(1F)
            ) {
                val fixedSpent = tryConvertStringToNumber(currentRawSpent).join(third = false)

                AnimatedContent(
                    label = "Delete or Apply",
                    targetState = (fixedSpent == "0" || fixedSpent == "0." || fixedSpent == "0.0") && mode === EditMode.EDIT,
                    transitionSpec = {
                        if (targetState && !initialState) {
                            fadeIn(
                                tween(durationMillis = 250)
                            ) togetherWith fadeOut(
                                tween(durationMillis = 250)
                            )
                        } else {
                            fadeIn(
                                tween(durationMillis = 250)
                            ) togetherWith fadeOut(
                                tween(durationMillis = 250)
                            )
                        }.using(
                            SizeTransform(clip = false)
                        )
                    }
                ) { targetIsDelete ->
                    if (targetIsDelete) {
                        KeyboardButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(BUTTON_GAP),
                            type = KeyboardButtonType.DELETE,
                            icon = painterResource(R.drawable.ic_delete_forever),
                            onClick = {
                                editorViewModel.editedTransaction?.let {
                                    spendsViewModel.removeSpent(
                                        it
                                    )
                                }
                                editorViewModel.resetEditingSpent()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    } else {
                        KeyboardButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(BUTTON_GAP),
                            type = KeyboardButtonType.PRIMARY,
                            icon = painterResource(R.drawable.ic_apply),
                            onClick = {
                                if (debugProgress == -1) {
                                    editorViewModel.resetEditingSpent()

                                    appViewModel.setIsDebug(!appViewModel.isDebug.value!!)

                                    coroutineScope.launch {
                                        appViewModel.showSnackbar(
                                            "Debug ${
                                                if (appViewModel.isDebug.value!!) {
                                                    "ON"
                                                } else {
                                                    "OFF"
                                                }
                                            }"
                                        )
                                    }

                                    return@KeyboardButton
                                }

                                debugProgress = 0

                                runBlocking {
                                    if (editorViewModel.canCommitEditingSpent()) {
                                        if (mode == EditMode.EDIT) {
                                            val newVersionOfSpent =
                                                editorViewModel.editedTransaction!!.copy(
                                                    value = editorViewModel.currentSpent,
                                                    date = editorViewModel.currentDate,
                                                    comment = (editorViewModel.currentComment.value
                                                        ?: "").trim()
                                                )

                                            spendsViewModel.removeSpent(
                                                editorViewModel.editedTransaction!!,
                                                silent = true
                                            )
                                            spendsViewModel.addSpent(newVersionOfSpent)
                                        } else {
                                            spendsViewModel.addSpent(
                                                Transaction(
                                                    type = TransactionType.SPENT,
                                                    value = editorViewModel.currentSpent,
                                                    date = Date(),
                                                    comment = (editorViewModel.currentComment.value
                                                        ?: "").trim()
                                                )
                                            )
                                            appViewModel.activateTutorial(TUTORS.OPEN_HISTORY)
                                        }

                                        editorViewModel.resetEditingSpent()
                                    }
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun KeyboardPreview() {
    DollargrainTheme {
        Keyboard()
    }
}
