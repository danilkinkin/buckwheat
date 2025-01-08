package com.luna.dollargrain.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.luna.dollargrain.R
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.colorOnEditor

@Composable
fun NoSpends(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_spends),
            style = MaterialTheme.typography.bodyMedium,
            color = colorOnEditor.copy(alpha = 0.7f)
        )
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        NoSpends()
    }
}