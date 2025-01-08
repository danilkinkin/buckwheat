package com.luna.dollargrain.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
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
import com.luna.dollargrain.R
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.combineColors

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
    endCaption: String? = null,
    iconInset: Boolean = true,
    text: String,
    wrapMainText: Boolean = false,
    description: String? = null,
    denseDescriptionOffset: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    descriptionTextStyle: TextStyle = MaterialTheme.typography.bodySmall
        .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
) {
    Column(modifier) {
        Box(Modifier.padding(top = 16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(24.dp)
                    .padding(horizontal = 24.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier
                        .padding(
                            start = if (!iconInset && icon === null) 8.dp else (24 + 16).dp,
                            top = 0.dp,
                            bottom = if (description !== null) 0.dp else 16.dp,
                        )
                        .heightIn(24.dp)
                        .widthIn(min = 100.dp)
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = text,
                        style = textStyle,
                        softWrap = wrapMainText,
                        overflow = if (wrapMainText) TextOverflow.Visible else TextOverflow.Ellipsis,
                    )
                }

                if (endCaption !== null) {
                    Text(
                        modifier = Modifier.widthIn(max = 200.dp),
                        text = endCaption,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                    )
                }

                if (endContent !== null || endIcon !== null) {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        Row(
                            Modifier
                                .height(24.dp)
                                .padding(start = 16.dp),
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
                        .height(24.dp)
                        .width(64.dp)
                        .padding(start = 24.dp, end = 16.dp),
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
                        start = if (!iconInset && icon === null) 32.dp else (24 + 24 + 16).dp,
                        top = if (denseDescriptionOffset) 0.dp else 8.dp,
                        end = 24.dp,
                        bottom = 24.dp,
                    )
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DollargrainTheme {
        TextRow(
            text = "Text row",
        )
    }
}

@Preview
@Composable
private fun PreviewWithDescription() {
    DollargrainTheme {
        TextRow(
            text = "Text row",
            description = "Description of text row",
        )
    }
}

@Preview
@Composable
private fun PreviewWithDescriptionIconInset() {
    DollargrainTheme {
        TextRow(
            text = "Text row",
            description = "Description of text row",
            iconInset = false,
        )
    }
}

@Preview()
@Composable
private fun PreviewTWithIcon() {
    DollargrainTheme {
        TextRow(
            icon = painterResource(id = R.drawable.ic_home),
            text = "Text row",
        )
    }
}

@Preview()
@Composable
private fun PreviewWithDescriptionWithIcon() {
    DollargrainTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            text = "Text row",
            description = "Description of text row",
        )
    }
}

@Preview()
@Composable
private fun PreviewTWithIconWithEndContent() {
    DollargrainTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            text = "Text row",
            endCaption = "Very looooooooooooong end content as text"
        )
    }
}

@Preview()
@Composable
private fun PreviewWithIconWithEndContentWithLongTitle() {
    DollargrainTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            text = "Text row loooooooooooooooooooooooooooooooong",
            endCaption = "Very looooooooooooong end content as text"
        )
    }
}

@Preview()
@Composable
private fun PreviewWithIcons() {
    DollargrainTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            endIcon = painterResource(R.drawable.ic_edit),
            text = "Text row loooooooooooooooooooooooooooooooong",
        )
    }
}

@Preview()
@Composable
private fun PreviewWithIconsWithChip() {
    DollargrainTheme {
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

@Preview()
@Composable
private fun PreviewWithIconsWithChipAndEndIcon() {
    DollargrainTheme {
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

@Preview()
@Composable
private fun PreviewWithIconsWithChipWithWrapText() {
    DollargrainTheme {
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

@Preview(name = "With icon, end content, end icon and description")
@Composable
private fun PreviewWithIconsWithChipWithDescriptionWithEndContentAndEnIcon() {
    DollargrainTheme {
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


@Preview(name = "With icon, end content, end icon and description")
@Composable
private fun PreviewWithIconsWithChipWithDescriptionWithEndContent() {
    DollargrainTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            endContent = {
                SuggestionChip(
                    label = { Text(text = "Suggestion") },
                    onClick = { /*TODO*/ },
                )
            },
            wrapMainText = true,
            text = "Text row",
            description = "Description loooooooooooooooooooooong text",
        )
    }
}