package com.danilkinkin.buckwheat.base

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

data class BottomSheetScrollState(
    val topPadding: Dp,
)

val LocalBottomSheetScrollState = compositionLocalOf { BottomSheetScrollState(0.dp) }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetWrapper(
    name: String,
    appViewModel: AppViewModel = viewModel(),
    cancelable: Boolean = true,
    state: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    content: @Composable (state: ModalBottomSheetState) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    observeLiveData(appViewModel.sheetStates) { sheets ->
        if (sheets.containsKey(name)) {
            state.bindCallback(sheets[name]!!.callback)
            coroutineScope.launch { state.show(sheets[name]!!.args) }
        } else if (state.targetValue !== ModalBottomSheetValue.Hidden) {
            coroutineScope.launch { state.hide() }
        }
    }

    DisposableEffect(state.render) {
        if (state.render) coroutineScope.launch { state.realShow() }

        onDispose { }
    }

    DisposableEffect(state.currentValue) {
        if (state.currentValue === ModalBottomSheetValue.Hidden) {
            state.render = false
            appViewModel.closeSheet(name)
        }

        onDispose { }
    }

    if (!state.render) return

    val localDensity = LocalDensity.current

    val statusBarHeight = LocalWindowInsets.current.calculateTopPadding()
    val isNightModeNow = isNightMode()

    setSystemStyle(
        style = {
            SystemBarState(
                statusBarDarkIcons = false,
                navigationBarDarkIcons = !isNightModeNow,
            )
        },
        key = state.targetValue,
        confirmChange = { state.targetValue !== ModalBottomSheetValue.Hidden },
    )

    // The top corner radius follows the drag position. Reading the raw offset here rebuilt a
    // RoundedCornerShape and recomposed the whole sheet on every frame. Quantised to 5% steps,
    // the shape is rebuilt a handful of times per drag - indistinguishable over 28dp of travel.
    val statusBarFillProgress by remember(statusBarHeight) {
        derivedStateOf {
            if (statusBarHeight == 0.dp) {
                0f
            } else {
                val remaining = with(localDensity) {
                    max(statusBarHeight - state.offset.value.roundToInt().toDp(), 0.toDp())
                }
                ((remaining / statusBarHeight) * 20f).roundToInt() / 20f
            }.coerceIn(0f, 1f)
        }
    }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    var predictiveBackProgress by remember {
        mutableFloatStateOf(0f)
    }

    ModalBottomSheetLayout(
        cancelable = cancelable,
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetState = state,
        predictiveBackProgress = predictiveBackProgress,
        sheetShape = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
            topStart = CornerSize(28.dp * (1F - statusBarFillProgress)),
            topEnd = CornerSize(28.dp * (1F - statusBarFillProgress)),
        ),
        sheetContent = {
            setSystemStyle(
                style = {
                    SystemBarState(
                        statusBarDarkIcons = !isNightModeNow,
                        navigationBarDarkIcons = !isNightModeNow,
                    )
                },
                key = statusBarFillProgress,
                confirmChange = { statusBarFillProgress > 0.5F },
            )

            Box {
                CompositionLocalProvider(
                    LocalBottomSheetScrollState provides BottomSheetScrollState(
                        topPadding = statusBarHeight * statusBarFillProgress,
                    )
                ) {
                    content(state)
                }
            }

            if (cancelable) {
                Box(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        Modifier
                            .height(4.dp)
                            .width(30.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .align(Alignment.Center)
                    )
                }
            }
        }
    ) {}

    PredictiveBackHandler(state.isVisible) { progress ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
            }

            coroutineScope.launch {
                appViewModel.closeSheet(name = name)
            }
        } catch (e: CancellationException) {
            predictiveBackProgress = 0f
        }
    }
}