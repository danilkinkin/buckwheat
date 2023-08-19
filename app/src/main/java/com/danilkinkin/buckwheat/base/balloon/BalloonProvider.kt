package com.danilkinkin.buckwheat.base.balloon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import kotlinx.coroutines.coroutineScope

suspend fun PointerInputScope.detectTapUnconsumed(
    onTap: (() -> Unit)? = null
) {
    coroutineScope {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            waitForUpOrCancellation()
            onTap?.invoke()
        }
    }
}

@Composable
fun BalloonProvider(
    appViewModel: AppViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val localDensity = LocalDensity.current

    val balloons by appViewModel.balloonController.balloons.observeAsState(initial = emptyMap())
    val showedBalloons by appViewModel.balloonController.showedBalloons.observeAsState(initial = emptySet())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapUnconsumed(onTap = {
                    balloons.forEach { balloon ->
                        appViewModel.balloonController.hide(balloon.key)
                    }
                })
            }
    ) {
        content()
    }

    balloons.entries.forEach { mapItem ->
        val balloon = mapItem.value
        val anchor = balloon.anchor
        var tooltipWidth by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    tooltipWidth = it.size.width.toFloat()
                }
                .absoluteOffset(
                    x = with(localDensity) { (anchor.x - tooltipWidth / 2f).toDp() },
                    y = with(localDensity) { anchor.y.toDp() } + 4.dp,
                ),
        ) {
            AnimatedVisibility(
                visible = showedBalloons.contains(balloon.id),
                enter = fadeIn(tween(durationMillis = 200)) + scaleIn(
                    tween(durationMillis = 200, easing = EaseOutExpo),
                    transformOrigin = TransformOrigin(0.5f, 0f)
                ),
                exit = fadeOut(tween(durationMillis = 200)) + scaleOut(
                    tween(durationMillis = 200, easing = EaseInExpo),
                    transformOrigin = TransformOrigin(0.5f, 0f)
                ),
            ) {
                Balloon(
                    modifier = Modifier
                ) {
                    balloon.content()
                }
                DisposableEffect(Unit) {
                    onDispose {
                        appViewModel.balloonController.destroy(balloon.id)
                    }
                }
            }
        }


        LaunchedEffect(Unit) {
            balloon.id.let { appViewModel.balloonController.show(it) }
        }
    }
}