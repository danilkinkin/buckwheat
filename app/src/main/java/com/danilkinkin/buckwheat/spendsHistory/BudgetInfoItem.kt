package com.danilkinkin.buckwheat.spendsHistory

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
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
import java.util.*

@Composable
fun BudgetInfo(
    budget: BigDecimal,
    startDate: Date,
    finishDate: Date,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = combineColors(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.surfaceVariant,
        angle = 0.9F,
    )
    val contentColor = contentColorFor(surfaceColor)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surfaceColor,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.new_budget),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = prettyCandyCanes(budget, currency = currency),
                style = MaterialTheme.typography.displayLarge,
                color = contentColor,
            )
            Spacer(Modifier.height(24.dp))
            Row {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = stringResource(R.string.label_start_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                    )
                    Text(
                        text = prettyDate(
                            startDate,
                            showTime = false,
                            forceShowDate = true,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                    )
                }

                Spacer(Modifier.width(16.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = contentColor,
                )

                Spacer(Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.label_finish_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                    )
                    Text(
                        text = prettyDate(
                            finishDate,
                            showTime = false,
                            forceShowDate = true,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                    )
                }
            }
        }
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        BudgetInfo(
            budget = BigDecimal(65000),
            startDate = Date(),
            finishDate = Date(),
            currency = ExtendCurrency(type = CurrencyType.NONE)
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        BudgetInfo(
            budget = BigDecimal(65000),
            startDate = Date(),
            finishDate = Date(),
            currency = ExtendCurrency(type = CurrencyType.NONE)
        )
    }
}