package com.danilkinkin.buckwheat.keyboard

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.join
import com.danilkinkin.buckwheat.util.tryConvertStringToNumber
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val BUTTON_GAP = 6.dp

@Composable
fun Keyboard(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
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


                if (newValue == "") {
                    runBlocking {
                        spendsViewModel.resetSpent()

                        isMutate = false
                    }
                }
            }
        }

        if (isMutate) {
            runBlocking {
                spendsViewModel.rawSpentValue.value = tryConvertStringToNumber(newValue).join(third = false)


                Log.d("Editor", "rawSpentValue = ${spendsViewModel.rawSpentValue}")

                if (spendsViewModel.stage.value === SpendsViewModel.Stage.IDLE) spendsViewModel.createSpent()
                spendsViewModel.editSpent(spendsViewModel.rawSpentValue.value!!.toBigDecimal())
            }
        }
    }

    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row (modifier = Modifier
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
                },
                onLongClick = {
                    spendsViewModel.resetSpent()
                },
            )
        }
        Row (modifier = Modifier
            .fillMaxSize()
            .weight(3F)) {
            Column (modifier = Modifier
                .fillMaxSize()
                .weight(3F)) {
                Row (modifier = Modifier
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
                            }
                        )
                    }
                }
                Row (modifier = Modifier
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
                            }
                        )
                    }
                }
                Row (modifier = Modifier
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
                        }
                    )
                    KeyboardButton(
                        modifier = Modifier
                            .weight(1F)
                            .padding(BUTTON_GAP),
                        type = KeyboardButtonType.DEFAULT,
                        text = ".",
                        onClick = {
                            dispatch(SpendsViewModel.Action.SET_DOT, null)
                        }
                    )
                }
            }
            Column (modifier = Modifier
                .fillMaxSize()
                .weight(1F)) {
                KeyboardButton(
                    modifier = Modifier
                        .weight(1F)
                        .padding(BUTTON_GAP),
                    type = KeyboardButtonType.PRIMARY,
                    icon = painterResource(R.drawable.ic_apply),
                    onClick = {
                        if ("${spendsViewModel.valueLeftDot}.${spendsViewModel.valueRightDot}" == "00000000.") {
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

                        runBlocking {
                            spendsViewModel.commitSpent()
                        }
                    }
                )
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