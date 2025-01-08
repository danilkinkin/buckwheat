package com.luna.dollargrain.analytics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.data.entities.TransactionType
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.numberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

@Composable
fun AverageSpendCard(
    modifier: Modifier = Modifier,
    spends: List<Transaction>,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current

    StatCard(
        modifier = modifier,
        value = if (spends.isNotEmpty()) {
            numberFormat(
                context,
                spends
                    .reduce { acc, spent -> acc.copy(value = acc.value + spent.value) }
                    .value
                    .divide(spends.size.toBigDecimal(), 2, RoundingMode.HALF_EVEN),
                currency = currency,
            )
        } else {
            "-"
        },
        label = "average spent",
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 32.dp),
        colors = CardDefaults.cardColors()
    )
}

@Preview
@Composable
private fun Preview() {
    DollargrainTheme {
        AverageSpendCard(
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
            currency = ExtendCurrency.none(),
        )
    }
}