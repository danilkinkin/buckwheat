package com.luna.dollargrain.history

import android.view.MotionEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.luna.dollargrain.R
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.colorEditor
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sqrt

data class SwipeActionsConfig(
    val threshold: Float,
    val icon: Painter?,
    val iconTint: Color,
    val background: Color,
    val backgroundActive: Color,
    val stayDismissed: Boolean,
    val onDismiss: () -> Unit,
)

val DefaultSwipeActionsConfig = SwipeActionsConfig(
    threshold = 0.4f,
    icon = null,
    iconTint = Color.Transparent,
    background = Color.Transparent,
    backgroundActive = Color.Transparent,
    stayDismissed = false,
    onDismiss = {},
)

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun SwipeActions(
    modifier: Modifier = Modifier,
    startActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    onTried: () -> Unit = {},
    showTutorial: Boolean = false,
    content: @Composable (DismissState) -> Unit,
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val height = constraints.maxHeight.toFloat()

    var willDismissDirection: DismissDirection? by remember {
        mutableStateOf(null)
    }

    val state = rememberDismissState(
        confirmStateChange = {
            onTried()
            if (willDismissDirection == DismissDirection.StartToEnd
                && it == DismissValue.DismissedToEnd
            ) {
                startActionsConfig.onDismiss()
                startActionsConfig.stayDismissed
            } else if (willDismissDirection == DismissDirection.EndToStart &&
                it == DismissValue.DismissedToStart
            ) {
                endActionsConfig.onDismiss()
                endActionsConfig.stayDismissed
            } else {
                false
            }
        }
    )

    var showingTutorial by remember {
        mutableStateOf(showTutorial)
    }

    if (showingTutorial) {
        val infiniteTransition = rememberInfiniteTransition()
        val x by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = width * (startActionsConfig.threshold) / 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing, delayMillis = 1000),
                repeatMode = RepeatMode.Reverse
            )
        )
        val dir by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        LaunchedEffect(key1 = x, block = {
            state.performDrag(x * (if (dir > 0.5f) 1f else -1f) - state.offset.value)
        })
    }

    LaunchedEffect(key1 = Unit, block = {
        snapshotFlow { state.offset.value }
            .collect {
                willDismissDirection = when {
                    it > width * startActionsConfig.threshold -> DismissDirection.StartToEnd
                    it < -width * endActionsConfig.threshold -> DismissDirection.EndToStart
                    else -> null
                }
            }
    })

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(key1 = willDismissDirection, block = {
        if (willDismissDirection != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    })

    val dismissDirections by remember(startActionsConfig, endActionsConfig) {
        derivedStateOf {
            mutableSetOf<DismissDirection>().apply {
                if (startActionsConfig != DefaultSwipeActionsConfig) add(DismissDirection.StartToEnd)
                if (endActionsConfig != DefaultSwipeActionsConfig) add(DismissDirection.EndToStart)
            }
        }
    }

    SwipeToDismiss(
        state = state,
        modifier = Modifier
            .pointerInteropFilter {
                if (it.action == MotionEvent.ACTION_DOWN) {
                    showingTutorial = false
                }
                false
            },
        directions = dismissDirections,
        dismissThresholds = {
            if (it == DismissDirection.StartToEnd)
                FractionalThreshold(startActionsConfig.threshold)
            else FractionalThreshold(endActionsConfig.threshold)
        },
        background = {
            AnimatedContent(
                targetState = Pair(state.dismissDirection, willDismissDirection != null),
                transitionSpec = {
                    fadeIn(
                        tween(0),
                        initialAlpha = if (targetState.second) 1f else 0f,
                    ) togetherWith fadeOut(
                        tween(0),
                        targetAlpha = if (targetState.second) .7f else 0f,
                    )
                }
            ) { (direction, willDismiss) ->
                val revealSize = remember { Animatable(if (willDismiss) 0f else 0f) }
                val iconSize = remember { Animatable(if (willDismiss) .8f else 1f) }
                LaunchedEffect(key1 = Unit, block = {
                    if (willDismiss) {
                        revealSize.snapTo(0f)
                        launch {
                            revealSize.animateTo(1f, animationSpec = tween(400))
                        }
                        iconSize.snapTo(.8f)
                        iconSize.animateTo(
                            1.25f,
                            spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                            )
                        )
                        iconSize.animateTo(
                            1f,
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                            )
                        )
                    }
                })
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = when (direction) {
                                DismissDirection.StartToEnd -> startActionsConfig.background
                                DismissDirection.EndToStart -> endActionsConfig.background
                                else -> Color.Transparent
                            },
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                CirclePath(
                                    revealSize.value,
                                    direction == DismissDirection.StartToEnd
                                )
                            )
                            .background(
                                color = when (direction) {
                                    DismissDirection.StartToEnd -> startActionsConfig.backgroundActive
                                    DismissDirection.EndToStart -> endActionsConfig.backgroundActive
                                    else -> Color.Transparent
                                },
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(
                                when (direction) {
                                    DismissDirection.StartToEnd -> Alignment.CenterStart
                                    else -> Alignment.CenterEnd
                                }
                            )
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .scale(iconSize.value),
                        contentAlignment = Alignment.Center
                    ) {
                        when (direction) {
                            DismissDirection.StartToEnd -> {
                                if (startActionsConfig.icon !== null) {
                                    Image(
                                        painter = startActionsConfig.icon,
                                        colorFilter = ColorFilter.tint(if (willDismiss) startActionsConfig.iconTint else startActionsConfig.backgroundActive),
                                        contentDescription = null
                                    )
                                }
                            }
                            DismissDirection.EndToStart -> {
                                if (endActionsConfig.icon !== null) {
                                    Image(
                                        painter = endActionsConfig.icon,
                                        colorFilter = ColorFilter.tint(if (willDismiss) endActionsConfig.iconTint else endActionsConfig.backgroundActive),
                                        contentDescription = null
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    ) {
        content(state)
    }
}


class CirclePath(private val progress: Float, private val start: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {

        val origin = Offset(
            x = if (start) size.height / 2 else size.width - size.height / 2,
            y = size.center.y,
        )

        val radius = (sqrt(
            size.height * size.height + size.width * size.width
        ) * 1f) * progress

        return Outline.Generic(
            Path().apply {
                addOval(
                    Rect(
                        center = origin,
                        radius = radius,
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(widthDp = 300)
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        SwipeActions(
            startActionsConfig = SwipeActionsConfig(
                threshold = 0.4f,
                background = MaterialTheme.colorScheme.tertiaryContainer,
                backgroundActive = MaterialTheme.colorScheme.tertiary,
                iconTint = MaterialTheme.colorScheme.onTertiary,
                icon = painterResource(R.drawable.ic_edit),
                stayDismissed = false,
                onDismiss = {

                }
            ),
            endActionsConfig = SwipeActionsConfig(
                threshold = 0.4f,
                background = MaterialTheme.colorScheme.errorContainer,
                backgroundActive = MaterialTheme.colorScheme.error,
                iconTint = MaterialTheme.colorScheme.onError,
                icon = painterResource(R.drawable.ic_delete_forever),
                stayDismissed = false,
                onDismiss = {

                }
            ),
        ) { state ->
            val size = with(LocalDensity.current) {
                java.lang.Float.max(
                    java.lang.Float.min(
                        16.dp.toPx(),
                        abs(state.offset.value)
                    ), 0f
                ).toDp()
            }

            val animateCorners by remember {
                derivedStateOf {
                    state.offset.value.absoluteValue > 30
                }
            }
            val startCorners by animateDpAsState(
                targetValue = when {
                    state.dismissDirection == DismissDirection.StartToEnd &&
                            animateCorners -> 8.dp
                    else -> 0.dp
                }
            )
            val endCorners by animateDpAsState(
                targetValue = when {
                    state.dismissDirection == DismissDirection.EndToStart &&
                            animateCorners -> 8.dp
                    else -> 0.dp
                }
            )

            Box(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            vertical = min(
                                size / 4f,
                                4.dp
                            )
                        )
                        .clip(RoundedCornerShape(size)),
                    color = colorEditor,
                    shape = RoundedCornerShape(
                        topStart = startCorners,
                        bottomStart = startCorners,
                        topEnd = endCorners,
                        bottomEnd = endCorners,
                    ),
                ) {
                }
                Box(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Swipe to dismiss",
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}