package com.danilkinkin.buckwheat.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.danilkinkin.buckwheat.util.combineColors

val colorGood = Color(0xFF40AC02)
val colorNotGood = Color(0xFFFABC20)
val colorBad = Color(0xFFC70909)
val colorMin = Color(0xFF185ED6)
val colorMax = Color(0xFFDD1414)


/**
 * The page behind the cards: the preset surface nudged 4.5% toward the accent. It is still quiet,
 * but it separates the page from the cards and gives each preset a visible atmosphere.
 */
val colorBackground
    @Composable
    @ReadOnlyComposable
    get() = lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primary,
        0.045f,
    )

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
    get() = MaterialTheme.colorScheme.surfaceContainerHigh

val colorOnEditor
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface
