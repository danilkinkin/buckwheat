package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.DescriptionButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*

@Composable
fun FinishPeriod(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onCreateNewPeriod: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val spends by spendsViewModel.getSpends().observeAsState(initial = emptyList())
    val wholeBudget = spendsViewModel.budget.value!!
    val restBudget = (spendsViewModel.budget.value!! - spendsViewModel.spent.value!!)
    val minSpent = spends.minByOrNull { it.value }
    val maxSpent = spends.maxByOrNull { it.value }
    
    Surface {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.finish_period_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.period_summary_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth()) {
                ValueWithLabel(
                    value = prettyCandyCanes(
                        wholeBudget,
                        currency = spendsViewModel.currency,
                    ),
                    label = stringResource(R.string.whole_budget),
                )
                Row {
                    ValueWithLabel(
                        value = prettyDate(
                            spendsViewModel.startDate,
                            showTime = false,
                            forceShowDate = true,
                        ),
                        label = stringResource(R.string.label_start_date),
                        fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                    )
                    Spacer(Modifier.width(24.dp))
                    ValueWithLabel(
                        value = prettyDate(
                            spendsViewModel.finishDate,
                            showTime = false,
                            forceShowDate = true,
                        ),
                        label = stringResource(R.string.label_finish_date),
                        fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                    )
                }
                ValueWithLabel(
                    value = prettyCandyCanes(
                        restBudget,
                        currency = spendsViewModel.currency,
                    ),
                    label = stringResource(R.string.rest_budget),
                    fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                )
                Row {
                    ValueWithLabel(
                        value = if (minSpent !== null) {
                            prettyCandyCanes(
                                minSpent.value,
                                currency = spendsViewModel.currency,
                            )
                        } else {
                            "-"
                        },
                        secondValue = if (minSpent !== null) {
                            prettyDate(
                                minSpent.date,
                                forceShowDate = true,
                                showTime = true,
                            )
                        } else {
                            null
                        },
                        label = stringResource(R.string.min_spent),
                        fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                        fontSizeSecondValue = MaterialTheme.typography.bodySmall.fontSize,
                    )
                    Spacer(Modifier.width(24.dp))
                    ValueWithLabel(
                        value = if (maxSpent !== null) {
                            prettyCandyCanes(
                                maxSpent.value,
                                currency = spendsViewModel.currency,
                            )
                        } else {
                            "-"
                        },
                        secondValue = if (maxSpent !== null) {
                            prettyDate(
                                maxSpent.date,
                                forceShowDate = true,
                                showTime = true,
                            )
                        } else {
                            null
                        },
                        label = stringResource(R.string.max_spent),
                        fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                        fontSizeSecondValue = MaterialTheme.typography.bodySmall.fontSize,
                    )
                    Spacer(Modifier.width(24.dp))
                    ValueWithLabel(
                        value = spends.size.toString(),
                        label = stringResource(R.string.count_spends),
                        fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                    )
                }
            }
            Spacer(Modifier.height(48.dp))
            DescriptionButton(
                title = { Text(stringResource(R.string.new_period_title)) },
                onClick = {
                    onCreateNewPeriod()
                    onClose()
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ValueWithLabel(
    modifier: Modifier = Modifier,
    value: String,
    secondValue: String? = null,
    label: String,
    fontSizeValue: TextUnit = MaterialTheme.typography.displayLarge.fontSize,
    fontSizeSecondValue: TextUnit = MaterialTheme.typography.displayLarge.fontSize,
    fontSizeLabel: TextUnit = MaterialTheme.typography.labelMedium.fontSize,
) {
    val color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.9F,
        )
    )

    Column(modifier.padding(bottom = 24.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge,
            fontSize = fontSizeValue,
            color = color,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
        )
        if (secondValue !== null) {
            Text(
                text = secondValue,
                style = MaterialTheme.typography.displayLarge,
                fontSize = fontSizeSecondValue,
                color = color,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = fontSizeLabel,
            color = color,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
        )
    }
}

@Preview
@Composable
fun PreviewRecalcBudget() {
    BuckwheatTheme {
        FinishPeriod()
    }
}