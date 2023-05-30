package com.danilkinkin.buckwheat.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danilkinkin.buckwheat.appTheme
import com.danilkinkin.buckwheat.dataStore
import com.danilkinkin.buckwheat.ui.harmonize.palettes.CorePalette
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


enum class ThemeMode { LIGHT, NIGHT, SYSTEM }

fun darkColorScheme(): ColorScheme {
    val palette = CorePalette.contentOf(colorSeed.toArgb())
    
    return darkColorScheme(
        primary = Color(palette.a1.tone(80)),
        onPrimary = Color(palette.a1.tone(20)),
        primaryContainer = Color(palette.a1.tone(30)),
        onPrimaryContainer = Color(palette.a1.tone(90)),
        inversePrimary = Color(palette.a1.tone(40)),
        secondary = Color(palette.a2.tone(80)),
        onSecondary = Color(palette.a2.tone(20)),
        secondaryContainer = Color(palette.a2.tone(30)),
        onSecondaryContainer = Color(palette.a2.tone(90)),
        tertiary = Color(palette.a3.tone(80)),
        onTertiary = Color(palette.a3.tone(20)),
        tertiaryContainer = Color(palette.a3.tone(30)),
        onTertiaryContainer = Color(palette.a3.tone(90)),
        background = Color(palette.n1.tone(10)),
        onBackground = Color(palette.n1.tone(90)),
        surface = Color(palette.n1.tone(10)),
        onSurface = Color(palette.n1.tone(90)),
        surfaceVariant = Color(palette.n1.tone(30)),
        onSurfaceVariant = Color(palette.n1.tone(80)),
        surfaceTint = Color(palette.n1.tone(90)), //
        inverseSurface = Color(palette.n1.tone(90)),
        inverseOnSurface = Color(palette.n1.tone(20)),
        error = Color(palette.error.tone(80)),
        onError = Color(palette.error.tone(20)),
        errorContainer = Color(palette.error.tone(30)),
        onErrorContainer = Color(palette.error.tone(80)),
        outline = Color(palette.n2.tone(60)),
        outlineVariant = Color(palette.n2.tone(50)), //
        scrim = Color(palette.n1.tone(30)), //
    )
}

fun lightColorScheme(): ColorScheme {
    val palette = CorePalette.contentOf(colorSeed.toArgb())

    return lightColorScheme(
        primary = Color(palette.a1.tone(40)),
        onPrimary = Color(palette.a1.tone(100)),
        primaryContainer = Color(palette.a1.tone(90)),
        onPrimaryContainer = Color(palette.a1.tone(10)),
        inversePrimary = Color(palette.a1.tone(80)),
        secondary = Color(palette.a2.tone(40)),
        onSecondary = Color(palette.a2.tone(100)),
        secondaryContainer = Color(palette.a2.tone(90)),
        onSecondaryContainer = Color(palette.a2.tone(10)),
        tertiary = Color(palette.a3.tone(40)),
        onTertiary = Color(palette.a3.tone(100)),
        tertiaryContainer = Color(palette.a3.tone(90)),
        onTertiaryContainer = Color(palette.a3.tone(10)),
        background = Color(palette.n1.tone(99)),
        onBackground = Color(palette.n1.tone(10)),
        surface = Color(palette.n1.tone(99)),
        onSurface = Color(palette.n1.tone(10)),
        surfaceVariant = Color(palette.n1.tone(90)),
        onSurfaceVariant = Color(palette.n1.tone(30)),
        surfaceTint = Color(palette.n1.tone(10)), //
        inverseSurface = Color(palette.n1.tone(20)),
        inverseOnSurface = Color(palette.n1.tone(95)),
        error = Color(palette.error.tone(40)),
        onError = Color(palette.error.tone(100)),
        errorContainer = Color(palette.error.tone(90)),
        onErrorContainer = Color(palette.error.tone(10)),
        outline = Color(palette.n2.tone(50)),
        outlineVariant = Color(palette.n2.tone(50)), //
        scrim = Color(palette.n1.tone(90)), //
    )
}


@Composable
fun isNightMode(): Boolean = when (LocalContext.current.appTheme) {
    ThemeMode.LIGHT -> false
    ThemeMode.NIGHT -> true
    else -> isSystemInDarkTheme()
}


@Composable
fun BuckwheatTheme(
    darkTheme: Boolean = isNightMode(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography,
        content = content
    )
}

suspend fun switchTheme(context: Context, mode: ThemeMode) {
    context.dataStore.edit {
        it[stringPreferencesKey("theme")] = mode.toString()
    }

    context.appTheme = mode
}

fun syncTheme(context: Context) {
    val currentValue = runBlocking { context.dataStore.data.first() }

    val mode = ThemeMode.valueOf(
        currentValue[stringPreferencesKey("theme")] ?: ThemeMode.SYSTEM.toString()
    )

    context.appTheme = mode
}