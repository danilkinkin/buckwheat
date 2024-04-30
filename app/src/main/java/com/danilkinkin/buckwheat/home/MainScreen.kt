package com.danilkinkin.buckwheat.home

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.LocalWindowSize
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.SwipeableSnackbarHost
import com.danilkinkin.buckwheat.base.TopSheetLayout
import com.danilkinkin.buckwheat.base.TopSheetValue
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.editor.Editor
import com.danilkinkin.buckwheat.analytics.ANALYTICS_SHEET
import com.danilkinkin.buckwheat.history.History
import com.danilkinkin.buckwheat.keyboard.Keyboard
import com.danilkinkin.buckwheat.onboarding.ON_BOARDING_SHEET
import com.danilkinkin.buckwheat.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.danilkinkin.buckwheat.ui.colorBackground
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    activityResultRegistryOwner: ActivityResultRegistryOwner?,
    spendsViewModel: SpendsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
) {
    val topSheetState = rememberSwipeableState(TopSheetValue.HalfExpanded)
    val coroutineScope = rememberCoroutineScope()
    val nightMode = remember { mutableStateOf(false) }

    val localDensity = LocalDensity.current
    val windowSizeClass = LocalWindowSize.current
    val windowInsets = LocalWindowInsets.current

    val snackBarMessage = stringResource(R.string.remove_spent)
    val snackBarAction = stringResource(R.string.remove_spent_undo)

    nightMode.value = isNightMode()

    setSystemStyle(
        style = {
            SystemBarState(
                statusBarColor = Color.Transparent,
                statusBarDarkIcons = !nightMode.value,
                navigationBarDarkIcons = !nightMode.value,
                navigationBarColor = Color.Transparent,
            )
        },
        key = nightMode.value,
    )

    observeLiveData(spendsViewModel.lastRemovedTransaction) {
        appViewModel.showSnackbar(
            message = snackBarMessage,
            actionLabel = snackBarAction,
            duration = SnackbarDuration.Long,
        ) { snackbarResult ->
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                spendsViewModel.undoRemoveSpent()
            }
        }
    }

    observeLiveData(spendsViewModel.requireDistributionRestedBudget) {
        if (it) appViewModel.openSheet(PathState(RECALCULATE_DAILY_BUDGET_SHEET))
    }

    observeLiveData(spendsViewModel.requireSetBudget) {
        if (it) appViewModel.openSheet(PathState(ON_BOARDING_SHEET))
    }

    observeLiveData(spendsViewModel.periodFinished) {
        if (it) appViewModel.openSheet(PathState(ANALYTICS_SHEET))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBackground),
    ) {
        val contentHeight = constraints.maxHeight.toFloat()
        val contentWidth = constraints.maxWidth.toFloat()

        val keyboardAdditionalOffset = windowInsets
            .calculateBottomPadding()
            .minus(16.dp)
            .coerceAtLeast(0.dp)

        val navigationBarOffset = windowInsets
            .calculateBottomPadding()
            .coerceAtLeast(16.dp)

        val systemKeyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        val internalKeyboardHeight = if (windowSizeClass == WindowWidthSizeClass.Compact) {
            contentWidth
        } else {
            contentWidth / 2f
        }
            .coerceAtMost(with(localDensity) { 500.dp.toPx() })
            .coerceAtMost(contentHeight / 2)

        val isShowSystemKeyboard =
            systemKeyboardHeight != 0.dp && appViewModel.showSystemKeyboard.value
        val isRequestedShowSystemKeyboard =
            systemKeyboardHeight != 0.dp || appViewModel.showSystemKeyboard.value

        val currentKeyboardHeight = if (isShowSystemKeyboard) {
            with(localDensity) { systemKeyboardHeight.toPx() }
        } else {
            internalKeyboardHeight
        }

        val editorHeight by remember(
            contentHeight,
            currentKeyboardHeight,
            isShowSystemKeyboard,
            keyboardAdditionalOffset,
            navigationBarOffset
        ) {
            derivedStateOf {
                contentHeight
                    .minus(
                        currentKeyboardHeight
                            .plus(with(localDensity) {
                                if (isShowSystemKeyboard) {
                                    16.dp.toPx()
                                } else {
                                    keyboardAdditionalOffset.toPx()
                                }
                            })
                            .coerceAtLeast(0f)
                    )
                    .coerceAtMost(contentHeight - with(localDensity) { navigationBarOffset.toPx() + 96.dp.toPx() })
            }
        }

        val editorHeightAnimated by animateFloatAsState(
            label = "editorHeightAnimatedValue",
            targetValue = editorHeight,
            animationSpec = tween(durationMillis = 350),
        )

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
                Spacer(
                    Modifier
                        .fillMaxHeight()
                        .width(16.dp)
                )
            }
            Box(
                Modifier
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
                                .height(with(localDensity) { internalKeyboardHeight.toDp() })
                                .fillMaxWidth()
                        )
                    }
                }

                if (windowSizeClass == WindowWidthSizeClass.Compact) {
                    val currentEditorHeight = with(localDensity) {
                        if (isRequestedShowSystemKeyboard) {
                            val halfExpanedOffset = (
                                    -contentHeight +
                                            navigationBarOffset.toPx() +
                                            16.dp.toPx() +
                                            editorHeightAnimated
                                    ).coerceAtMost(0f)

                            (topSheetState.offset.value.coerceIn(
                                halfExpanedOffset,
                                0f
                            ) + contentHeight - navigationBarOffset.toPx() - 16.dp.toPx()).toDp()
                        } else {
                            editorHeightAnimated.toDp()
                        }
                    }

                    TopSheetLayout(
                        swipeableState = topSheetState,
                        customHalfHeight = editorHeightAnimated,
                        lockSwipeable = appViewModel.lockSwipeable,
                        lockDraggable = appViewModel.lockDraggable,
                        sheetContentHalfExpand = {
                            Editor(
                                modifier = Modifier.requiredHeight(currentEditorHeight),
                                onOpenHistory = {
                                    coroutineScope.launch {
                                        topSheetState.animateTo(TopSheetValue.Expanded)
                                    }
                                },
                            )
                        }
                    ) {
                        History(
                            onClose = {
                                coroutineScope.launch {
                                    topSheetState.animateTo(TopSheetValue.HalfExpanded)
                                }
                            }
                        )
                    }

                    StatusBarStub()
                } else {
                    Card(
                        shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorEditor,
                            contentColor = colorOnEditor,
                        ),
                    ) {
                        Editor(
                            modifier = Modifier.requiredHeight(with(localDensity) { editorHeightAnimated.toDp() }),
                        )
                    }
                }
            }
        }

        BottomSheets(activityResultRegistryOwner)

        if (windowSizeClass == WindowWidthSizeClass.Compact) {
            SnackbarHost()
        }
    }
}

@Composable
fun BoxScope.SnackbarHost(
    appViewModel: AppViewModel = viewModel(),
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        SwipeableSnackbarHost(hostState = remember { appViewModel._snackbarHostState })
    }
}

@Composable
fun StatusBarStub() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(
                LocalWindowInsets.current.calculateTopPadding()
            )
            .background(colorEditor.copy(alpha = 0.9F))
    )
}
