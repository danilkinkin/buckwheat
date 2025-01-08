package com.luna.dollargrain.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.base.TextRow
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.util.numberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import com.luna.dollargrain.R

@Composable
fun Total(
    budget: BigDecimal,
    restBudget: BigDecimal,
    days: Int,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    val textColor = LocalContentColor.current

    Column {
        if (budget > BigDecimal.ZERO && days > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 64.dp, end = 16.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$${numberFormat(
                    context,
                    (budget / days.toBigDecimal()).setScale(0, RoundingMode.FLOOR),
                    currency,
                )} per day",
                color = textColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 64.dp, end = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            var description = ""
            if (budget <= BigDecimal.ZERO) {
                description += "- Enter your budget\n"
            }
            if (days <= 0) {
                description += "- Calculate the budget for at least one day\n"
            }

            TextRow(
                icon = painterResource(id = R.drawable.ic_info),
                text = "Unable to budget",
                description = description
            )
        }
    }
}