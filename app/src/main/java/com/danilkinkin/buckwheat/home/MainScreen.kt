package com.danilkinkin.buckwheat.home

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.*
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.editor.Editor
import com.danilkinkin.buckwheat.finishPeriod.FINISH_PERIOD_SHEET
import com.danilkinkin.buckwheat.keyboard.Keyboard
import com.danilkinkin.buckwheat.onboarding.ON_BOARDING_SHEET
import com.danilkinkin.buckwheat.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.danilkinkin.buckwheat.spendsHistory.History
import com.danilkinkin.buckwheat.ui.*
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    windowSizeClass: WindowWidthSizeClass,
    spendsViewModel: SpendsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
) {
    val topSheetState = rememberSwipeableState(TopSheetValue.HalfExpanded)
    val coroutineScope = rememberCoroutineScope()

    val localDensity = LocalDensity.current

    val snackBarMessage = stringResource(R.string.remove_spent)
    val snackBarAction = stringResource(R.string.remove_spent_undo)

    val isNightModeM = remember { mutableStateOf(false) }

    isNightModeM.value = isNightMode()

    val isShowSystemKeyboard = WindowInsets.isImeVisible
    val systemKeyboardHeight = with(localDensity) {
        WindowInsets.ime.asPaddingValues().calculateBottomPadding().toPx()
    }

    setSystemStyle(
        style = {
            SystemBarState(
                statusBarColor = Color.Transparent,
                statusBarDarkIcons = !isNightModeM.value,
                navigationBarDarkIcons = !isNightModeM.value,
                navigationBarColor = Color.Transparent,
            )
        },
        key = isNightModeM.value,
    )

    LaunchedEffect(Unit) {
        spendsViewModel.lastRemoveSpent.collectLatest {
            val snackbarResult = appViewModel.snackbarHostState.showSnackbar(
                message = snackBarMessage,
                actionLabel = snackBarAction,
                duration = SnackbarDuration.Long,
            )

            if (snackbarResult == SnackbarResult.ActionPerformed) {
                spendsViewModel.undoRemoveSpent(it)
            }
        }
    }

    observeLiveData(spendsViewModel.requireReCalcBudget) {
        if (it) appViewModel.openSheet(PathState(RECALCULATE_DAILY_BUDGET_SHEET))
    }

    observeLiveData(spendsViewModel.requireSetBudget) {
        if (it) appViewModel.openSheet(PathState(ON_BOARDING_SHEET))
    }

    observeLiveData(spendsViewModel.finishPeriod) {
        if (it) appViewModel.openSheet(PathState(FINISH_PERIOD_SHEET))
    }

    val keyboardAdditionalOffset = (WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() - 16.dp).coerceAtLeast(0.dp)

    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBackground),
    ) {
        val contentHeight = constraints.maxHeight.toFloat()
        val keyboardHeight = if (windowSizeClass == WindowWidthSizeClass.Compact) {
            constraints.maxWidth.toFloat()
        } else {
            constraints.maxWidth.toFloat() / 2f
        }
            .coerceAtMost(with(localDensity) { 500.dp.toPx() })
            .coerceAtMost(contentHeight / 2)
        val currentKeyboardHeight by animateFloatAsState(
            targetValue = if (isShowSystemKeyboard && appViewModel.showSystemKeyboard.value) {
                systemKeyboardHeight
            } else {
                keyboardHeight
            }.coerceAtLeast(with(LocalDensity.current) { navigationBarHeight.toPx() - 16.dp.toPx() })
        )
        val editorHeight = contentHeight - currentKeyboardHeight - with(localDensity) { keyboardAdditionalOffset.toPx() }

        Log.d("currentKeyboardHeight", currentKeyboardHeight.toString())
        Log.d("editorHeight", editorHeight.toString())

        Row {
            if (windowSizeClass != WindowWidthSizeClass.Compact) {
                Surface(
                    color = colorEditor,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .navigationBarsPadding(),
                ) {
                    Box {
                        History()
                        StatusBarStub()
                        SnackbarHost()
                    }
                }
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .width(16.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isShowSystemKeyboard,
                    enter = fadeIn(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) + slideInVertically(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) { with(localDensity) { 10.dp.toPx().toInt() } },
                    exit = fadeOut(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) + slideOutVertically(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) { with(localDensity) { 10.dp.toPx().toInt() } },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = keyboardAdditionalOffset),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Keyboard(
                            modifier = Modifier
                                .height(with(localDensity) { keyboardHeight.toDp() })
                                .fillMaxWidth()
                        )
                    }
                }

                if (windowSizeClass == WindowWidthSizeClass.Compact) {
                    TopSheetLayout(
                        swipeableState = topSheetState,
                        customHalfHeight = editorHeight,
                        lockSwipeable = appViewModel.lockSwipeable,
                        sheetContentHalfExpand = {
                            Editor(
                                modifier = Modifier.height(with(localDensity) { editorHeight.toDp() }),
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

                    StatusBarStub()

                    SnackbarHost()
                } else {
                    Card(
                        shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorEditor,
                            contentColor = colorOnEditor,
                        ),
                    ) {
                        Editor(
                            modifier = Modifier.height(with(localDensity) { editorHeight.toDp() }),
                        )
                    }
                }
            }
        }

        BottomSheets(windowSizeClass)
    }
}

@Composable
fun BoxScope.SnackbarHost(
    appViewModel: AppViewModel = viewModel(),
) {
    val snackbarHostState = remember { appViewModel.snackbarHostState }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        SwipeableSnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
fun StatusBarStub() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(
                WindowInsets.systemBars
                    .asPaddingValues()
                    .calculateTopPadding()
            )
            .background(colorEditor.copy(alpha = 0.9F))
    )
}