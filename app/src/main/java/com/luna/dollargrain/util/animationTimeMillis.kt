package com.luna.dollargrain.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis

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