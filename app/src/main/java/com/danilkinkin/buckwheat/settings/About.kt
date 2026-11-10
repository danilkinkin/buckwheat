package com.danilkinkin.buckwheat.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.BuildConfig
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ClickableText
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.openInBrowser

private const val VARA_URL = "https://github.com/Varashines"
private const val BUCKWHEAT_URL = "https://github.com/danilkinkin/buckwheat"
private const val DANIL_URL = "https://danilkinkin.com"

@Composable
fun About(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.about_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(alpha = 0.75f),
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.12f))
            Spacer(Modifier.height(16.dp))

            val contentColor = LocalContentColor.current
            val linkColor = MaterialTheme.colorScheme.primary

            val credit = buildAnnotatedString {
                withStyle(SpanStyle(color = contentColor)) {
                    append("${stringResource(R.string.about_built_by)} ")
                }
                pushStringAnnotation(tag = "vara", annotation = VARA_URL)
                withStyle(SpanStyle(color = linkColor)) {
                    append(stringResource(R.string.about_credit_me))
                }
                pop()

                withStyle(SpanStyle(color = contentColor)) {
                    append(" ${stringResource(R.string.about_on_top_of)} ")
                }
                pushStringAnnotation(tag = "buckwheat", annotation = BUCKWHEAT_URL)
                withStyle(SpanStyle(color = linkColor)) {
                    append("Buckwheat")
                }
                pop()

                withStyle(SpanStyle(color = contentColor)) {
                    append(", ${stringResource(R.string.about_original_by)} ")
                }
                pushStringAnnotation(tag = "danil", annotation = DANIL_URL)
                withStyle(SpanStyle(color = linkColor)) {
                    append("@danilkinkin")
                }
                pop()
            }

            ClickableText(
                text = credit,
                style = MaterialTheme.typography.bodyMedium,
                onClick = { offset ->
                    credit.getStringAnnotations(
                        tag = "vara",
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let { openInBrowser(context, it.item) }

                    credit.getStringAnnotations(
                        tag = "buckwheat",
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let { openInBrowser(context, it.item) }

                    credit.getStringAnnotations(
                        tag = "danil",
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let { openInBrowser(context, it.item) }
                },
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { openInBrowser(context, VARA_URL + "/buckwheat") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.about_source_code),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = { openInBrowser(context, BUCKWHEAT_URL) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.about_original_project),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
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
