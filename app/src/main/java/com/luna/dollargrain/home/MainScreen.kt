package com.luna.dollargrain.home

import androidx.activity.compose.LocalActivityResultRegistryOwner
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.LocalWindowSize
import com.luna.dollargrain.base.SwipeableSnackbarHost
import com.luna.dollargrain.base.TopSheetLayout
import com.luna.dollargrain.base.TopSheetValue
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.data.SystemBarState
import com.luna.dollargrain.editor.Editor
import com.luna.dollargrain.analytics.ANALYTICS_SHEET
import com.luna.dollargrain.history.History
import com.luna.dollargrain.keyboard.Keyboard
import com.luna.dollargrain.onboarding.ON_BOARDING_SHEET
import com.luna.dollargrain.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.luna.dollargrain.ui.colorEditor
import com.luna.dollargrain.ui.colorOnEditor
import com.luna.dollargrain.ui.isNightMode
import com.luna.dollargrain.util.observeLiveData
import com.luna.dollargrain.util.setSystemStyle
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

    val snackBarMessage = "money returned to budget!!"
    val snackBarAction = "undo"

    nightMode.value = isNightMode()

    // sets status bar and nav bar to transparent
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

    // errrrrrr
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
            .background(MaterialTheme.colorScheme.background),
    ) {
        val contentHeight = constraints.maxHeight.toFloat()
        val contentWidth = constraints.maxWidth.toFloat()

        // keyboard initializing
        val keyboardAdditionalOffset = windowInsets
            .calculateBottomPadding()
            .minus(16.dp)
            .coerceAtLeast(0.dp)

        val navigationBarOffset = windowInsets
            .calculateBottomPadding()
            .coerceAtLeast(16.dp)

        val internalKeyboardHeight = if (windowSizeClass == WindowWidthSizeClass.Compact) {
            contentWidth
        } else {
            contentWidth / 2f
        }
            .coerceAtMost(with(localDensity) { 500.dp.toPx() })
            .coerceAtMost(contentHeight / 2)

        val currentKeyboardHeight = internalKeyboardHeight

        val editorHeight by remember(
            contentHeight,
            currentKeyboardHeight,
            keyboardAdditionalOffset,
            navigationBarOffset
        ) {
            derivedStateOf {
                contentHeight
                    .minus(
                        currentKeyboardHeight
                            .plus(with(localDensity) {
                                keyboardAdditionalOffset.toPx()
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
                // internal keyboard
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
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
                        editorHeightAnimated.toDp()
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun Preview() {
    MainScreen(LocalActivityResultRegistryOwner.current)
}
