package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.appTheme
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.ThemeMode
import com.danilkinkin.buckwheat.ui.switchTheme
import kotlinx.coroutines.launch

@Composable
fun ThemeSwitcher() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun handleSwitchTheme(mode: ThemeMode) {
        coroutineScope.launch {
            switchTheme(context, mode)
        }
    }

    Column {
        TextRow(
            icon = painterResource(R.drawable.ic_dark_mode),
            text = stringResource(R.string.theme_label),
        )
        CheckedRow(
            checked = context.appTheme == ThemeMode.LIGHT,
            onValueChange = { handleSwitchTheme(ThemeMode.LIGHT) },
            text = stringResource(R.string.theme_light),
        )
        CheckedRow(
            checked = context.appTheme == ThemeMode.NIGHT,
            onValueChange = { handleSwitchTheme(ThemeMode.NIGHT) },
            text = stringResource(R.string.theme_dark),
        )
        CheckedRow(
            checked = context.appTheme == ThemeMode.SYSTEM,
            onValueChange = { handleSwitchTheme(ThemeMode.SYSTEM) },
            text = stringResource(R.string.theme_system),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Surface {
            ThemeSwitcher()
        }
    }
}