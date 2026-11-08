package com.danilkinkin.buckwheat.ui

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danilkinkin.buckwheat.appTheme
import com.danilkinkin.buckwheat.appThemePreset
import com.danilkinkin.buckwheat.settingsDataStore
import kotlinx.coroutines.flow.first

enum class ThemeMode { LIGHT, NIGHT, SYSTEM }

private val themeStoreKey = stringPreferencesKey("theme")
private val themePresetStoreKey = intPreferencesKey("themePreset")

@Composable
fun isNightMode(): Boolean = when (LocalContext.current.appTheme) {
    ThemeMode.LIGHT -> false
    ThemeMode.NIGHT -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun BuckwheatTheme(
    darkTheme: Boolean = isNightMode(),
    presetIndex: Int = LocalContext.current.appThemePreset,
    content: @Composable () -> Unit,
) {
    val colorScheme = createColorSchemeForPreset(
        preset = ThemePresets.getPreset(presetIndex),
        isDark = darkTheme,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography(LocalContext.current),
        content = content,
    )
}

suspend fun switchTheme(context: Context, mode: ThemeMode) {
    context.settingsDataStore.edit {
        it[themeStoreKey] = mode.toString()
    }

    context.appTheme = mode
}

suspend fun switchThemePreset(context: Context, index: Int) {
    context.settingsDataStore.edit {
        it[themePresetStoreKey] = index
    }

    context.appThemePreset = index
}

suspend fun syncTheme(context: Context) {
    val currentValue = context.settingsDataStore.data.first()

    val mode = ThemeMode.valueOf(
        currentValue[themeStoreKey] ?: ThemeMode.SYSTEM.toString()
    )

    context.appTheme = mode
    context.appThemePreset = currentValue[themePresetStoreKey] ?: 0
}
