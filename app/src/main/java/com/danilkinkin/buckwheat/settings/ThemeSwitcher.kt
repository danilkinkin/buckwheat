package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.appTheme
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.ThemeMode
import com.danilkinkin.buckwheat.ui.switchTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.*
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState

const val SETTINGS_CHANGE_THEME_SHEET = "settings.changeTheme"

@Composable
fun ThemeSwitcher(appViewModel: AppViewModel = hiltViewModel()) {
    val context = LocalContext.current

    ButtonRow(
        icon = painterResource(R.drawable.ic_dark_mode),
        text = stringResource(R.string.theme_label),
        onClick = {
            appViewModel.openSheet(PathState(SETTINGS_CHANGE_THEME_SHEET))
        },
        endContent = {
            Text(
                text = when (context.appTheme) {
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                    ThemeMode.NIGHT -> stringResource(R.string.theme_dark)
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = LocalContentColor.current.copy(alpha = 0.6f),
            )
        }
    )
}

@Composable
fun ThemeSwitcherDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    fun handleSwitchTheme(mode: ThemeMode) {
        coroutineScope.launch {
            switchTheme(context, mode)
            onClose()
        }
    }

    Surface {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.theme_label),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
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