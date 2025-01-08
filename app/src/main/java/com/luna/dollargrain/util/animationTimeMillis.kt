package com.luna.dollargrain.util

import androidx.compose.runtime.*

@Composable
fun animationTimeMillis(): MutableState<Long> {
    val millisState = remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        val startTime = withFrameMillis { it }

        while (true) {
            withFrameMillis { frameTime ->
                millisState.value = frameTime - startTime
            }
        }
    }
    return millisState
}