package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.danilkinkin.buckwheat.util.countDays

@Composable
fun FinishPeriod(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onCreateNewPeriod: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val spends by spendsViewModel.getSpends().observeAsState(initial = emptyList())
    val wholeBudget = spendsViewModel.budget.value!!
    val restBudget = (spendsViewModel.budget.value!! - spendsViewModel.spent.value!! - spendsViewModel.spentFromDailyBudget.value!!)

    val navigationBarHeight = WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)
    
    Surface {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = navigationBarHeight),
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
                WholeBudgetCard(
                    budget = wholeBudget,
                    currency = spendsViewModel.currency,
                    startDate = spendsViewModel.startDate.value!!,
                    finishDate = spendsViewModel.finishDate.value!!,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    RestBudgetCard(
                        modifier = Modifier.weight(1f),
                        rest = restBudget,
                        budget = wholeBudget,
                        currency = spendsViewModel.currency,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FillCircleStub()
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    MinMaxSpentCard(
                        modifier = Modifier.weight(1f),
                        isMin = true,
                        spends = spends,
                        currency = spendsViewModel.currency,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    MinMaxSpentCard(
                        modifier = Modifier.weight(1f),
                        isMin = false,
                        spends = spends,
                        currency = spendsViewModel.currency,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    SpendsCountCard(
                        modifier = Modifier,
                        count = spends.size,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AverageSpendCard(
                        modifier = Modifier.weight(1f),
                        spends = spends,
                        currency = spendsViewModel.currency,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OverspendingInfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    budget = wholeBudget,
                    spends = spends,
                    startDate = spendsViewModel.startDate.value!!,
                    finishDate = spendsViewModel.finishDate.value!!,
                    currency = spendsViewModel.currency,
                )
                Spacer(modifier = Modifier.height(16.dp))
                AdviceCard(Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(48.dp))
            DescriptionButton(
                title = { Text(stringResource(R.string.new_period_title)) },
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
                onClick = {
                    onCreateNewPeriod()
                    onClose()
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        FinishPeriod()
    }
}