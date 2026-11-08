package com.danilkinkin.buckwheat.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Launcher aliases, in the same order as [com.danilkinkin.buckwheat.ui.ThemePresets.presets].
 * Each alias points at MainActivity and carries its own adaptive icon, so the launcher entry can
 * follow the selected palette.
 */
private val launcherAliases = listOf(
    "LauncherMac",
    "LauncherTrippy",
    "LauncherChill",
    "LauncherEpic",
    "LauncherEmotional",
    "LauncherSunset",
)

/**
 * Switches the enabled launcher alias to the preset at [presetIndex].
 *
 * The target alias is enabled before the others are disabled, so the app is never left without a
 * launcher component. DONT_KILL_APP keeps the current process (and the open theme sheet) alive.
 */
fun applyLauncherIcon(context: Context, presetIndex: Int) {
    val selectedIndex = presetIndex.coerceIn(launcherAliases.indices)
    val packageManager = context.packageManager

    setAliasState(
        packageManager = packageManager,
        context = context,
        alias = launcherAliases[selectedIndex],
        enabled = true,
    )

    launcherAliases.forEachIndexed { index, alias ->
        if (index == selectedIndex) return@forEachIndexed

        setAliasState(
            packageManager = packageManager,
            context = context,
            alias = alias,
            enabled = false,
        )
    }
}

private fun setAliasState(
    packageManager: PackageManager,
    context: Context,
    alias: String,
    enabled: Boolean,
) {
    val component = ComponentName(context.packageName, "${context.packageName}.$alias")
    val desired = if (enabled) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    } else {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }

    if (packageManager.getComponentEnabledSetting(component) == desired) {
        return
    }

    packageManager.setComponentEnabledSetting(
        component,
        desired,
        PackageManager.DONT_KILL_APP,
    )
}
