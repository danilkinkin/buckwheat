package com.danilkinkin.buckwheat.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.danilkinkin.buckwheat.MainActivity

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

private const val LOG_TAG = "LauncherIcon"

/**
 * Switches the enabled launcher alias to the preset at [presetIndex].
 *
 * The target alias is enabled before the others are disabled, so the app is never left without a
 * launcher component. DONT_KILL_APP keeps the current process (and the open theme sheet) alive.
 *
 * The fork has a different applicationId (`com.varashines.eva`) than its code namespace
 * (`com.danilkinkin.buckwheat`). ComponentName needs the applicationId as its package and the
 * manifest class name - which is expanded from the namespace - as its class, so the two parts are
 * built separately.
 */
fun applyLauncherIcon(context: Context, presetIndex: Int) {
    val selectedIndex = presetIndex.coerceIn(launcherAliases.indices)
    val packageManager = context.packageManager
    val classNamespace = MainActivity::class.java.name.substringBeforeLast('.')

    setAliasState(
        packageManager = packageManager,
        packageName = context.packageName,
        className = "$classNamespace.${launcherAliases[selectedIndex]}",
        enabled = true,
    )

    launcherAliases.forEachIndexed { index, alias ->
        if (index == selectedIndex) return@forEachIndexed

        setAliasState(
            packageManager = packageManager,
            packageName = context.packageName,
            className = "$classNamespace.$alias",
            enabled = false,
        )
    }
}

private fun setAliasState(
    packageManager: PackageManager,
    packageName: String,
    className: String,
    enabled: Boolean,
) {
    val component = ComponentName(packageName, className)
    val desired = if (enabled) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    } else {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }

    try {
        if (packageManager.getComponentEnabledSetting(component) == desired) return

        packageManager.setComponentEnabledSetting(
            component,
            desired,
            PackageManager.DONT_KILL_APP,
        )
    } catch (e: RuntimeException) {
        // Icon switching must never take down the activity lifecycle. The previous alias stays
        // enabled when this fails, so the app remains launchable.
        Log.e(LOG_TAG, "Unable to set $className enabled=$enabled", e)
    }
}
