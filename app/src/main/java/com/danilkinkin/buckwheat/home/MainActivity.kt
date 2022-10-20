package com.danilkinkin.buckwheat.home

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
    val isDone: MutableState<Boolean> = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
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

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isDone.value) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)

                        true
                    } else {
                        false
                    }
                }
            }
        )

    }
}

