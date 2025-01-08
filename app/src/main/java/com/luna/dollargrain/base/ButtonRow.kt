package com.luna.dollargrain.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.luna.dollargrain.R
import com.luna.dollargrain.ui.DollargrainTheme

@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconInset: Boolean = true,
    endIcon: Painter? = null,
    text: String,
    wrapMainText: Boolean = false,
    description: String? = null,
    denseDescriptionOffset: Boolean = true,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null,
    endCaption: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    TextRow(
        modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick.invoke() },
        icon = icon,
        iconInset = iconInset,
        endIcon = endIcon,
        wrapMainText = wrapMainText,
        text = text,
        description = description,
        denseDescriptionOffset = denseDescriptionOffset,
        endContent = endContent,
        endCaption = endCaption,
    )
}

@Preview
@Composable
fun PreviewButtonRowWithIcon() {
    DollargrainTheme {
        ButtonRow(
            icon = painterResource(R.drawable.ic_home),
            text = "Text row",
            onClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewButtonRowWithoutIcon() {
    DollargrainTheme {
        ButtonRow(
            text = "Text row",
            onClick = {},
        )
    }
}