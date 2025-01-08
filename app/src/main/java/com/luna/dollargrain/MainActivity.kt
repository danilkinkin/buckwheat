package com.luna.dollargrain

import OverrideLocalize
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.luna.dollargrain.base.balloon.BalloonProvider
import com.luna.dollargrain.data.dao.StorageDao
import com.luna.dollargrain.di.migrateToDataStore
import com.luna.dollargrain.home.MainScreen
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.ThemeMode
import com.luna.dollargrain.ui.syncTheme
import com.luna.dollargrain.util.LockScreenOrientation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import syncOverrideLocale
import java.util.Locale
import javax.inject.Inject

val Context.budgetDataStore by preferencesDataStore("budget")
val Context.settingsDataStore by preferencesDataStore("settings")
var Context.appTheme by mutableStateOf(ThemeMode.SYSTEM)
var Context.appLocale: Locale? by mutableStateOf(null)
var Context.systemLocale: Locale? by mutableStateOf(null)
var Context.errorForReport: String? by mutableStateOf(null)

val LocalWindowSize = compositionLocalOf { WindowWidthSizeClass.Compact }
val LocalWindowInsets = compositionLocalOf { PaddingValues(0.dp) }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Broke tests. Set to true for tests
    private val isDone = mutableStateOf(false)
    private val isReady = mutableStateOf(false)

    //TODO: Remove after 01.01.2024. Need for migration to DataStore
    @Inject
    lateinit var storageDao: StorageDao

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
                migrateToDataStore(context, storageDao)

                // App ready for work
                isReady.value = true
            }


            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }


            val windowInsets = WindowInsets
                .systemBars
                .asPaddingValues()


            CatchAndSendCrashReport()

            if (isReady.value) {
                DollargrainTheme {
                    OverrideLocalize {
                        BalloonProvider {
                            CompositionLocalProvider(
                                LocalWindowSize provides widthSizeClass,
                                LocalWindowInsets provides windowInsets,
                            ) {
                                MainScreen(activityResultRegistryOwner)

                                LaunchedEffect(Unit) {
                                    // App rendered and splash screen can be hidden
                                    isDone.value = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
