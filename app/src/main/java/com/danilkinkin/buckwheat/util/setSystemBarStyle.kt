package com.danilkinkin.buckwheat.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun setSystemStyle(
    style: SystemBarState,
    key: Any = Unit,
    confirmChange: () -> Boolean = { true },
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(key) {
        if (!confirmChange()) {
            return@DisposableEffect onDispose {

            }
        }

        appViewModel.statusBarStack.add(style)

        systemUiController.setStatusBarColor(
            color = style.statusBarColor,
            darkIcons = style.statusBarDarkIcons,
        )
        systemUiController.setNavigationBarColor(
            color = style.navigationBarColor,
            darkIcons = style.navigationBarDarkIcons,
            navigationBarContrastEnforced = false,
        )

        onDispose {
            appViewModel.statusBarStack.removeLastOrNull()
            val systemBarState = appViewModel.statusBarStack.lastOrNull()

            if (systemBarState !== null) {
                systemUiController.setStatusBarColor(
                    color = systemBarState.statusBarColor,
                    darkIcons = systemBarState.statusBarDarkIcons,
                )
                systemUiController.setNavigationBarColor(
                    color = systemBarState.navigationBarColor,
                    darkIcons = systemBarState.navigationBarDarkIcons,
                    navigationBarContrastEnforced = false,
                )
            }
        }
    }
}