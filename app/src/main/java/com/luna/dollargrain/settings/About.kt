package com.luna.dollargrain.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.base.ClickableText
import com.luna.dollargrain.base.DescriptionButton
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.openInBrowser

@Composable
fun About(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        val contentColor = LocalContentColor.current

        Column(Modifier.padding(16.dp)) {
            Text(
                text = "about app",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "dollargrain is my personal fork of buckwheat by danilkinkin with a slightly tweaked ui and functions. It was made for high seas by hack club :3",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("development n design: natalie luna")

            val devwebsite = buildAnnotatedString {
                pushStringAnnotation(
                    tag = "developer",
                    annotation = "https://luna.hackclub.app",
                )
                withStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.primary)
                ) {
                    append("website")
                }

                appendInlineContent("openInBrowser")

                pop()
            }

            val devgithub = buildAnnotatedString {
                pushStringAnnotation(
                    tag = "developer",
                    annotation = "https://luna.hackclub.app",
                )
                withStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.primary)
                ) {
                    append("website")
                }

                appendInlineContent("openInBrowser")

                pop()
            }

            Row {
                ClickableText(
                    text = devwebsite,
                    inlineContent = mapOf(
                        "openInBrowser" to InlineTextContent(
                            Placeholder(
                                MaterialTheme.typography.bodyLarge.fontSize,
                                MaterialTheme.typography.bodyLarge.fontSize,
                                PlaceholderVerticalAlign.TextCenter,
                            )
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                painter = painterResource(R.drawable.ic_open_in_browser_small),
                                contentDescription = null,
                            )
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { offset ->
                        devwebsite.getStringAnnotations(
                            tag = "developer",
                            start = offset,
                            end = offset,
                        ).firstOrNull()?.let {
                            openInBrowser(
                                context,
                                "https://luna.hackclub.app",
                            )
                        }

                    },
                )

                Spacer(modifier = Modifier.width(24.dp))

                ClickableText(
                    text = devgithub,
                    inlineContent = mapOf(
                        "openInBrowser" to InlineTextContent(
                            Placeholder(
                                MaterialTheme.typography.bodyLarge.fontSize,
                                MaterialTheme.typography.bodyLarge.fontSize,
                                PlaceholderVerticalAlign.TextCenter,
                            )
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                painter = painterResource(R.drawable.ic_open_in_browser_small),
                                contentDescription = null,
                            )
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { offset ->
                        devwebsite.getStringAnnotations(
                            tag = "developer",
                            start = offset,
                            end = offset,
                        ).firstOrNull()?.let {
                            openInBrowser(
                                context,
                                "https://luna.hackclub.app",
                            )
                        }

                    },
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            DescriptionButton(
                title = { Text("contribute (to danilkinkin)") },
                icon = painterResource(R.drawable.ic_contribute),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                    end = 12.dp,
                ),
                onClick = {
                    val currentLocale = configuration.locales[0].language

                    openInBrowser(
                        context,
                        "https://buckwheat.app/contribute"
                    )
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            DescriptionButton(
                title = { Text("report a bug!!") },
                icon = painterResource(R.drawable.ic_bug_report),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                    end = 12.dp,
                ),
                onClick = {
                    appViewModel.openSheet(PathState(BUG_REPORTER_SHEET))
                },
            )
        }
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        About()
    }
}