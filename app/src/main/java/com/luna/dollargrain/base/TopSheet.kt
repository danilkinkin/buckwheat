package com.luna.dollargrain.base

import android.view.MotionEvent
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.R
import com.luna.dollargrain.base.balloon.BalloonScope
import com.luna.dollargrain.base.balloon.rememberBalloonState
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.di.TUTORIAL_STAGE
import com.luna.dollargrain.di.TUTORS
import com.luna.dollargrain.ui.colorEditor
import com.luna.dollargrain.ui.colorOnEditor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.max
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

@ExperimentalMaterialApi
enum class TopSheetValue {
    Expanded,
    HalfExpanded
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@ExperimentalMaterialApi
fun TopSheetLayout(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel(),
    swipeableState: SwipeableState<TopSheetValue> = rememberSwipeableState(TopSheetValue.HalfExpanded),
    customHalfHeight: Float? = null,
    lockSwipeable: MutableState<Boolean>,
    lockDraggable: MutableState<Boolean>,
    sheetContentHalfExpand: @Composable () -> Unit,
    sheetContentExpand: @Composable () -> Unit,
) {
    val localDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val tutorial by appViewModel.getTutorialStage(TUTORS.OPEN_HISTORY).observeAsState(TUTORIAL_STAGE.NONE)

    var lock by remember { mutableStateOf(false) }
    var scroll by remember { mutableStateOf(false) }

    var predictiveBackProgress by remember {
        mutableFloatStateOf(0f)
    }

    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        val fullHeight = constraints.maxHeight.toFloat()
        val halfHeight = customHalfHeight ?: (fullHeight / 2)
        val expandHeight =
            with(localDensity) { (fullHeight - navigationBarHeight.toPx() - 16.dp.toPx()) }
        val currOffset = swipeableState.offset.value
        val maxOffset = (-(expandHeight - halfHeight)).coerceAtMost(0f)

        val prevHalfHeight = remember { mutableFloatStateOf(halfHeight) }
        val isLockProgress = remember(swipeableState.isAnimationRunning) {
            mutableStateOf(prevHalfHeight.value != halfHeight && swipeableState.isAnimationRunning)
        }

        val progress = if (isLockProgress.value) {
            if (swipeableState.currentValue === TopSheetValue.HalfExpanded) 0f else 1f
        } else {
            (1f - (currOffset / maxOffset)).coerceIn(0f, 1f)
        }

        prevHalfHeight.value = halfHeight

        val connection = remember {
            object : NestedScrollConnection {

                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    val delta = available.y

                    if (!scroll && !lockSwipeable.value) {
                        lock = false
                    }

                    scroll = true

                    if (lockSwipeable.value) lock = true

                    return if (lock || lockSwipeable.value) {
                        super.onPreScroll(available, source)
                    } else {
                        swipeableState.performDrag(delta).toOffset()
                    }
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    lock = lockSwipeable.value
                    scroll = false

                    return if (!lockSwipeable.value) {
                        swipeableState.performFling(available.y)
                        available
                    } else {
                        super.onPreFling(available)
                    }
                }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity {
                    scroll = false
                    swipeableState.performFling(velocity = available.y)
                    return super.onPostFling(consumed, available)
                }

                private fun Float.toOffset() = Offset(0f, this)
            }
        }


        Box(Modifier.fillMaxSize()) {
            Scrim(
                color = ModalBottomSheetDefaults.scrimColor,
                targetValue = (progress * 5).coerceIn(0f, 1f) * (1f - predictiveBackProgress * 0.7f),
            )
        }

        val halfExpanedOffset = (-(expandHeight - halfHeight)).coerceAtMost(0f)

        Card(
            shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorEditor,
                contentColor = colorOnEditor,
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(with(localDensity) {
                    (fullHeight - navigationBarHeight.toPx() - 16.dp.toPx()).toDp()
                })
                .nestedScroll(connection)
                .offset {
                    val swipeOffset = swipeableState.offset.value
                    val predictiveOffset = halfExpanedOffset * predictiveBackProgress * 0.3f

                    IntOffset(
                        x = 0,
                        y = (swipeOffset + predictiveOffset)
                            .coerceIn(halfExpanedOffset, 0f)
                            .roundToInt(),
                    )
                }
                .swipeable(
                    enabled = !lockDraggable.value,
                    state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        halfExpanedOffset to TopSheetValue.HalfExpanded,
                        0f to TopSheetValue.Expanded
                    ),
                )

        ) {
            Box(modifier = modifier.fillMaxSize()) {
                if (progress != 0f) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .alpha(max(progress * 2f - 1f, 0f))
                    ) {
                        sheetContentExpand()
                    }

                    DisposableEffect(Unit) {
                        if (tutorial !== TUTORIAL_STAGE.PASSED) {
                            appViewModel.passTutorial(TUTORS.OPEN_HISTORY)
                        }

                        appViewModel.topSheetDown.value = true

                        onDispose {
                            appViewModel.topSheetDown.value = false
                        }
                    }
                }

                if (progress != 1f) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .alpha(max(1f - progress * 2, 0f)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1F)
                                .background(colorEditor)
                        )
                        sheetContentHalfExpand()
                    }
                }

                Box(
                    Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorEditor.copy(alpha = 0f),
                                    colorEditor.copy(
                                        alpha = progress
                                            .roundToInt()
                                            .toFloat()
                                    ),
                                ),
                                startY = 20f,
                                endY = 80f,
                            )
                        )
                        .pointerInteropFilter {
                            when (it.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    if (swipeableState.currentValue === TopSheetValue.Expanded) {
                                        lock = false

                                        true
                                    } else {
                                        false
                                    }
                                }

                                else -> false
                            }
                        }
                        .padding(bottom = 10.dp, top = 32.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    val balloonState = rememberBalloonState()

                    BalloonScope(
                        Modifier
                            .height(4.dp)
                            .width(30.dp)
                            .align(Alignment.Center),
                        balloonState = balloonState,
                        content = {
                            Text(
                                text = "view spending history",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClose = {
                            appViewModel.passTutorial(TUTORS.OPEN_HISTORY)
                        }
                    ) {
                        Box(
                            Modifier
                                .height(4.dp)
                                .width(30.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                        .copy(alpha = if (lockDraggable.value) 0f else 0.3f),
                                    shape = CircleShape
                                )
                                .align(Alignment.Center)
                        )
                    }

                    DisposableEffect(tutorial) {
                        coroutineScope.launch {
                            delay(1000)
                            if (tutorial == TUTORIAL_STAGE.READY_TO_SHOW) {
                                balloonState.show()
                            }
                        }


                        onDispose { }
                    }
                }
            }
        }
    }

    PredictiveBackHandler(swipeableState.currentValue === TopSheetValue.Expanded) { progress ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
            }

            coroutineScope.launch {
                swipeableState.animateTo(TopSheetValue.HalfExpanded)
                predictiveBackProgress = 0f
            }
        } catch (e: CancellationException) {
            predictiveBackProgress = 0f
        }
    }
}

@Composable
fun Scrim(
    color: Color,
    targetValue: Float
) {
    if (color.isSpecified) {

        Canvas(
            Modifier.fillMaxSize()
        ) {
            drawRect(color = color, alpha = targetValue)
        }
    }
}