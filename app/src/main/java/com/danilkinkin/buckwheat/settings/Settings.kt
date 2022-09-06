package com.danilkinkin.buckwheat.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import com.danilkinkin.buckwheat.base.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.data.ThemeMode
import com.danilkinkin.buckwheat.data.ThemeViewModel
import com.danilkinkin.buckwheat.home.dataStore
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.copyLinkToClipboard

@OptIn(
    ExperimentalMaterial3Api::class,
)
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

    Surface {
        Column(modifier = Modifier.navigationBarsPadding()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
            Divider()
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
            Card(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.about),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.description),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.developer),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    ButtonWithIcon(
                        title = stringResource(R.string.site),
                        icon = painterResource(R.drawable.ic_open_in_browser),
                        onClick = {
                            copyLinkToClipboard(
                                context,
                                "https://danilkinkin.com",
                            )
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ButtonWithIcon(
                        title = stringResource(R.string.report_bug),
                        icon = painterResource(R.drawable.ic_bug_report),
                        onClick = {
                            copyLinkToClipboard(
                                context,
                                "https://github.com/danilkinkin/buckweat/issues",
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ButtonWithIcon(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
){
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .weight(1F))
        Icon(
            painter = icon,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
fun PreviewSettings() {
    BuckwheatTheme {
        Settings()
    }
}