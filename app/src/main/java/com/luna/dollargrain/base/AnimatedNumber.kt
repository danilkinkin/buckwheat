package com.luna.dollargrain.base

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import java.text.BreakIterator

data class CharState(val preview: String, val current: String)

@Composable
fun AnimatedNumber(
    value: String = "",
    style: TextStyle = MaterialTheme.typography.displayLarge,
) {

    var previewsValue by remember { mutableStateOf<List<String>>(emptyList()) }
    var blocks by remember { mutableStateOf<List<CharState>>(emptyList()) }

    DisposableEffect(value) {
        var splittedValue = emptyList<String>().toMutableList()
        val it = BreakIterator.getCharacterInstance()
        it.setText(value)
        var count = 0

        var start = 0
        var end = it.next()
        while (end != BreakIterator.DONE) {
            splittedValue.add(value.substring(start, end))

            start = end
            end = it.next()
            count++
        }

        val length = splittedValue.size.coerceAtLeast(previewsValue.size)

        var newBlocks: MutableList<CharState> = emptyList<CharState>().toMutableList()

        for (i in 0 .. length) {
            newBlocks.add(
                CharState(
                    preview = previewsValue.getOrElse(previewsValue.size - i) { " " },
                    current = splittedValue.getOrElse(splittedValue.size - i) { " " },
                )
            )
        }

        newBlocks = newBlocks.asReversed()

        blocks = newBlocks
        previewsValue = splittedValue

        onDispose {  }
    }

    Row {
        blocks.forEach {
            AnimatedContent(
                targetState = it.current,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically(tween(durationMillis = 200)) { height -> height } + fadeIn(tween(durationMillis = 200))).togetherWith(
                            slideOutVertically(tween(durationMillis = 200)) { height -> -height } + fadeOut(
                                tween(durationMillis = 200)
                            ))
                    } else {
                        (slideInVertically(tween(durationMillis = 200)) { height -> -height } + fadeIn(tween(durationMillis = 200))).togetherWith(
                            slideOutVertically(tween(durationMillis = 200)) { height -> height } + fadeOut(
                                tween(durationMillis = 200)
                            ))
                    }.using(
                        SizeTransform(clip = false)
                    )
                }, label = ""
            ) { targetCount ->
                Text(text = targetCount, style = style)
            }
        }
    }
}