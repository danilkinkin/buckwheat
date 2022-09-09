package com.danilkinkin.buckwheat.topSheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

@ExperimentalMaterialApi
enum class TopSheetValue {
    Expanded,
    HalfExpanded
}

@ExperimentalMaterialApi
class TopSheetState(
    initialValue: TopSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    internal val isSkipHalfExpanded: Boolean,
    confirmStateChange: (TopSheetValue) -> Boolean = { true },
    internal val sheetHeightFloat: MutableState<Float>,
) : SwipeableState<TopSheetValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {
    val isExpand: Boolean
        get() = currentValue != TopSheetValue.Expanded

    val sheetHeight: Float
        get() = this.sheetHeightFloat.value

    constructor(
        initialValue: TopSheetValue,
        animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
        confirmStateChange: (TopSheetValue) -> Boolean = { true },
        sheetHeightFloat: MutableState<Float>,
    ) : this(initialValue, animationSpec, isSkipHalfExpanded = false, confirmStateChange, sheetHeightFloat)

    init {
        if (isSkipHalfExpanded) {
            require(initialValue != TopSheetValue.HalfExpanded) {
                "The initial value must not be set to HalfExpanded if skipHalfExpanded is set to" +
                        " true."
            }
        }
    }

    internal suspend fun halfExpand() {
        animateTo(TopSheetValue.HalfExpanded)
    }

    internal suspend fun expand() = animateTo(TopSheetValue.Expanded)

    internal val nestedScrollConnection = this.PreUpPostTopNestedScrollConnection

    companion object {
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            skipHalfExpanded: Boolean,
            confirmStateChange: (TopSheetValue) -> Boolean
        ): Saver<TopSheetState, *> = Saver(
            save = { Pair(it.currentValue, it.sheetHeightFloat) },
            restore = { (currentValue, sheetHeightFloat) ->
                TopSheetState(
                    initialValue = currentValue,
                    animationSpec = animationSpec,
                    isSkipHalfExpanded = skipHalfExpanded,
                    confirmStateChange = confirmStateChange,
                    sheetHeightFloat = sheetHeightFloat,
                )
            }
        )
    }
}

@Composable
@ExperimentalMaterialApi
fun rememberTopSheetState(
    initialValue: TopSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    skipHalfExpanded: Boolean,
    confirmStateChange: (TopSheetValue) -> Boolean = { true },
    sheetHeightFloat: MutableState<Float>,
): TopSheetState {
    return rememberSaveable(
        initialValue, animationSpec, skipHalfExpanded, confirmStateChange,
        saver = TopSheetState.Saver(
            animationSpec = animationSpec,
            skipHalfExpanded = skipHalfExpanded,
            confirmStateChange = confirmStateChange
        )
    ) {
        TopSheetState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            isSkipHalfExpanded = skipHalfExpanded,
            confirmStateChange = confirmStateChange,
            sheetHeightFloat = sheetHeightFloat,
        )
    }
}

/**
 * Create a [TopSheetState] and [remember] it.
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Composable
@ExperimentalMaterialApi
fun rememberTopSheetState(
    initialValue: TopSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (TopSheetValue) -> Boolean = { true }
): TopSheetState = rememberTopSheetState(
    initialValue = initialValue,
    animationSpec = animationSpec,
    skipHalfExpanded = false,
    confirmStateChange = confirmStateChange,
    sheetHeightFloat = mutableStateOf(0f)
)

@Composable
@ExperimentalMaterialApi
fun TopSheetLayout(
    modifier: Modifier = Modifier,
    sheetState: TopSheetState = rememberTopSheetState(TopSheetValue.HalfExpanded),
    halfHeight: Float? = null,
    scrollState: LazyListState = rememberLazyListState(),
    itemsCount: Int = 1,
    sheetContent: LazyListScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        val fullHeight = constraints.maxHeight.toFloat()
        val halfHeight = halfHeight ?: (fullHeight / 2)
        val sheetHeightState = remember { mutableStateOf<Float?>(null) }

        Box(
            Modifier
                .fillMaxWidth()
                .nestedScroll(sheetState.nestedScrollConnection)
                .offset {
                    val y = if (sheetState.anchors.isEmpty()) {
                        // if we don't know our anchors yet, render the sheet as hidden
                        -fullHeight.roundToInt()
                    } else {
                        // if we do know our anchors, respect them
                        sheetState.offset.value.roundToInt()
                    }
                    IntOffset(0, y)
                }
                .topSheetSwipeable(
                    sheetState,
                    fullHeight,
                    halfHeight,
                    sheetHeightState,
                )
        ) {
            Box {
                DisposableEffect(
                    key1 = itemsCount,
                    effect = {
                        if (itemsCount == 0) return@DisposableEffect onDispose {  }

                        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                            scrollState.scrollToItem(itemsCount)
                        }

                        onDispose {  }
                    }
                )

                LazyColumn(
                    state = scrollState,
                    content = sheetContent,
                    modifier = Modifier.onGloballyPositioned {
                        sheetHeightState.value = it.size.height.toFloat()
                        sheetState.sheetHeightFloat.value = it.size.height.toFloat()
                    },
                )
            }
        }
    }
}

@Suppress("ModifierInspectorInfo")
@OptIn(ExperimentalMaterialApi::class)
private fun Modifier.topSheetSwipeable(
    sheetState: TopSheetState,
    fullHeight: Float,
    halfHeight: Float,
    sheetHeightState: State<Float?>
): Modifier {
    val sheetHeight = sheetHeightState.value

    val modifier = if (sheetHeight != null) {
        val anchors = mapOf(
            -(sheetHeight - halfHeight) to TopSheetValue.HalfExpanded,
            min(0f, fullHeight - (fullHeight - sheetHeight)) to TopSheetValue.Expanded
        )

        Modifier.swipeable(
            state = sheetState,
            anchors = anchors,
            orientation = Orientation.Vertical,
            resistance = null
        )
    } else {
        Modifier
    }

    return this.then(modifier)
}