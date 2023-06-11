package com.danilkinkin.buckwheat.widget

import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceComposable
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProvider
import androidx.glance.color.colorProviders
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.util.combineColors

@Composable
fun BuckwheatWidgetTheme(
    content: @Composable () -> Unit,
) {
    val lightScheme = dynamicLightColorScheme(LocalContext.current)
    val darkScheme = dynamicDarkColorScheme(LocalContext.current)

    CompositionLocalProvider(
        LocalBuckwheatGlanceColors provides ColorScheme(
            primary = DayNightColorProvider(lightScheme.primary, darkScheme.primary),
            onPrimary = DayNightColorProvider(lightScheme.onPrimary, darkScheme.onPrimary),
            primaryContainer = DayNightColorProvider(
                lightScheme.primaryContainer,
                darkScheme.primaryContainer
            ),
            onPrimaryContainer = DayNightColorProvider(
                lightScheme.onPrimaryContainer,
                darkScheme.onPrimaryContainer
            ),
            secondary = DayNightColorProvider(lightScheme.secondary, darkScheme.secondary),
            onSecondary = DayNightColorProvider(lightScheme.onSecondary, darkScheme.onSecondary),
            secondaryContainer = DayNightColorProvider(
                lightScheme.secondaryContainer,
                darkScheme.secondaryContainer
            ),
            onSecondaryContainer = DayNightColorProvider(
                lightScheme.onSecondaryContainer,
                darkScheme.onSecondaryContainer
            ),
            tertiary = DayNightColorProvider(lightScheme.tertiary, darkScheme.tertiary),
            onTertiary = DayNightColorProvider(lightScheme.onTertiary, darkScheme.onTertiary),
            tertiaryContainer = DayNightColorProvider(
                lightScheme.tertiaryContainer,
                darkScheme.tertiaryContainer
            ),
            onTertiaryContainer = DayNightColorProvider(
                lightScheme.onTertiaryContainer,
                darkScheme.onTertiaryContainer
            ),
            error = DayNightColorProvider(lightScheme.error, darkScheme.error),
            errorContainer = DayNightColorProvider(
                lightScheme.errorContainer,
                darkScheme.errorContainer
            ),
            onError = DayNightColorProvider(lightScheme.onError, darkScheme.onError),
            onErrorContainer = DayNightColorProvider(
                lightScheme.onErrorContainer,
                darkScheme.onErrorContainer
            ),
            background = DayNightColorProvider(lightScheme.background, darkScheme.background),
            onBackground = DayNightColorProvider(lightScheme.onBackground, darkScheme.onBackground),
            surface = DayNightColorProvider(lightScheme.surface, darkScheme.surface),
            onSurface = DayNightColorProvider(lightScheme.onSurface, darkScheme.onSurface),
            surfaceVariant = DayNightColorProvider(
                lightScheme.surfaceVariant,
                darkScheme.surfaceVariant
            ),
            onSurfaceVariant = DayNightColorProvider(
                lightScheme.onSurfaceVariant,
                darkScheme.onSurfaceVariant
            ),
            outline = DayNightColorProvider(lightScheme.outline, darkScheme.outline),
            inverseOnSurface = DayNightColorProvider(
                lightScheme.inverseOnSurface,
                darkScheme.inverseOnSurface
            ),
            inverseSurface = DayNightColorProvider(
                lightScheme.inverseSurface,
                darkScheme.inverseSurface
            ),
            inversePrimary = DayNightColorProvider(
                lightScheme.inversePrimary,
                darkScheme.inversePrimary
            ),
        ),
        content = {
            GlanceTheme(
                colors = colorProviders(
                    primary = BuckwheatGlanceTheme.colors.primary.colorProvider,
                    onPrimary = BuckwheatGlanceTheme.colors.onPrimary.colorProvider,
                    primaryContainer = BuckwheatGlanceTheme.colors.primaryContainer.colorProvider,
                    onPrimaryContainer = BuckwheatGlanceTheme.colors.onPrimaryContainer.colorProvider,
                    secondary = BuckwheatGlanceTheme.colors.secondary.colorProvider,
                    onSecondary = BuckwheatGlanceTheme.colors.onSecondary.colorProvider,
                    secondaryContainer = BuckwheatGlanceTheme.colors.secondaryContainer.colorProvider,
                    onSecondaryContainer = BuckwheatGlanceTheme.colors.onSecondaryContainer.colorProvider,
                    tertiary = BuckwheatGlanceTheme.colors.tertiary.colorProvider,
                    onTertiary = BuckwheatGlanceTheme.colors.onTertiary.colorProvider,
                    tertiaryContainer = BuckwheatGlanceTheme.colors.tertiaryContainer.colorProvider,
                    onTertiaryContainer = BuckwheatGlanceTheme.colors.onTertiaryContainer.colorProvider,
                    error = BuckwheatGlanceTheme.colors.error.colorProvider,
                    errorContainer = BuckwheatGlanceTheme.colors.errorContainer.colorProvider,
                    onError = BuckwheatGlanceTheme.colors.onError.colorProvider,
                    onErrorContainer = BuckwheatGlanceTheme.colors.onErrorContainer.colorProvider,
                    background = BuckwheatGlanceTheme.colors.background.colorProvider,
                    onBackground = BuckwheatGlanceTheme.colors.onBackground.colorProvider,
                    surface = BuckwheatGlanceTheme.colors.surface.colorProvider,
                    onSurface = BuckwheatGlanceTheme.colors.onSurface.colorProvider,
                    surfaceVariant = BuckwheatGlanceTheme.colors.surfaceVariant.colorProvider,
                    onSurfaceVariant = BuckwheatGlanceTheme.colors.onSurfaceVariant.colorProvider,
                    outline = BuckwheatGlanceTheme.colors.outline.colorProvider,
                    inverseOnSurface = BuckwheatGlanceTheme.colors.inverseOnSurface.colorProvider,
                    inverseSurface = BuckwheatGlanceTheme.colors.inverseSurface.colorProvider,
                    inversePrimary = BuckwheatGlanceTheme.colors.inversePrimary.colorProvider,
                ),
                content = content
            )
        }
    )
}

data class DayNightColorProvider(val day: Color, val night: Color) {
    val colorProvider = ColorProvider(day, night)
    fun getDayColor() = day
    fun getNightColor() = night
}

object BuckwheatGlanceTheme {
    val colors: ColorScheme
        @GlanceComposable @Composable
        @ReadOnlyComposable
        get() = LocalBuckwheatGlanceColors.current
}

@Composable
fun DayNightColorProvider.alpha(
    backdropColor: DayNightColorProvider,
    alpha: Float = 0.5F
): ColorProvider {
    return ColorProvider(
        day = combineColors(this.getDayColor(), backdropColor.getDayColor(), 1F - alpha),
        night = combineColors(this.getNightColor(), backdropColor.getNightColor(), 1F - alpha),
    )
}

internal val LocalBuckwheatGlanceColors: ProvidableCompositionLocal<ColorScheme> =
    staticCompositionLocalOf { error("No ColorScheme provided") }

data class ColorScheme(
    val primary: DayNightColorProvider,
    val onPrimary: DayNightColorProvider,
    val primaryContainer: DayNightColorProvider,
    val onPrimaryContainer: DayNightColorProvider,
    val secondary: DayNightColorProvider,
    val onSecondary: DayNightColorProvider,
    val secondaryContainer: DayNightColorProvider,
    val onSecondaryContainer: DayNightColorProvider,
    val tertiary: DayNightColorProvider,
    val onTertiary: DayNightColorProvider,
    val tertiaryContainer: DayNightColorProvider,
    val onTertiaryContainer: DayNightColorProvider,
    val error: DayNightColorProvider,
    val errorContainer: DayNightColorProvider,
    val onError: DayNightColorProvider,
    val onErrorContainer: DayNightColorProvider,
    val background: DayNightColorProvider,
    val onBackground: DayNightColorProvider,
    val surface: DayNightColorProvider,
    val onSurface: DayNightColorProvider,
    val surfaceVariant: DayNightColorProvider,
    val onSurfaceVariant: DayNightColorProvider,
    val outline: DayNightColorProvider,
    val inverseOnSurface: DayNightColorProvider,
    val inverseSurface: DayNightColorProvider,
    val inversePrimary: DayNightColorProvider,
)