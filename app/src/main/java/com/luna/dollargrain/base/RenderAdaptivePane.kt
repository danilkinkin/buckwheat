package com.luna.dollargrain.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.LocalWindowSize

@Composable
fun RenderAdaptivePane(
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    if (LocalWindowSize.current == WindowWidthSizeClass.Compact) {
        content()
    } else {
        Row {
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = contentAlignment,
            ) {
                content()
            }
        }
    }
}