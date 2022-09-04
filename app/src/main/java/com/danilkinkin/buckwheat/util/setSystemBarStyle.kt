package com.danilkinkin.buckwheat.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun setSystemStyle(
    style: () -> SystemBarState,
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

        val computeStyle = style()

        Log.d("setSystemStyle", "set statusBarDarkIcons = ${computeStyle.statusBarDarkIcons}")

        appViewModel.statusBarStack.add(style)

        systemUiController.setStatusBarColor(
            color = computeStyle.statusBarColor,
            darkIcons = computeStyle.statusBarDarkIcons,
        )
        systemUiController.setNavigationBarColor(
            color = computeStyle.navigationBarColor,
            darkIcons = computeStyle.navigationBarDarkIcons,
            navigationBarContrastEnforced = false,
        )

        onDispose {
            appViewModel.statusBarStack.removeLastOrNull()
            val systemBarState = appViewModel.statusBarStack.lastOrNull()

            if (systemBarState !== null) {
                val computeBackStyle = systemBarState()

                Log.d("setSystemStyle", "dispose statusBarDarkIcons = ${computeBackStyle.statusBarDarkIcons}")

                systemUiController.setStatusBarColor(
                    color = computeBackStyle.statusBarColor,
                    darkIcons = computeBackStyle.statusBarDarkIcons,
                )
                systemUiController.setNavigationBarColor(
                    color = computeBackStyle.navigationBarColor,
                    darkIcons = computeBackStyle.navigationBarDarkIcons,
                    navigationBarContrastEnforced = false,
                )
            }


        }
    }
}