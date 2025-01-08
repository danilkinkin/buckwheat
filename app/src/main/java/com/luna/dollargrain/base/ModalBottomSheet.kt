package com.luna.dollargrain.base


import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.util.PreUpPostDownNestedScrollConnection
import com.luna.dollargrain.util.SwipeableState
import com.luna.dollargrain.util.swipeable
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@ExperimentalMaterialApi
enum class ModalBottomSheetValue { Hidden, Expanded, HalfExpanded }

@ExperimentalMaterialApi
class ModalBottomSheetState(
    initialValue: ModalBottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    internal val isSkipHalfExpanded: Boolean,
    confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true },
    render: Boolean,
) : SwipeableState<ModalBottomSheetValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {
    var render: Boolean by mutableStateOf(render)
    var args: Map<String, Any?> by mutableStateOf(emptyMap())
    var callback: (result: Map<String, Any?>) -> Unit by mutableStateOf({})

    val isVisible: Boolean
        get() = currentValue != ModalBottomSheetValue.Hidden

    private val hasHalfExpandedState: Boolean
        get() = anchors.values.contains(ModalBottomSheetValue.HalfExpanded)

    init {
        if (isSkipHalfExpanded) {
            require(initialValue != ModalBottomSheetValue.HalfExpanded) {
                "The initial value must not be set to HalfExpanded if skipHalfExpanded is set to" +
                        " true."
            }
        }
    }

    suspend fun realShow() {
        val targetValue = when {
            //hasHalfExpandedState -> ModalBottomSheetValue.HalfExpanded
            else -> ModalBottomSheetValue.Expanded
        }
        animateTo(targetValue = targetValue)
    }

    fun show(args: Map<String, Any?>) {
        this.args = args
        this.render = true
    }

    suspend fun show() {
        show(emptyMap())
    }

    fun bindCallback(callback: (result: Map<String, Any?>) -> Unit) {
        this.callback = callback
    }

    suspend fun hide(result: Map<String, Any?>) {
        callback(result)
        animateTo(ModalBottomSheetValue.Hidden)
    }

    suspend fun hide() {
        hide(emptyMap())
    }

    internal val nestedScrollConnection = this.PreUpPostDownNestedScrollConnection

    companion object {
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            skipHalfExpanded: Boolean,
            confirmStateChange: (ModalBottomSheetValue) -> Boolean,
        ): Saver<ModalBottomSheetState, *> = Saver(
            save = { Pair(it.currentValue, it.render) },
            restore = { (value, render) ->
                ModalBottomSheetState(
                    initialValue = value,
                    animationSpec = animationSpec,
                    isSkipHalfExpanded = skipHalfExpanded,
                    confirmStateChange = confirmStateChange,
                    render = render,
                )
            }
        )
    }
}

@Composable
@ExperimentalMaterialApi
fun rememberModalBottomSheetState(
    initialValue: ModalBottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    skipHalfExpanded: Boolean,
    confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true },
    render: Boolean = initialValue !== ModalBottomSheetValue.Hidden,
): ModalBottomSheetState {
    return rememberSaveable(
        initialValue,
        animationSpec,
        skipHalfExpanded,
        confirmStateChange,
        render,
        saver = ModalBottomSheetState.Saver(
            animationSpec = animationSpec,
            skipHalfExpanded = skipHalfExpanded,
            confirmStateChange = confirmStateChange,
        )
    ) {
        ModalBottomSheetState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            isSkipHalfExpanded = skipHalfExpanded,
            confirmStateChange = confirmStateChange,
            render = render,
        )
    }
}

@Composable
@ExperimentalMaterialApi
fun rememberModalBottomSheetState(
    initialValue: ModalBottomSheetValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true },
): ModalBottomSheetState = rememberModalBottomSheetState(
    initialValue = initialValue,
    animationSpec = animationSpec,
    skipHalfExpanded = true,
    confirmStateChange = confirmStateChange,
    render = initialValue !== ModalBottomSheetValue.Hidden
)

@Composable
@ExperimentalMaterialApi
fun ModalBottomSheetLayout(
    sheetContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    cancelable: Boolean = true,
    predictiveBackProgress: Float = 0f,
    content: @Composable () -> Unit
) {
    val localDensity = LocalDensity.current
    val scope = rememberCoroutineScope()



    BoxWithConstraints(modifier, contentAlignment = Alignment.TopEnd) {
        val fullHeight = constraints.maxHeight.toFloat()
        val sheetHeightState = remember { mutableStateOf<Float?>(null) }
        Box(Modifier.fillMaxSize()) {
            content()
            Scrim(
                color = scrimColor,
                onDismiss = {
                    if (cancelable && sheetState.confirmStateChange(ModalBottomSheetValue.Hidden)) {
                        scope.launch { sheetState.hide() }
                    }
                },
                visible = sheetState.targetValue != ModalBottomSheetValue.Hidden,
                predictiveBackProgress = predictiveBackProgress
            )
        }

        RenderAdaptivePane(
            contentAlignment = Alignment.TopEnd
        ) {

            val offset = IntOffset.let {
                val y = if (sheetState.anchors.isEmpty()) {
                    // if we don't know our anchors yet, render the sheet as hidden
                    fullHeight.roundToInt()
                } else {
                    // if we do know our anchors, respect them
                    sheetState.offset.value.roundToInt()
                }

                IntOffset(0, y)
            }
            val navigationBarHeight = with(localDensity) {
                LocalWindowInsets.current.calculateBottomPadding().roundToPx()
            }


            Surface(
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (cancelable) {
                            Modifier.nestedScroll(sheetState.nestedScrollConnection)
                        } else {
                            Modifier
                        }
                    )
                    .offset { offset }
                    .offset { offset.copy(y = offset.y + navigationBarHeight) }
                    .scale(1f - predictiveBackProgress * 0.08f)
                    .offset(y = predictiveBackProgress * 64.dp)
                    .offset { offset.copy(y = -offset.y - navigationBarHeight) }
                    .bottomSheetSwipeable(sheetState, fullHeight, sheetHeightState, cancelable),
                shape = sheetShape,
                elevation = sheetElevation,
                color = sheetBackgroundColor,
                contentColor = sheetContentColor
            ) {
                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            sheetHeightState.value = it.size.height.toFloat()
                        }
                ) {
                    sheetContent()
                }
            }
        }
    }
}

@Suppress("ModifierInspectorInfo")
@OptIn(ExperimentalMaterialApi::class)
private fun Modifier.bottomSheetSwipeable(
    sheetState: ModalBottomSheetState,
    fullHeight: Float,
    sheetHeightState: State<Float?>,
    cancelable: Boolean = true,
): Modifier {
    val sheetHeight = sheetHeightState.value

    val modifier = if (sheetHeight != null) {
        val anchors = if (sheetHeight < fullHeight / 2 || sheetState.isSkipHalfExpanded) {
            mapOf(
                fullHeight to ModalBottomSheetValue.Hidden,
                fullHeight - sheetHeight to ModalBottomSheetValue.Expanded
            )
        } else {
            mapOf(
                fullHeight to ModalBottomSheetValue.Hidden,
                fullHeight / 2 to ModalBottomSheetValue.HalfExpanded,
                max(0f, fullHeight - sheetHeight) to ModalBottomSheetValue.Expanded
            )
        }
        Modifier.swipeable(
            state = sheetState,
            anchors = anchors,
            orientation = Orientation.Vertical,
            enabled = cancelable && (sheetState.currentValue != ModalBottomSheetValue.Hidden),
            resistance = null
        )
    } else {
        Modifier
    }

    return this.then(modifier)
}

@Composable
private fun Scrim(
    color: Color,
    onDismiss: () -> Unit,
    visible: Boolean,
    predictiveBackProgress: Float = 0f
) {
    if (color.isSpecified) {
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = TweenSpec()
        )

        val dismissModifier = if (visible) {
            Modifier
                .pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
                .semantics(mergeDescendants = true) {
                    contentDescription = null.toString()
                    onClick { onDismiss(); true }
                }
        } else {
            Modifier
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissModifier)
        ) {
            drawRect(color = color, alpha = alpha * (1f - predictiveBackProgress * 0.6f))
        }
    }
}


object ModalBottomSheetDefaults {
    val Elevation = 16.dp
    val scrimColor: Color
        @Composable
        get() = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
}