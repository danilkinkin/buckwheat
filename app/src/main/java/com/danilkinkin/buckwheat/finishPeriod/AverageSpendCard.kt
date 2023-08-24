package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.util.numberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Composable
fun AverageSpendCard(
    modifier: Modifier = Modifier,
    spends: List<Spent>,
    currency: ExtendCurrency,
) {
    StatCard(
        modifier = modifier,
        value = if (spends.isNotEmpty()) {
            numberFormat(
                spends
                    .reduce { acc, spent -> acc.copy(value = acc.value + spent.value) }
                    .value
                    .divide(spends.size.toBigDecimal(), 2, RoundingMode.HALF_EVEN),
                currency = currency,
            )
        } else {
            "-"
        },
        label = stringResource(R.string.average_spent),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 32.dp)
    )
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        AverageSpendCard(
            spends = listOf(
                Spent(value = BigDecimal(30), date = Date()),
                Spent(value = BigDecimal(15), date = Date()),
                Spent(value = BigDecimal(42), date = Date()),
            ),
            currency = ExtendCurrency.none(),
        )
    }
}