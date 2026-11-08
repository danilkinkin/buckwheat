package com.danilkinkin.buckwheat.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

/**
 * What this month has cost so far, compact enough to sit in the editor toolbar next to the
 * settings button. Kept separate from the amount input so entering an expense stays the
 * default state.
 */
@Composable
fun MonthTotal(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    val currentMonth = YearMonth.from(LocalDate.now())
    val monthTotal = spends
        .asSequence()
        .filter { transaction -> YearMonth.from(transaction.date.toLocalDate()) == currentMonth }
        .fold(BigDecimal.ZERO) { total, transaction -> total + transaction.value }

    Column(modifier) {
        Text(
            text = prettyYearMonth(currentMonth),
            style = MaterialTheme.typography.labelSmall,
            color = colorOnEditor.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = numberFormat(context, monthTotal, currency, trimDecimalPlaces = true),
            style = MaterialTheme.typography.titleLarge,
            color = colorOnEditor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
