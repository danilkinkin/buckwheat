package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
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
    endIcon: Painter? = null,
    text: String,
) {
    val color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.3F,
        )
    )

    Box(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    start = (24 + 16).dp,
                    end = if (endIcon !== null) (24 + 16).dp else 0.dp,
                )
            )
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
                    tint = color,
                    contentDescription = null
                )
            }
        }

        if (endIcon !== null) {
            Box(
                Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = endIcon,
                    tint = color,
                    contentDescription = null
                )
            }
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