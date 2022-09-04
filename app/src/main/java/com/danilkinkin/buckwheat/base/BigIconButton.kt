package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = Color.Transparent,
        contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer)
    ),
    icon: Painter,
    contentDescription: String?,
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .background(color = colors.containerColor(enabled).value)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = false,
                )
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(36.dp),
            painter = icon,
            tint = colors.contentColor(enabled).value,
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
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer)
            ),
            onClick = {},
        )
    }
}