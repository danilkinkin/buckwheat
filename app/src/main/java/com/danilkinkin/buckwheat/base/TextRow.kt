package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun TextRow(
    icon: Painter? = null,
    text: String,
    modifier: Modifier = Modifier,
) {
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
                modifier = Modifier.padding(start = (24 + 16).dp)
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
                    contentDescription = null
                )
            }
        }
    }

}

@Preview()
@Composable
fun PreviewTextRowWithIcon() {
    BuckwheatTheme {
        TextRow(
            icon = painterResource(R.drawable.ic_home),
            text = "Text row",
        )
    }
}

@Preview
@Composable
fun PreviewTextRowWithoutIcon() {
    BuckwheatTheme {
        TextRow(
            text = "Text row",
        )
    }
}