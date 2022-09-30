package com.danilkinkin.buckwheat.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.danilkinkin.buckwheat.util.combineColors


val colorBackground
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val colorOnBackground
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val colorButton
    @Composable
    @ReadOnlyComposable
    get() = combineColors(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        0.56F
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
        0.96F,
    )

val colorOnEditor
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface