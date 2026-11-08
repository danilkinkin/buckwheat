package com.danilkinkin.buckwheat.history

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.util.Date

@Composable
fun HistoryDateDivider(
    date: java.time.LocalDate,
    dayTotal: BigDecimal,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prettyDate(date.toDate(), forceShowDate = true, showTime = false, human = true),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = numberFormat(context, dayTotal, currency = currency),
            style = MaterialTheme.typography.labelMedium,
            color = colorOnEditor.copy(alpha = 0.6f),
        )
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        HistoryDateDivider(
            date = Date().toLocalDate(),
            dayTotal = BigDecimal(12340),
            currency = ExtendCurrency.none(),
        )
    }
}
