package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.colorOnEditor
import java.lang.Float.max
import kotlin.math.roundToInt

@ExperimentalMaterialApi
enum class TopSheetValue {
    Expanded,
    HalfExpanded
}

@Composable
@ExperimentalMaterialApi
fun TopSheetLayout(
    modifier: Modifier = Modifier,
    swipeableState: SwipeableState<TopSheetValue> = rememberSwipeableState(TopSheetValue.HalfExpanded),
    customHalfHeight: Float? = null,
    lockSwipeable: MutableState<Boolean>,
    sheetContentHalfExpand: @Composable () -> Unit,
    sheetContentExpand: @Composable () -> Unit,
) {
    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    var lock by remember { mutableStateOf(false) }
    var scroll by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        val fullHeight = constraints.maxHeight.toFloat()
        val halfHeight = customHalfHeight ?: (fullHeight / 2)
        val expandHeight = with(LocalDensity.current) { (fullHeight - navigationBarHeight.toPx() - 16.dp.toPx()) }
        val currOffset = swipeableState.offset.value
        val maxOffset = -(expandHeight - halfHeight)
        val progress = (1f - (currOffset / maxOffset)).coerceIn(0f, 1f)

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
                targetValue = (progress * 5).coerceIn(0f, 1f),
            )
        }

        androidx.compose.material3.Card(
            shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorEditor,
                contentColor = colorOnEditor,
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { (fullHeight - navigationBarHeight.toPx() - 16.dp.toPx()).toDp() })
                .nestedScroll(connection)
                .offset {
                    IntOffset(
                        x = 0,
                        y = swipeableState.offset.value
                            .coerceIn(-(expandHeight - halfHeight), 0f)
                            .roundToInt(),
                    )
                }
                .swipeable(
                    state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        -(expandHeight - halfHeight) to TopSheetValue.HalfExpanded,
                        0f to TopSheetValue.Expanded
                    ),
                )

        ) {
            Box(modifier = modifier.fillMaxSize()) {
                if (progress != 0f) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .alpha(max(progress * 2f - 1f, 0f))) {
                        sheetContentExpand()
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
                                .background(colorEditor))
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
                                startY = 0f,
                                endY = (6 / 32f) * 100f,
                            )
                        )
                        .padding(bottom = 10.dp, top = 16.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        Modifier
                            .height(4.dp)
                            .width(50.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .align(Alignment.Center)
                    )
                }
            }
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