package com.danilkinkin.buckwheat.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.*
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.editor.Editor
import com.danilkinkin.buckwheat.keyboard.Keyboard
import com.danilkinkin.buckwheat.recalcBudget.RecalcBudget
import com.danilkinkin.buckwheat.settings.Settings
import com.danilkinkin.buckwheat.spendsHistory.History
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorBackground
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import com.danilkinkin.buckwheat.wallet.FinishDateSelector
import com.danilkinkin.buckwheat.wallet.Wallet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    spendsViewModel: SpendsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
) {
    val topSheetState =
        androidx.compose.material.rememberSwipeableState(TopSheetValue.HalfExpanded)
    val walletSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val finishDateSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val settingsSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val recalcBudgetSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val presetFinishDate = remember { mutableStateOf<Date?>(null) }
    val requestFinishDateCallback = remember { mutableStateOf<((finishDate: Date) -> Unit)?>(null) }
    val snackbarHostState = remember { appViewModel.snackbarHostState }

    val localDensity = LocalDensity.current

    val snackBarMessage = stringResource(R.string.remove_spent)
    val snackBarAction = stringResource(R.string.remove_spent_undo)

    val isNightModeM = remember { mutableStateOf(false) }

    isNightModeM.value = isNightMode()

    setSystemStyle(
        style = {
            SystemBarState(
                statusBarColor = Color.Transparent,
                statusBarDarkIcons = !isNightModeM.value,
                navigationBarDarkIcons = false,
                navigationBarColor = Color.Transparent,
            )
        },
        key = isNightModeM.value,
    )



    LaunchedEffect(Unit) {
        spendsViewModel.lastRemoveSpent.collectLatest {
            val snackbarResult = snackbarHostState.showSnackbar(
                message = snackBarMessage,
                actionLabel = snackBarAction
            )

            if (snackbarResult == SnackbarResult.ActionPerformed) {
                spendsViewModel.undoRemoveSpent(it)
            }
        }
    }

    observeLiveData(spendsViewModel.requireReCalcBudget) {
        Log.d("MainScreen", "requireReCalcBudget = $it")
        if (it) {
            coroutineScope.launch {
                recalcBudgetSheetState.show()
            }
        }
    }

    observeLiveData(spendsViewModel.requireSetBudget) {
        Log.d("MainScreen", "requireSetBudget = $it")
        if (it) {
            coroutineScope.launch {
                walletSheetState.show()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBackground),
    ) {
        val contentHeight = constraints.maxHeight.toFloat()
        val contentWidth = constraints.maxWidth.toFloat()
        val editorHeight = contentHeight - contentWidth

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = with(localDensity) { editorHeight.toDp() })
        ) {
            Keyboard(
                modifier = Modifier
                    .height(with(localDensity) { contentWidth.toDp() })
                    .fillMaxWidth()
            )
        }

        TopSheetLayout(
            swipeableState = topSheetState,
            // sheetState = topSheetState,
            customHalfHeight = editorHeight,
            lockSwipeable = appViewModel.lockSwipeable,
            sheetContentHalfExpand = {
                Editor(
                    modifier = Modifier.height(with(localDensity) { editorHeight.toDp() }),
                    onOpenWallet = {
                        coroutineScope.launch {
                            walletSheetState.show()
                        }
                    },
                    onOpenSettings = {
                        coroutineScope.launch {
                            settingsSheetState.show()
                        }
                    },
                    onReaclcBudget = {
                        coroutineScope.launch {
                            recalcBudgetSheetState.show()
                        }
                    },
                    onOpenHistory = {
                        coroutineScope.launch {
                            topSheetState.animateTo(TopSheetValue.Expanded)
                        }
                    },
                )
            }
        ) {
            History()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateTopPadding()
                )
                .background(colorEditor.copy(alpha = 0.9F))
        )

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            SnackbarHost(hostState = snackbarHostState)

            if (false) {
                FloatingActionButton(
                    modifier = Modifier.padding(end = 24.dp, bottom = 24.dp),
                    onClick = {
                        coroutineScope.launch {
                            // topSheetState.halfExpand()
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_home),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )

                }
            }
        }

        BottomSheetWrapper(
            state = walletSheetState,
            cancelable = spendsViewModel.requireSetBudget.value == false,
        ) {
            Wallet(
                requestFinishDate = { presetValue, callback ->
                    coroutineScope.launch {
                        finishDateSheetState.show()

                        presetFinishDate.value = presetValue
                        requestFinishDateCallback.value = callback
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        walletSheetState.hide()
                    }
                }
            )
        }

        BottomSheetWrapper(state = finishDateSheetState) {
            FinishDateSelector(
                selectDate = presetFinishDate.value,
                onBackPressed = {
                    coroutineScope.launch {
                        finishDateSheetState.hide()
                    }
                },
                onApply = {
                    requestFinishDateCallback.value?.let { callback -> callback(it) }
                    coroutineScope.launch {
                        finishDateSheetState.hide()
                    }
                },
            )
        }

        BottomSheetWrapper(state = settingsSheetState) {
            Settings(
                onClose = {
                    coroutineScope.launch {
                        settingsSheetState.hide()
                    }
                }
            )
        }

        BottomSheetWrapper(
            state = recalcBudgetSheetState,
            cancelable = false,
        ) {
            RecalcBudget(
                onClose = {
                    coroutineScope.launch {
                        recalcBudgetSheetState.hide()
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun MainActivityPreview() {
    BuckwheatTheme {
        MainScreen()
    }
}
