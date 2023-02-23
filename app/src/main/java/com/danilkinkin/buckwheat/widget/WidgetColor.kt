package com.danilkinkin.buckwheat.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.glance.LocalContext
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.harmonize
import com.danilkinkin.buckwheat.util.toPalette
import androidx.glance.appwidget.unit.ColorProvider

val Context.isNightMode: Boolean
    get() =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

data class HarmonizedWidgetColorProviderPalette(
    val main: ColorProvider,
    val onMain: ColorProvider,
    val container: ColorProvider,
    val onContainer: ColorProvider,
    val surface: ColorProvider,
    val onSurface: ColorProvider,
    val surfaceVariant: ColorProvider,
    val onSurfaceVariant: ColorProvider,
)

@Composable
fun generateWidgetColorPalette(): HarmonizedWidgetColorProviderPalette {
    val context = LocalContext.current

    val harmonizedColor = harmonize(
        combineColors(
            listOf(
                colorBad,
                colorNotGood,
                colorGood,
            ),
            0.3f,
        ),
        Color(ContextCompat.getColor(context, R.color.material_dynamic_primary40))
    )

    val dayPalette = toPalette(harmonizedColor, darkTheme = false)
    val nightPalette = toPalette(harmonizedColor, darkTheme = true)

    return HarmonizedWidgetColorProviderPalette(
        main = ColorProvider(dayPalette.main, nightPalette.main),
        onMain = ColorProvider(
            dayPalette.onMain,
            nightPalette.onMain
        ),
        container = ColorProvider(
            dayPalette.container,
            nightPalette.container
        ),
        onContainer = ColorProvider(
            dayPalette.onContainer,
            nightPalette.onContainer
        ),
        surface = ColorProvider(
            dayPalette.surface,
            nightPalette.surface
        ),
        onSurface = ColorProvider(
            dayPalette.onSurface,
            nightPalette.onSurface
        ),
        surfaceVariant = ColorProvider(
            dayPalette.surfaceVariant,
            nightPalette.surfaceVariant
        ),
        onSurfaceVariant = ColorProvider(
            dayPalette.onSurfaceVariant,
            nightPalette.onSurfaceVariant
        ),
    )
}