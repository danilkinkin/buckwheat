package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.openInBrowser
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.util.sendEmail

const val BUG_REPORTER_SHEET = "bugReporter"

@Composable
fun BugReporter(onClose: () -> Unit = {}) {
    val context = LocalContext.current
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
                    text = stringResource(R.string.report_bug_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Divider()
            ButtonRow(
                icon = painterResource(R.drawable.ic_github),
                text = stringResource(R.string.report_via_github),
                onClick = {
                    openInBrowser(
                        context,
                        "https://github.com/danilkinkin/buckwheat/issues",
                    )
                    onClose()
                },
            )
            ButtonRow(
                icon = painterResource(R.drawable.ic_email),
                text = stringResource(R.string.report_via_email),
                onClick = {
                    sendEmail(
                        context,
                        arrayOf("hello@danilkinkin.com"),
                        "Buckwheat bug report",
                    )
                    onClose()
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        BugReporter()
    }
}