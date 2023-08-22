package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun Total(
    budget: BigDecimal,
    restBudget: BigDecimal,
    days: Int,
    currency: ExtendCurrency,
) {
    val textColor = LocalContentColor.current

    Column {
        if (budget > BigDecimal.ZERO && days > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.total_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 64.dp, end = 16.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.per_day,
                    prettyCandyCanes(
                        (budget / days.toBigDecimal()).setScale(0, RoundingMode.FLOOR),
                        currency,
                    ),
                ),
                color = textColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 64.dp, end = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            var description = ""
            if (budget <= BigDecimal.ZERO) {
                description += "- " + stringResource(id = R.string.budget_must_greater_zero) + "\n"
            }
            if (days <= 0) {
                description += "- " + stringResource(id = R.string.days_must_greater_zero) + "\n"
            }

            TextRow(
                icon = painterResource(R.drawable.ic_info),
                text = stringResource(id = R.string.unable_calc_budget),
                description = description
            )
        }
    }
}