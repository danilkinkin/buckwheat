package com.luna.dollargrain.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.appTheme
import com.luna.dollargrain.base.ButtonRow
import com.luna.dollargrain.base.CheckedRow
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.ThemeMode
import com.luna.dollargrain.ui.switchTheme
import kotlinx.coroutines.launch

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
        endCaption = when (context.appTheme) {
            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
            ThemeMode.NIGHT -> stringResource(R.string.theme_dark)
            ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSwitcherDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val navigationBarHeight = max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    fun handleSwitchTheme(mode: ThemeMode) {
        coroutineScope.launch {
            switchTheme(context, mode)
            onClose()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onClose()
        },
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)
    ) {
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
    DollargrainTheme {
        Surface {
            ThemeSwitcher()
        }
    }
}