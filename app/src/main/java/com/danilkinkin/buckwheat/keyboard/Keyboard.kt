package com.danilkinkin.buckwheat.keyboard

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val BUTTON_GAP = 4.dp

@Composable
fun Keyboard(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()

    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = navigationBarHeight)
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
                        spendsViewModel.executeAction(SpendsViewModel.Action.PUT_NUMBER, i)
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
                    spendsViewModel.executeAction(SpendsViewModel.Action.REMOVE_LAST)
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
                                spendsViewModel.executeAction(SpendsViewModel.Action.PUT_NUMBER, i)
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
                                spendsViewModel.executeAction(SpendsViewModel.Action.PUT_NUMBER, i)
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
                            spendsViewModel.executeAction(SpendsViewModel.Action.PUT_NUMBER, 0)
                        }
                    )
                    KeyboardButton(
                        modifier = Modifier
                            .weight(1F)
                            .padding(BUTTON_GAP),
                        type = KeyboardButtonType.DEFAULT,
                        text = ".",
                        onClick = {
                            spendsViewModel.executeAction(SpendsViewModel.Action.SET_DOT)
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