package com.luna.dollargrain.history

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.prettyDate
import com.luna.dollargrain.util.toDate
import com.luna.dollargrain.util.toLocalDate
import java.time.LocalDate
import java.util.*

@Composable
fun HistoryDateDivider(date: LocalDate) {
    Text(
        text = prettyDate(date.toDate(), forceShowDate = true, showTime = false, human = true),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 32.dp, end = 32.dp, top = 36.dp, bottom = 16.dp)
    )
}

@Preview
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        HistoryDateDivider(Date().toLocalDate())
    }
}