package com.danilkinkin.buckwheat.finishPeriod

import android.graphics.PointF
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.editor.calcMaxFont
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FillCircleStub(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val haptic = LocalHapticFeedback.current
    val localDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var size by remember { mutableStateOf(0.dp) }
    var confettiEjectPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }

    DisposableEffect(isPressed.value) {
        if (isPressed.value) coroutineScope.launch {
            scale.animateTo(
                1.5f,
                animationSpec = tween(
                    durationMillis = 20,
                    easing = LinearEasing
                )
            )
        } else coroutineScope.launch {
            scale.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = 120,
                    easing = LinearEasing,
                )
            )
        }

        onDispose { }
    }


    Box(
        Modifier
            .widthIn(max = 120.dp)
            .fillMaxHeight()
            .onGloballyPositioned {
                size = with(localDensity) { it.size.height.toDp() }

                confettiEjectPosition = Offset(
                    x = it.positionInWindow().x + it.size.width / 3,
                    y = it.positionInWindow().y + it.size.height / 3,
                )
            }
    ) {
        Card(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    onClick = {
                        coroutineScope.launch {
                            if (!isPressed.value) {
                                scale.animateTo(
                                    1.5f,
                                    animationSpec = tween(
                                        durationMillis = 20,
                                        easing = LinearEasing
                                    )
                                )
                            }
                            scale.animateTo(
                                1f,
                                animationSpec = tween(
                                    durationMillis = 120,
                                    easing = LinearEasing,
                                )
                            )
                        }

                        appViewModel.confettiController.spawn(
                            ejectPoint = PointF(confettiEjectPosition.x, confettiEjectPosition.y),
                            ejectVector = PointF(-60f, -100f),
                            ejectAngle = 80,
                            ejectForceCoefficient = 9f,
                            count = 30 to 60,
                        )
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                ),
            shape = CircleShape,
        ) {
            val fontSize = calcMaxFont(
                with(localDensity) {(size * 0.565685425f /* 0.8f / sqrt(2f) */).toPx()
                },
                text = "\uD83C\uDF89",
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-45f)
                    .scale(1f, scale.value)
                    .rotate(-45f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\uD83C\uDF89",
                    fontSize = fontSize
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Box(
            modifier = Modifier
                .height(200.dp)
                .width(900.dp)
        ) {
            FillCircleStub()
        }
    }
}