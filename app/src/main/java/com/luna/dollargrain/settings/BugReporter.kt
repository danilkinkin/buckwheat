package com.luna.dollargrain.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.R
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.colorEditor
import com.luna.dollargrain.ui.colorOnEditor
import com.luna.dollargrain.util.collectEnvInfo
import com.luna.dollargrain.util.openInBrowser
import com.luna.dollargrain.util.sendEmail

const val BUG_REPORTER_SHEET = "bugReporter"

@Composable
fun BugReporter(onClose: () -> Unit = {}) {
    val context = LocalContext.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    val addYourCommentToReportHint = stringResource(R.string.add_your_comment_to_report)

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
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
            Row(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .height(IntrinsicSize.Min),
            ) {
                Button(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                    icon = painterResource(R.drawable.ic_github),
                    text = stringResource(R.string.report_via_github),
                    onClick = {
                        openInBrowser(
                            context,
                            "https://github.com/luna/buckwheat/issues",
                        )
                        onClose()
                    },
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                    icon = painterResource(R.drawable.ic_email),
                    text = stringResource(R.string.report_via_email),
                    onClick = {
                        sendEmail(
                            context,
                            arrayOf("hello@luna.com"),
                            "Buckwheat bug report",
                            """
    
    
$addYourCommentToReportHint


${collectEnvInfo(context)}
""".trimIndent(),
                        )
                        onClose()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Button(modifier: Modifier = Modifier, icon: Painter, text: String, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorEditor,
            contentColor = colorOnEditor,
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                painter = icon,
                tint = colorOnEditor,
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        BugReporter()
    }
}