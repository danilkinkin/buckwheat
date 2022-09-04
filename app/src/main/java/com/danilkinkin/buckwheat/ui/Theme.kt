package com.danilkinkin.buckwheat.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import com.danilkinkin.buckwheat.data.ThemeViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.danilkinkin.buckwheat.data.ThemeMode
import com.danilkinkin.buckwheat.home.dataStore

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)



@Composable
private fun isNightMode(): Boolean {
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