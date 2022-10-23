package com.danilkinkin.buckwheat.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorBackground
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    spendsViewModel: SpendsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
) {
    val topSheetState = rememberSwipeableState(TopSheetValue.HalfExpanded)
    val coroutineScope = rememberCoroutineScope()
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
                navigationBarDarkIcons = !isNightModeM.value,
                navigationBarColor = Color.Transparent,
            )
        },
        key = isNightModeM.value,
    )

    LaunchedEffect(Unit) {
        spendsViewModel.lastRemoveSpent.collectLatest {
            val snackbarResult = snackbarHostState.showSnackbar(
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBackground),
    ) {
        val contentHeight = constraints.maxHeight.toFloat()
        val contentWidth = constraints.maxWidth.toFloat()
        val editorHeight = contentHeight - contentWidth - with(localDensity) { keyboardAdditionalOffset.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = keyboardAdditionalOffset),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Keyboard(
                modifier = Modifier
                    .height(with(localDensity) { contentWidth.toDp() })
                    .fillMaxWidth()
            )
        }

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
            SwipeableSnackbarHost(hostState = snackbarHostState)
        }

        BottomSheets()
    }
}

@Preview
@Composable
fun MainActivityPreview() {
    BuckwheatTheme {
        MainScreen()
    }
}
