package com.danilkinkin.buckwheat.keyboard

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.getFloatDivider
import com.danilkinkin.buckwheat.util.join
import com.danilkinkin.buckwheat.util.tryConvertStringToNumber
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

val BUTTON_GAP = 6.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Keyboard(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val mode by spendsViewModel.mode.observeAsState(SpendsViewModel.Mode.ADD)
    val currentRawSpent by spendsViewModel.rawSpentValue.observeAsState("")
    var debugProgress by remember { mutableStateOf(0) }
    val dispatch = rememberAppKeyboardDispatcher { action, value ->
        var isMutate = true
        var newValue = spendsViewModel.rawSpentValue.value ?: ""

        when (action) {
            SpendsViewModel.Action.PUT_NUMBER -> {
                newValue += value
            }
            SpendsViewModel.Action.SET_DOT -> {
                newValue += "."
            }
            SpendsViewModel.Action.REMOVE_LAST -> {
                newValue = newValue.dropLast(1)
                Log.d("mode", mode.toString())
                Log.d("newValue", "'${newValue}'")

                if (newValue == "") {
                    if (mode === SpendsViewModel.Mode.ADD) runBlocking {
                        spendsViewModel.resetSpent()

                        isMutate = false
                    }
                }
            }
        }

        if (isMutate) runBlocking {
            spendsViewModel.rawSpentValue.value = tryConvertStringToNumber(newValue).join(third = false)

            if (spendsViewModel.stage.value === SpendsViewModel.Stage.IDLE) spendsViewModel.createSpent()
            spendsViewModel.editSpent(spendsViewModel.rawSpentValue.value!!.toBigDecimal())
        } else if (newValue == "") {
            spendsViewModel.rawSpentValue.value = newValue
        }
    }

    Column (
        modifier
            .fillMaxSize()
            .padding(14.dp)) {
        Row (
            Modifier
                .fillMaxSize()
                .weight(1F)) {
            for (i in 7..9) {
                KeyboardButton(
                    modifier = Modifier
                        .weight(1F)
                        .padding(BUTTON_GAP),
                    type = KeyboardButtonType.DEFAULT,
                    text = i.toString(),
                    onClick = {
                        dispatch(SpendsViewModel.Action.PUT_NUMBER, i)
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
                    dispatch(SpendsViewModel.Action.REMOVE_LAST, null)
                    debugProgress = 0
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onLongClick = {
                    debugProgress = 0
                    if (mode === SpendsViewModel.Mode.ADD) {
                        spendsViewModel.resetSpent()
                    } else {
                        spendsViewModel.rawSpentValue.value = tryConvertStringToNumber("0").join(third = false)

                        if (spendsViewModel.stage.value === SpendsViewModel.Stage.IDLE) spendsViewModel.createSpent()
                        spendsViewModel.editSpent(spendsViewModel.rawSpentValue.value!!.toBigDecimal())
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
        }
        Row (
            Modifier
                .fillMaxSize()
                .weight(3F)) {
            Column (
                Modifier
                    .fillMaxSize()
                    .weight(3F)) {
                Row (
                    Modifier
                        .fillMaxSize()
                        .weight(1F)) {
                    for (i in 4..6) {
                        KeyboardButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(BUTTON_GAP),
                            type = KeyboardButtonType.DEFAULT,
                            text = i.toString(),
                            onClick = {
                                dispatch(SpendsViewModel.Action.PUT_NUMBER, i)
                                debugProgress = 0
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }
                Row (
                    Modifier
                        .fillMaxSize()
                        .weight(1F)) {
                    for (i in 1..3) {
                        KeyboardButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(BUTTON_GAP),
                            type = KeyboardButtonType.DEFAULT,
                            text = i.toString(),
                            onClick = {
                                dispatch(SpendsViewModel.Action.PUT_NUMBER, i)
                                debugProgress = 0
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }
                Row (
                    Modifier
                        .fillMaxSize()
                        .weight(1F)) {
                    KeyboardButton(
                        modifier = Modifier
                            .weight(2F)
                            .padding(BUTTON_GAP),
                        type = KeyboardButtonType.DEFAULT,
                        text = "0",
                        onClick = {
                            dispatch(SpendsViewModel.Action.PUT_NUMBER, 0)
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
                            dispatch(SpendsViewModel.Action.SET_DOT, null)
                            debugProgress = if (debugProgress == 8) -1 else 0
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )
                }
            }
            Column (
                Modifier
                    .fillMaxSize()
                    .weight(1F)
            ) {
                val fixedSpent = tryConvertStringToNumber(currentRawSpent).join(third = false)

                AnimatedContent(
                    targetState = (fixedSpent == "0" || fixedSpent == "0." || fixedSpent == "0.0") && mode === SpendsViewModel.Mode.EDIT,
                    transitionSpec = {
                        if (targetState && !initialState) {
                            fadeIn(
                                tween(durationMillis = 250)
                            ) with fadeOut(
                                tween(durationMillis = 250)
                            )
                        } else {
                            fadeIn(
                                tween(durationMillis = 250)
                            ) with fadeOut(
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
                                spendsViewModel.editedSpent?.let { spendsViewModel.removeSpent(it) }
                                spendsViewModel.resetSpent()
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
                                    spendsViewModel.resetSpent()

                                    appViewModel.setIsDebug(!appViewModel.isDebug.value!!)

                                    coroutineScope.launch {
                                        appViewModel.snackbarHostState.showSnackbar(
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
                                    spendsViewModel.commitSpent()
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
    BuckwheatTheme {
        Keyboard()
    }
}