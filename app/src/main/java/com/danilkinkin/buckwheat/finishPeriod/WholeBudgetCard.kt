package com.danilkinkin.buckwheat.finishPeriod

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Composable
fun WholeBudgetCard(
    budget: BigDecimal,
    currency: ExtendCurrency,
    startDate: Date,
    finishDate: Date,
) {
    StatCard(
        modifier = Modifier.fillMaxWidth(),
        value = prettyCandyCanes(
            budget,
            currency = currency,
        ),
        label = stringResource(R.string.whole_budget),
        valueFontSize = MaterialTheme.typography.displayMedium.fontSize,
        content = {
            val textColor = LocalContentColor.current

            Spacer(modifier = Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = prettyDate(
                            startDate,
                            showTime = false,
                            forceShowDate = true,
                            forceShowYear = true,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.label_start_date),
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor.copy(alpha = 0.6f),
                    )
                }

                Spacer(Modifier.width(16.dp).weight(1f))

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                )

                Spacer(Modifier.width(16.dp).weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = prettyDate(
                            finishDate,
                            showTime = false,
                            forceShowDate = true,
                            forceShowYear = true,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.label_finish_date),
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        WholeBudgetCard(
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
            startDate = LocalDate.now().minusDays(28).toDate(),
            finishDate = Date(),
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        WholeBudgetCard(
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
            startDate = LocalDate.now().minusDays(28).toDate(),
            finishDate = Date(),
        )
    }
}