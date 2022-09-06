package com.danilkinkin.buckwheat.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.copyLinkToClipboard

@Composable
fun About(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
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

@Composable
fun ButtonWithIcon(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.fillMaxWidth().weight(1F))
        Icon(
            painter = icon,
            contentDescription = null,
        )
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        About()
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        About()
    }
}