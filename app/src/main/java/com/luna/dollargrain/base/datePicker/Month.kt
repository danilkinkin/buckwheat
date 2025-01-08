package com.luna.dollargrain.base.datePicker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.prettyYearMonth
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun MonthHeader(modifier: Modifier = Modifier, yearMonth: YearMonth) {
    Row(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(start = 24.dp)
                .weight(1f),
            text = prettyYearMonth(yearMonth).lowercase(),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
fun PreviewMonthHeader() {
    DollargrainTheme {
        MonthHeader(
            yearMonth = YearMonth.from(LocalDate.now().withDayOfMonth(1)),
        )
    }
}