package com.danilkinkin.buckwheat.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import com.danilkinkin.buckwheat.base.ClickableText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.DescriptionButton
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.copyLinkToClipboard

@Composable
fun About(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        val contentColor = LocalContentColor.current

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

            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = contentColor)) {
                    append("${stringResource(R.string.developer)} ")
                }

                pushStringAnnotation(
                    tag = "developer",
                    annotation = "https://danilkinkin.com",
                )
                withStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.primary)
                ) {
                    append("@danilkinkin ")
                }

                appendInlineContent("openInBrowser")

                pop()
            }

            ClickableText(
                text = annotatedString,
                inlineContent = mapOf(
                    "openInBrowser" to InlineTextContent(
                        Placeholder(
                            MaterialTheme.typography.bodyLarge.fontSize,
                            MaterialTheme.typography.bodyLarge.fontSize,
                            PlaceholderVerticalAlign.TextCenter,
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_browser),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                        )
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "developer",
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let {
                        copyLinkToClipboard(
                            context,
                            "https://danilkinkin.com",
                        )
                    }

                },
            )
            Spacer(modifier = Modifier.height(24.dp))
            DescriptionButton(
                title = { Text(stringResource(R.string.report_bug)) },
                icon = painterResource(R.drawable.ic_bug_report),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                    end = 12.dp,
                ),
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