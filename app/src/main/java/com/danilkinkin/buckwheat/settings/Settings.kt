package com.danilkinkin.buckwheat.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.danilkinkin.buckwheat.base.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.BuildConfig
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.data.ThemeMode
import com.danilkinkin.buckwheat.data.ThemeViewModel
import com.danilkinkin.buckwheat.home.dataStore
import com.danilkinkin.buckwheat.ui.BuckwheatTheme


@Composable
fun Settings(onClose: () -> Unit = {}) {
    val context = LocalContext.current

    val viewModel = remember {
        ThemeViewModel(context.dataStore)
    }

    val theme = viewModel.state.observeAsState().value

    fun switchTheme(mode: ThemeMode) {
        viewModel.changeThemeMode(mode)
    }

    LaunchedEffect(viewModel) {
        viewModel.request()
    }

    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    Surface {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Divider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TextRow(
                    icon = painterResource(R.drawable.ic_dark_mode),
                    text = stringResource(R.string.theme_label),
                )
                CheckedRow(
                    checked = theme == ThemeMode.LIGHT,
                    onValueChange = { switchTheme(ThemeMode.LIGHT) },
                    text = stringResource(R.string.theme_light),
                )
                CheckedRow(
                    checked = theme == ThemeMode.NIGHT,
                    onValueChange = { switchTheme(ThemeMode.NIGHT) },
                    text = stringResource(R.string.theme_dark),
                )
                CheckedRow(
                    checked = theme == ThemeMode.SYSTEM,
                    onValueChange = { switchTheme(ThemeMode.SYSTEM) },
                    text = stringResource(R.string.theme_system),
                )
                Divider()
                TextRow(
                    text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                )
                About(Modifier.padding(start = 16.dp, end = 16.dp))
            }
        }
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        Settings()
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        Settings()
    }
}