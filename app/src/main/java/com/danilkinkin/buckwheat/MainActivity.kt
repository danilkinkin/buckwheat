package com.danilkinkin.buckwheat

import OverrideLocalize
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.danilkinkin.buckwheat.base.balloon.BalloonProvider
import com.danilkinkin.buckwheat.home.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.ThemeMode
import com.danilkinkin.buckwheat.ui.syncTheme
import com.danilkinkin.buckwheat.util.locScreenOrientation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import syncOverrideLocale
import java.util.*

val Context.budgetDataStore by preferencesDataStore("budget")
val Context.settingsDataStore by preferencesDataStore("settings")
var Context.appTheme by mutableStateOf(ThemeMode.SYSTEM)
var Context.appLocale: Locale? by mutableStateOf(null)
var Context.systemLocale: Locale? by mutableStateOf(null)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val isDone: MutableState<Boolean> = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this.applicationContext
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen().setKeepOnScreenCondition { !isDone.value }
        lifecycleScope.launch {
            context.settingsDataStore.data.first()
        }

        super.onCreate(savedInstanceState)

        setContent {
            val localContext = LocalContext.current
            val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current

            LaunchedEffect(Unit) {
                syncTheme(localContext)
                syncOverrideLocale(localContext)

                isDone.value = true
            }

            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                locScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }

            if (isDone.value) {
                BuckwheatTheme {
                    OverrideLocalize {
                        BalloonProvider {
                            MainScreen(widthSizeClass, activityResultRegistryOwner)
                        }
                    }
                }
            }
        }
    }
}
