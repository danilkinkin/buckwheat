package com.danilkinkin.buckwheat.recalcBudget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import java.math.RoundingMode
import kotlin.math.abs

@Composable
fun RecalcBudget(
    spendsViewModel: SpendsViewModel = viewModel(),
    onClose: () -> Unit = {},
) {
    val restDays = countDays(spendsViewModel.finishDate)
    val skippedDays = abs(countDays(spendsViewModel.lastReCalcBudgetDate!!))

    val restBudget =
        (spendsViewModel.budget.value!! - spendsViewModel.spent.value!!) - spendsViewModel.dailyBudget.value!!
    val perDayBudget = restBudget / (restDays + skippedDays - 1).toBigDecimal()

    val requireDistributeBudget = perDayBudget * (skippedDays - 1).coerceAtLeast(0)
        .toBigDecimal() + spendsViewModel.dailyBudget.value!! - spendsViewModel.spentFromDailyBudget.value!!

    val budgetPerDaySplit =
        ((restBudget + spendsViewModel.dailyBudget.value!! - spendsViewModel.spentFromDailyBudget.value!!) / restDays.toBigDecimal()).setScale(
            0,
            RoundingMode.FLOOR
        )
    val budgetPerDayAdd = (restBudget / restDays.toBigDecimal()).setScale(0, RoundingMode.FLOOR)
    val budgetPerDayAddDailyBudget = budgetPerDayAdd + requireDistributeBudget


    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.new_day_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = prettyCandyCanes(
                requireDistributeBudget,
                currency = spendsViewModel.currency,
            ),
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.recalc_budget),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(48.dp))
        ButtonWithIcon(
            title = stringResource(R.string.split_rest_days_title),
            description = stringResource(
                R.string.split_rest_days_description,
                prettyCandyCanes(
                    budgetPerDaySplit,
                    currency = spendsViewModel.currency,
                ),
            ),
            onClick = {
                spendsViewModel.reCalcDailyBudget(budgetPerDaySplit)

                onClose()
            },
        )
        Spacer(Modifier.height(16.dp))
        ButtonWithIcon(
            title = stringResource(R.string.add_current_day_title),
            description = stringResource(
                R.string.add_current_day_description,
                prettyCandyCanes(
                    requireDistributeBudget + budgetPerDayAdd,
                    currency = spendsViewModel.currency,
                ),
                prettyCandyCanes(
                    budgetPerDayAdd,
                    currency = spendsViewModel.currency,
                ),
            ),
            onClick = {
                spendsViewModel.reCalcDailyBudget(budgetPerDayAdd + requireDistributeBudget)

                onClose()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonWithIcon(
    title: String,
    description: String,
    onClick: () -> Unit,
){
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .weight(weight = 1F, fill = true)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .padding(end = 8.dp),
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
fun PreviewButtonWithIcon(){
    BuckwheatTheme {
        ButtonWithIcon(
            title = "Title",
            description = "Button looooooooooooooooooooooooooooooooooong description",
            onClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewRecalcBudget() {
    BuckwheatTheme {
        RecalcBudget()
    }
}