package com.danilkinkin.buckwheat.home

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.ThemeMode
import com.danilkinkin.buckwheat.ui.syncTheme

val Context.dataStore by preferencesDataStore("settings")
var Context.appTheme by mutableStateOf(ThemeMode.SYSTEM)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val isDone: MutableState<Boolean> = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !isDone.value }

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                syncTheme(context)

                isDone.value = true
            }

            BuckwheatTheme {
                //Colors()
                //FinishPeriod()
                MainScreen()
            }
        }
    }
}

