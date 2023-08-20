package com.danilkinkin.buckwheat.util

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.Dp

@Composable
fun rememberNavigationBarHeight(): Dp {
    return WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
}