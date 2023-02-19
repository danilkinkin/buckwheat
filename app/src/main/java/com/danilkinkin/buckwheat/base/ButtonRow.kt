package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    endIcon: Painter? = null,
    text: String,
    wrapMainText: Boolean = false,
    description: String? = null,
    denseDescriptionOffset: Boolean = true,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    TextRow(
        modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple()
            ) { onClick.invoke() },
        icon = icon,
        endIcon = endIcon,
        wrapMainText = wrapMainText,
        text = text,
        description = description,
        denseDescriptionOffset = denseDescriptionOffset,
        endContent = endContent,
    )
}

@Preview
@Composable
fun PreviewButtonRowWithIcon() {
    BuckwheatTheme {
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
    BuckwheatTheme {
        ButtonRow(
            text = "Text row",
            onClick = {},
        )
    }
}