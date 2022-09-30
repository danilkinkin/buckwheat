package com.danilkinkin.buckwheat.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import com.danilkinkin.buckwheat.data.ThemeViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.danilkinkin.buckwheat.data.ThemeMode
import com.danilkinkin.buckwheat.home.dataStore

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFffb59a),
    onPrimary = Color(0xFF5a1b00),
    primaryContainer = Color(0xFF802a00),
    onPrimaryContainer = Color(0xFFffdbce),
    inversePrimary = Color(0xFFFA5A00), //
    secondary = Color(0xFFf2bf43),
    onSecondary = Color(0xFF3f2e00),
    secondaryContainer = Color(0xFF5b4300),
    onSecondaryContainer = Color(0xFFffdf9b),
    tertiary = Color(0xFFb0d445),
    onTertiary = Color(0xFF283500),
    tertiaryContainer = Color(0xFF3c4d00),
    onTertiaryContainer = Color(0xFFccf15e),
    background = Color(0xFF201a18),
    onBackground = Color(0xFFede0dc),
    surface = Color(0xFF201a18),
    onSurface = Color(0xFFede0dc),
    surfaceVariant = Color(0xFF53433e),
    onSurfaceVariant = Color(0xFFd8c2bb),
    surfaceTint = Color(0xFFFA5A00),
    inverseSurface = Color(0xFF000000),
    inverseOnSurface = Color(0xFFFFFFFF),
    error = Color(0xFFffb4ab),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000a),
    onErrorContainer = Color(0xFFffdad6),
    outline = Color(0xFFa08d86),
    outlineVariant = Color(0xFFFA5A00),
    scrim = Color(0xFFFA5A00),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFa73a00),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFFffdbce),
    onPrimaryContainer = Color(0xFF370e00),
    inversePrimary = Color(0xFFFA5A00),
    secondary = Color(0xFF785a00),
    onSecondary = Color(0xFFffffff),
    secondaryContainer = Color(0xFFffdf9b),
    onSecondaryContainer = Color(0xFF251a00),
    tertiary = Color(0xFF506600),
    onTertiary = Color(0xFFffffff),
    tertiaryContainer = Color(0xFFccf15e),
    onTertiaryContainer = Color(0xFF161f00),
    background = Color(0xFFfffbff),
    onBackground = Color(0xFF201a18),
    surface = Color(0xFFfffbff),
    onSurface = Color(0xFF201a18),
    surfaceVariant = Color(0xFFf5ded6),
    onSurfaceVariant = Color(0xFF53433e),
    surfaceTint = Color(0xFFFA5A00),
    inverseSurface = Color(0xFF161616),
    inverseOnSurface = Color(0xFFFFFFFF),
    error = Color(0xFFba1a1a),
    onError = Color(0xFFffffff),
    errorContainer = Color(0xFFffdad6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF85736d),
    outlineVariant = Color(0xFFFA5A00),
    scrim = Color(0xFFFA5A00),
)



@Composable
fun isNightMode(): Boolean {
    val context = LocalContext.current
    val viewModel = remember { ThemeViewModel(context.dataStore) }
    val state = viewModel.state.observeAsState()

    LaunchedEffect(viewModel) { viewModel.request() }

    return when (state.value) {
        ThemeMode.LIGHT -> false
        ThemeMode.NIGHT -> true
        else -> isSystemInDarkTheme()
    }
}


@Composable
fun BuckwheatTheme(
    darkTheme: Boolean = isNightMode(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography,
        content = content
    )
}