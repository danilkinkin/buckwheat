package com.danilkinkin.buckwheat.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Hand-tuned palettes, ported from MediaTracker so both apps share one theme vocabulary.
 *
 * Material You derives colours from the wallpaper, which makes an app look different on every
 * device; these six presets give a stable, deliberate identity instead. The dark tokens are
 * explicit, light mode is generated from each preset's accent so the palettes stay
 * distinguishable instead of collapsing to grey.
 */
data class ThemePreset(
    val name: String,
    val accentHex: String,
    val primaryColor: Color,
    val onPrimaryColor: Color,
    val primaryContainerColor: Color,
    val onPrimaryContainerColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceContainerHighColor: Color,
    val surfaceContainerHighestColor: Color,
    val secondaryContainerColor: Color,
    val onSecondaryContainerColor: Color,
    val surfaceContainerColor: Color,
    val outlineColor: Color,
    val onSurfaceColor: Color,
    val onSurfaceVariantColor: Color,
) {
    /** The vivid identity colour, for swatches and previews (not the tonal primary). */
    val accentColor: Color
        get() = runCatching { Color(android.graphics.Color.parseColor(accentHex)) }
            .getOrDefault(primaryColor)
}

object ThemePresets {
    val presets = listOf(
        // 0: Mac (Native Azure)
        ThemePreset(
            name = "Mac",
            accentHex = "#0A84FF",
            primaryColor = Color(0xFFA1C9FF),
            onPrimaryColor = Color(0xFF00325B),
            primaryContainerColor = Color(0xFF004880),
            onPrimaryContainerColor = Color(0xFFD2E4FF),
            backgroundColor = Color(0xFF111418),
            surfaceColor = Color(0xFF16191E),
            surfaceContainerHighColor = Color(0xFF24272C),
            surfaceContainerHighestColor = Color(0xFF2F3237),
            secondaryContainerColor = Color(0xFF39465A),
            onSecondaryContainerColor = Color(0xFFD8E3F8),
            surfaceContainerColor = Color(0xFF191C21),
            outlineColor = Color(0xFF8C9199),
            onSurfaceColor = Color(0xFFE2E2E7),
            onSurfaceVariantColor = Color(0xFFC2C7CF),
        ),
        // 1: Trippy (Cyan / Teal)
        ThemePreset(
            name = "Trippy",
            accentHex = "#3E9DB0",
            primaryColor = Color(0xFF8EDBE8),
            onPrimaryColor = Color(0xFF00363F),
            primaryContainerColor = Color(0xFF004E5C),
            onPrimaryContainerColor = Color(0xFFB0ECF5),
            backgroundColor = Color(0xFF0E1416),
            surfaceColor = Color(0xFF131A1C),
            surfaceContainerHighColor = Color(0xFF20282B),
            surfaceContainerHighestColor = Color(0xFF2A3336),
            secondaryContainerColor = Color(0xFF2F4A50),
            onSecondaryContainerColor = Color(0xFFCFE9EE),
            surfaceContainerColor = Color(0xFF161D1F),
            outlineColor = Color(0xFF869396),
            onSurfaceColor = Color(0xFFDEE4E6),
            onSurfaceVariantColor = Color(0xFFBCC7CA),
        ),
        // 2: Chill (Nordic Emerald)
        ThemePreset(
            name = "Chill",
            accentHex = "#67A06B",
            primaryColor = Color(0xFF6DDFB8),
            onPrimaryColor = Color(0xFF003828),
            primaryContainerColor = Color(0xFF00513B),
            onPrimaryContainerColor = Color(0xFF8BFCD4),
            backgroundColor = Color(0xFF101513),
            surfaceColor = Color(0xFF151B18),
            surfaceContainerHighColor = Color(0xFF262D29),
            surfaceContainerHighestColor = Color(0xFF313833),
            secondaryContainerColor = Color(0xFF334B41),
            onSecondaryContainerColor = Color(0xFFBDECD8),
            surfaceContainerColor = Color(0xFF1C221E),
            outlineColor = Color(0xFF89938D),
            onSurfaceColor = Color(0xFFE0E3DF),
            onSurfaceVariantColor = Color(0xFFBFC9C2),
        ),
        // 3: Epic (Violet / Royal Purple)
        ThemePreset(
            name = "Epic",
            accentHex = "#8C66A8",
            primaryColor = Color(0xFFDCB8F4),
            onPrimaryColor = Color(0xFF3D1E4E),
            primaryContainerColor = Color(0xFF553567),
            onPrimaryContainerColor = Color(0xFFF0D7FF),
            backgroundColor = Color(0xFF141217),
            surfaceColor = Color(0xFF1A171D),
            surfaceContainerHighColor = Color(0xFF2A252D),
            surfaceContainerHighestColor = Color(0xFF352F38),
            secondaryContainerColor = Color(0xFF46324D),
            onSecondaryContainerColor = Color(0xFFEED7F3),
            surfaceContainerColor = Color(0xFF1F1B22),
            outlineColor = Color(0xFF948E9B),
            onSurfaceColor = Color(0xFFE6DFE8),
            onSurfaceVariantColor = Color(0xFFCBC2CD),
        ),
        // 4: Emotional (Rose)
        ThemePreset(
            name = "Emotional",
            accentHex = "#D06A93",
            primaryColor = Color(0xFFF4B8D4),
            onPrimaryColor = Color(0xFF4E1E38),
            primaryContainerColor = Color(0xFF67355A),
            onPrimaryContainerColor = Color(0xFFFFD7EE),
            backgroundColor = Color(0xFF161217),
            surfaceColor = Color(0xFF1C171B),
            surfaceContainerHighColor = Color(0xFF2C252B),
            surfaceContainerHighestColor = Color(0xFF372F36),
            secondaryContainerColor = Color(0xFF4B3245),
            onSecondaryContainerColor = Color(0xFFFFD7EE),
            surfaceContainerColor = Color(0xFF211B20),
            outlineColor = Color(0xFF9B8E97),
            onSurfaceColor = Color(0xFFEBDFE7),
            onSurfaceVariantColor = Color(0xFFCFC2CA),
        ),
        // 5: Sunset (Amber)
        ThemePreset(
            name = "Sunset",
            accentHex = "#E07A48",
            primaryColor = Color(0xFFFFB77C),
            onPrimaryColor = Color(0xFF4C2700),
            primaryContainerColor = Color(0xFF6C3A00),
            onPrimaryContainerColor = Color(0xFFFFDDBA),
            backgroundColor = Color(0xFF17120E),
            surfaceColor = Color(0xFF1D1713),
            surfaceContainerHighColor = Color(0xFF2E2620),
            surfaceContainerHighestColor = Color(0xFF39302B),
            secondaryContainerColor = Color(0xFF564233),
            onSecondaryContainerColor = Color(0xFFFBDECD),
            surfaceContainerColor = Color(0xFF231C17),
            outlineColor = Color(0xFF9F8D83),
            onSurfaceColor = Color(0xFFEDE0D9),
            onSurfaceVariantColor = Color(0xFFD4C3B9),
        ),
    )

    fun getPreset(index: Int): ThemePreset = presets.getOrElse(index) { presets[0] }
}

fun createColorSchemeForPreset(preset: ThemePreset, isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = preset.primaryColor,
            onPrimary = preset.onPrimaryColor,
            primaryContainer = preset.primaryContainerColor,
            onPrimaryContainer = preset.onPrimaryContainerColor,
            secondary = preset.secondaryContainerColor,
            onSecondary = preset.onSecondaryContainerColor,
            secondaryContainer = preset.secondaryContainerColor,
            onSecondaryContainer = preset.onSecondaryContainerColor,
            tertiary = Color(0xFFFFD60A),
            onTertiary = Color.Black,
            tertiaryContainer = Color(0xFF3E2D00),
            onTertiaryContainer = Color(0xFFFFE082),
            background = preset.backgroundColor,
            onBackground = preset.onSurfaceColor,
            surface = preset.surfaceColor,
            onSurface = preset.onSurfaceColor,
            surfaceVariant = preset.surfaceContainerColor,
            onSurfaceVariant = preset.onSurfaceVariantColor,
            surfaceContainer = preset.surfaceContainerColor,
            surfaceContainerHigh = preset.surfaceContainerHighColor,
            surfaceContainerHighest = preset.surfaceContainerHighestColor,
            outline = preset.outlineColor,
            outlineVariant = preset.outlineColor.copy(alpha = 0.4f),
            error = Color(0xFFFF5252),
            onError = Color.White,
        )
    } else {
        // Near-white surfaces subtly tinted toward the preset accent, so the palettes stay
        // distinguishable in light mode instead of all reading as the same grey.
        val accent = preset.accentColor
        val ink = Color(0xFF16171C)

        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = lerp(Color.White, accent, 0.18f),
            onPrimaryContainer = lerp(ink, accent, 0.35f),
            secondary = lerp(ink, accent, 0.35f),
            onSecondary = Color.White,
            secondaryContainer = lerp(Color.White, accent, 0.12f),
            onSecondaryContainer = lerp(ink, accent, 0.35f),
            tertiary = Color(0xFFB58900),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFF3CD),
            onTertiaryContainer = Color(0xFF4A3700),
            background = Color(0xFFF2F3F7),
            onBackground = ink,
            surface = Color(0xFFF2F3F7),
            onSurface = ink,
            surfaceVariant = Color(0xFFE7EAEF),
            onSurfaceVariant = lerp(ink, accent, 0.22f),
            surfaceContainer = Color(0xFFF7F8FA),
            surfaceContainerHigh = Color(0xFFFFFFFF),
            surfaceContainerHighest = Color(0xFFEAECF0),
            outline = lerp(Color(0xFF8A8F9C), accent, 0.12f),
            outlineVariant = lerp(Color(0xFFD2D6E0), accent, 0.10f),
            error = Color(0xFFD32F2F),
            onError = Color.White,
        )
    }
}
