package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun EditorRow(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    fontSizeValue: TextUnit = MaterialTheme.typography.displayLarge.fontSize,
    fontSizeLabel: TextUnit = MaterialTheme.typography.labelMedium.fontSize,
) {
    Column(
        modifier = modifier
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge,
            fontSize = fontSizeValue,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = fontSizeLabel,
        )
    }
}

@Preview
@Composable
fun PreviewEditorRow() {
    BuckwheatTheme {
        EditorRow(
            value = "1 245 P",
            label = stringResource(id = R.string.budget_for_today),
        )
    }
}