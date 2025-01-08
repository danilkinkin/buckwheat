package com.luna.dollargrain.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.luna.dollargrain.util.combineColors

val colorSeed = Color(0xFFCC4C08)
val colorGood = Color(0xFF40AC02)
val colorNotGood = Color(0xFFFABC20)
val colorBad = Color(0xFFC70909)
val colorMin = Color(0xFF185ED6)
val colorMax = Color(0xFFDD1414)


val colorBackground
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val colorButton
    @Composable
    @ReadOnlyComposable
    get() = combineColors(
        combineColors(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            0.76F
        ),
        MaterialTheme.colorScheme.surface,
        0.68F
    )

val colorOnButton
    @Composable
    @ReadOnlyComposable
    get() = combineColors(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.onSurface,
        0.56F
    )
val colorEditor
    @Composable
    @ReadOnlyComposable
    get() = combineColors(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
        0.56F,
    )

val colorOnEditor
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface