package com.danilkinkin.buckwheat.widget

import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProvider
import androidx.glance.color.colorProviders

@Composable
fun BuckwheatWidgetTheme(
    content: @Composable () -> Unit,
) {
    val lightScheme = dynamicLightColorScheme(LocalContext.current)
    val darkScheme = dynamicDarkColorScheme(LocalContext.current)

    GlanceTheme(
        colors = colorProviders(
            primary = ColorProvider(lightScheme.primary, darkScheme.primary),
            onPrimary = ColorProvider(lightScheme.onPrimary, darkScheme.onPrimary),
            primaryContainer = ColorProvider(lightScheme.primaryContainer, darkScheme.primaryContainer),
            onPrimaryContainer = ColorProvider(lightScheme.onPrimaryContainer, darkScheme.onPrimaryContainer),
            secondary = ColorProvider(lightScheme.secondary, darkScheme.secondary),
            onSecondary = ColorProvider(lightScheme.onSecondary, darkScheme.onSecondary),
            secondaryContainer = ColorProvider(lightScheme.secondaryContainer, darkScheme.secondaryContainer),
            onSecondaryContainer = ColorProvider(lightScheme.onSecondaryContainer, darkScheme.onSecondaryContainer),
            tertiary = ColorProvider(lightScheme.tertiary, darkScheme.tertiary),
            onTertiary = ColorProvider(lightScheme.onTertiary, darkScheme.onTertiary),
            tertiaryContainer = ColorProvider(lightScheme.tertiaryContainer, darkScheme.tertiaryContainer),
            onTertiaryContainer = ColorProvider(lightScheme.onTertiaryContainer, darkScheme.onTertiaryContainer),
            error = ColorProvider(lightScheme.error, darkScheme.error),
            errorContainer = ColorProvider(lightScheme.errorContainer, darkScheme.errorContainer),
            onError = ColorProvider(lightScheme.onError, darkScheme.onError),
            onErrorContainer = ColorProvider(lightScheme.onErrorContainer, darkScheme.onErrorContainer),
            background = ColorProvider(lightScheme.background, darkScheme.background),
            onBackground = ColorProvider(lightScheme.onBackground, darkScheme.onBackground),
            surface = ColorProvider(lightScheme.surface, darkScheme.surface),
            onSurface = ColorProvider(lightScheme.onSurface, darkScheme.onSurface),
            surfaceVariant = ColorProvider(lightScheme.surfaceVariant, darkScheme.surfaceVariant),
            onSurfaceVariant = ColorProvider(lightScheme.onSurfaceVariant, darkScheme.onSurfaceVariant),
            outline = ColorProvider(lightScheme.outline, darkScheme.outline),
            inverseOnSurface = ColorProvider(lightScheme.inverseOnSurface, darkScheme.inverseOnSurface),
            inverseSurface = ColorProvider(lightScheme.inverseSurface, darkScheme.inverseSurface),
            inversePrimary = ColorProvider(lightScheme.inversePrimary, darkScheme.inversePrimary),
        ),
        content = content
    )
}