package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.combineColors

@Composable
fun TextRow(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconTint: Color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.3F,
        )
    ),
    endIcon: Painter? = null,
    endContent: @Composable (() -> Unit)? = null,
    text: String,
    wrapMainText: Boolean = false,
    description: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    descriptionTextStyle: TextStyle = MaterialTheme.typography.bodyMedium
        .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
) {
    Column(modifier) {
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(56.dp)
                    .padding(horizontal = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = textStyle,
                    softWrap = wrapMainText,
                    overflow = if (wrapMainText) TextOverflow.Visible else TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(
                            start = (24 + 16).dp,
                            top = 16.dp,
                            bottom = if (description !== null) 4.dp else 16.dp,
                        )
                        .weight(1f)
                )

                if (endContent !== null || endIcon !== null) {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        Row(
                            Modifier.height(56.dp).padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (endContent !== null) {
                                endContent()
                                if (endIcon == null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            if (endIcon !== null) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    painter = endIcon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }

            if (icon !== null) {
                Box(
                    Modifier
                        .height(56.dp)
                        .width(56.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        painter = icon,
                        tint = iconTint,
                        contentDescription = null
                    )
                }
            }
        }
        if (description !== null) {
            Text(
                text = description,
                style = descriptionTextStyle,
                softWrap = true,
                modifier = Modifier
                    .padding(
                        start = (24 + 16 * 2).dp,
                        end = 24.dp,
                        bottom = 16.dp,
                    )
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        TextRow(
            text = "Text row",
        )
    }
}

@Preview()
@Composable
private fun PreviewTWithIcon() {
    BuckwheatTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            text = "Text row",
        )
    }
}

@Preview()
@Composable
private fun PreviewWithIcons() {
    BuckwheatTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            endIcon = painterResource(R.drawable.ic_edit),
            text = "Text row loooooooooooooooooooooooooooooooong",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
private fun PreviewWithIconsWithChip() {
    BuckwheatTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            endContent = {
                SuggestionChip(
                    label = { Text(text = "Suggestion") },
                    onClick = { /*TODO*/ },
                )
            },
            text = "Text row loooooooooooooooooooooooooooooooong",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
private fun PreviewWithIconsWithChipAndEndIcon() {
    BuckwheatTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            endIcon = painterResource(R.drawable.ic_edit),
            endContent = {
                SuggestionChip(
                    label = { Text(text = "Suggestion") },
                    onClick = { /*TODO*/ },
                )
            },
            text = "Text row loooooooooooooooooooooooooooooooong",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
private fun PreviewWithIconsWithChipWithWrapText() {
    BuckwheatTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            endIcon = painterResource(R.drawable.ic_edit),
            endContent = {
                SuggestionChip(
                    label = { Text(text = "Suggestion") },
                    onClick = { /*TODO*/ },
                )
            },
            wrapMainText = true,
            text = "Text row loooooooooooooooooooooooooooooooong",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "With icon, end content, end icon and description")
@Composable
private fun PreviewWithIconsWithChipWithDescription() {
    BuckwheatTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            endIcon = painterResource(R.drawable.ic_edit),
            endContent = {
                SuggestionChip(
                    label = { Text(text = "Suggestion") },
                    onClick = { /*TODO*/ },
                )
            },
            wrapMainText = true,
            text = "Text row loooooooooooooooooooooooooooooooong",
            description = "Description looooooooooooooooooooooooooooooooooooong text",
        )
    }
}