package com.danilkinkin.buckwheat.home

import androidx.activity.result.ActivityResultRegistryOwner
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.editor.Editor
import com.danilkinkin.buckwheat.history.History
import com.danilkinkin.buckwheat.keyboard.Keyboard
import com.danilkinkin.buckwheat.ui.colorBackground
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Quiet period after the last IME inset change before the editor follows the final height. */
private const val KEYBOARD_SETTLE_DELAY_MS = 100L

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
                statusBarDarkBackground = nightMode.value,
                navigationBarDarkBackground = nightMode.value,
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

        // The IME reports its height frame by frame while it animates. Feeding those intermediate
        // values into editorHeight made the sheet balloon for a few frames and collapse again:
        // the editor rendered, dropped, then came back up with the keyboard. Wait until the inset
        // stops changing and apply it in one step instead.
        var settledImeHeight by remember { mutableStateOf(0.dp) }
        LaunchedEffect(systemKeyboardHeight, appViewModel.showSystemKeyboard.value) {
            if (!appViewModel.showSystemKeyboard.value || systemKeyboardHeight == 0.dp) {
                settledImeHeight = 0.dp
            } else {
                delay(KEYBOARD_SETTLE_DELAY_MS)
                settledImeHeight = systemKeyboardHeight
            }
        }

        val internalKeyboardHeight = if (windowSizeClass == WindowWidthSizeClass.Compact) {
            contentWidth
        } else {
            contentWidth / 2f
        }
            .coerceAtMost(with(localDensity) { 500.dp.toPx() })
            .coerceAtMost(contentHeight / 2)

        val isShowSystemKeyboard =
            settledImeHeight != 0.dp && appViewModel.showSystemKeyboard.value

        val currentKeyboardHeight = if (isShowSystemKeyboard) {
            with(localDensity) { settledImeHeight.toPx() }
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
                // No fade: while the system keyboard rises it covers this area anyway, and a
                // second animation running against the IME transition is what made the switch
                // feel jittery.
                if (!isShowSystemKeyboard) {
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
                    // While the system keyboard is up it already covers the navigation bar, so
                    // reserving navigation bar space pushes the editor up and leaves a dead gap
                    // between the comment field and the keyboard.
                    val bottomInset = if (isShowSystemKeyboard) 0.dp else navigationBarOffset

                    val currentEditorHeight = with(localDensity) {
                        if (isShowSystemKeyboard) {
                            val halfExpanedOffset = (
                                    -contentHeight +
                                            bottomInset.toPx() +
                                            16.dp.toPx() +
                                            editorHeight
                                    ).coerceAtMost(0f)

                            (topSheetState.offset.value.coerceIn(
                                halfExpanedOffset,
                                0f
                            ) + contentHeight - bottomInset.toPx() - 16.dp.toPx()).toDp()
                        } else {
                            editorHeight.toDp()
                        }
                    }

                    TopSheetLayout(
                        swipeableState = topSheetState,
                        customHalfHeight = editorHeight,
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
                            modifier = Modifier.requiredHeight(with(localDensity) { editorHeight.toDp() }),
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
