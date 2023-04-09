package com.danilkinkin.buckwheat.base

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle

data class CharState(val preview: String, val current: String)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedNumber(
    value: String = "",
    style: TextStyle = MaterialTheme.typography.displayLarge,
) {

    var previewsValue by remember { mutableStateOf("") }
    var blocks by remember { mutableStateOf<List<CharState>>(emptyList()) }

    DisposableEffect(value) {
        val length = value.length.coerceAtLeast(previewsValue.length)

        var newBlocks: MutableList<CharState> = emptyList<CharState>().toMutableList()

        for (i in 0 .. length) {
            newBlocks.add(
                CharState(
                    preview = previewsValue.getOrElse(previewsValue.length - i) { ' ' }.toString(),
                    current = value.getOrElse(value.length - i) { ' ' }.toString(),
                )
            )
        }

        newBlocks = newBlocks.asReversed()

        blocks = newBlocks
        previewsValue = value

        onDispose {  }
    }

    Row {
        blocks.forEach {
            AnimatedContent(
                targetState = it.current,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        slideInVertically { height -> -height } + fadeIn() with
                                slideOutVertically { height -> height } + fadeOut()
                    }.using(
                        SizeTransform(clip = false)
                    )
                }
            ) { targetCount ->
                Text(text = targetCount, style = style)
            }
        }
    }
}