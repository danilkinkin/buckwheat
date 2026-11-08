package com.danilkinkin.buckwheat.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.wallet.CURRENCY_EDITOR
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

/**
 * Idle state of the editor: instead of greeting the user with an empty "0", show the one number
 * that matters — what this month has cost so far — plus the currency, which is otherwise
 * unreachable now that the wallet has no entry point. Tapping the amount starts a new spend; the
 * full, swipeable ledger stays behind the sheet's expand gesture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard(
    modifier: Modifier = Modifier,
    onStartEditing: () -> Unit = {},
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    val currentMonth = YearMonth.from(LocalDate.now())
    val monthTotal = spends
        .asSequence()
        .filter { transaction -> YearMonth.from(transaction.date.toLocalDate()) == currentMonth }
        .fold(BigDecimal.ZERO) { total, transaction -> total + transaction.value }

    val currencyLabel = when (currency.type) {
        ExtendCurrency.Type.NONE -> stringResource(R.string.set_currency)
        else -> currency.value.orEmpty()
    }

    Column(
        modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onStartEditing() },
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = prettyYearMonth(currentMonth),
                    style = MaterialTheme.typography.labelLarge,
                    color = colorOnEditor.copy(alpha = 0.6f),
                )
                Card(
                    onClick = { appViewModel.openSheet(PathState(CURRENCY_EDITOR)) },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = currencyLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = numberFormat(context, monthTotal, currency, trimDecimalPlaces = true),
                style = MaterialTheme.typography.displayMedium,
                color = colorOnEditor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
