package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun BigIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    icon: Painter,
    contentDescription: String?,
) {
    val contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer)
    val disabledContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer).copy(0.38f)

    Box(
        modifier = modifier
            .padding(8.dp)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = false,
                    color = contentColor,
                )
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(34.dp),
            painter = icon,
            tint = if (enabled) contentColor else disabledContentColor,
            contentDescription = contentDescription,
        )
    }
}

@Preview
@Composable
fun PreviewBigIconButton() {
    BuckwheatTheme {
        BigIconButton(
            icon = painterResource(R.drawable.ic_balance_wallet),
            contentDescription = null,
            onClick = {},
        )
    }
}