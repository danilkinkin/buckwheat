package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*

@Composable
fun TextWithLabel(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    fontSizeValue: TextUnit = MaterialTheme.typography.displayLarge.fontSize,
    fontSizeLabel: TextUnit = MaterialTheme.typography.labelMedium.fontSize,
    contentPaddingValues: PaddingValues = PaddingValues(start = 36.dp, end = 36.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    val color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.9F,
        )
    )

    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge,
            fontSize = fontSizeValue,
            color = color,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.padding(contentPaddingValues),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = fontSizeLabel,
            color = color,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.padding(contentPaddingValues),
        )
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        TextWithLabel(
            value = "1 245 234 234 P",
            label = stringResource(id = R.string.budget_for_today),
        )
    }
}