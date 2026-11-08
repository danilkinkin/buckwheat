package com.danilkinkin.buckwheat.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.history.NoSpends
import com.danilkinkin.buckwheat.history.SpentItem
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date

private const val RECENT_SPENDS_COUNT = 3

/**
 * Idle state of the editor: instead of greeting the user with an empty "0", show what the
 * ledger actually holds. Tapping anywhere starts a new spend, which swaps this out for the
 * amount input and keeps the numeric keypad untouched.
 */
@Composable
fun HomeDashboard(
    modifier: Modifier = Modifier,
    onStartEditing: () -> Unit = {},
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
    val todayTotal = spends
        .asSequence()
        .filter { transaction -> isSameDay(transaction.date, Date()) }
        .fold(BigDecimal.ZERO) { total, transaction -> total + transaction.value }
    val recentSpends = spends.takeLast(RECENT_SPENDS_COUNT).asReversed()

    Column(
        modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onStartEditing() },
    ) {
        Column(
            Modifier.padding(start = 36.dp, end = 36.dp, top = 8.dp),
        ) {
            Text(
                text = prettyYearMonth(currentMonth),
                style = MaterialTheme.typography.labelLarge,
                color = colorOnEditor.copy(alpha = 0.6f),
            )
            Text(
                text = numberFormat(context, monthTotal, currency, trimDecimalPlaces = true),
                style = MaterialTheme.typography.displaySmall,
                color = colorOnEditor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorOnEditor.copy(alpha = 0.6f),
                )
                Text(
                    text = numberFormat(context, todayTotal, currency, trimDecimalPlaces = true),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorOnEditor,
                )
            }
            Spacer(Modifier.height(12.dp))
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colorOnEditor.copy(alpha = 0.12f)),
            )
        }

        if (recentSpends.isEmpty()) {
            NoSpends(Modifier.weight(1f))
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(recentSpends, key = { transaction -> transaction.uid }) { transaction ->
                    SpentItem(transaction = transaction, currency = currency)
                }
            }
        }
    }
}
