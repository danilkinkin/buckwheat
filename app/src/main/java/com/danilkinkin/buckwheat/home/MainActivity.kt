package com.danilkinkin.buckwheat.home

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

val Context.dataStore by preferencesDataStore("settings")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val spendsViewModel: SpendsViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val systemUiController = rememberSystemUiController()

            DisposableEffect(systemUiController) {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = false,
                    isNavigationBarContrastEnforced = false,
                )

                onDispose {}
            }

            BuckwheatTheme {
                MainScreen()
            }
        }
    }
}

