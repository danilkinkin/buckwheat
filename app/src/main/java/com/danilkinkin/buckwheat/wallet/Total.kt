package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.util.ExtendCurrency
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
        if (budget > BigDecimal(0) && days > 0) {
            Text(
                text = stringResource(R.string.total_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 56.dp, end = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.per_day,
                    prettyCandyCanes(
                        if (days != 0) {
                            (budget / days.toBigDecimal()).setScale(0, RoundingMode.FLOOR)
                        } else {
                            budget
                        },
                        currency,
                    ),
                ),
                color = textColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, end = 16.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                    ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Box(
                        modifier = Modifier.heightIn(24.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = stringResource(id = R.string.unable_calc_budget),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    if (budget <= BigDecimal(0)) {
                        Text(
                            text = "- " + stringResource(id = R.string.budget_must_greater_zero),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.8f),
                        )
                    }
                    if (days <= 0) {
                        Text(
                            text = "- " + stringResource(id = R.string.days_must_greater_zero),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
    }
}