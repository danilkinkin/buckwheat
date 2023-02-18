package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.finishPeriod.RestBudgetCard
import com.danilkinkin.buckwheat.finishPeriod.WholeBudgetCard
import com.danilkinkin.buckwheat.util.ExtendCurrency

@Composable
fun BudgetSummary(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onEdit: () -> Unit = {},
) {
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    val wholeBudget = spendsViewModel.budget.value!!
    val restBudget =
        (spendsViewModel.budget.value!! - spendsViewModel.spent.value!! - spendsViewModel.spentFromDailyBudget.value!!)

    Column(Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)) {
        WholeBudgetCard(
            budget = wholeBudget,
            currency = currency!!,
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
                currency = currency!!,
            )
            Spacer(modifier = Modifier.width(16.dp))
            EditButton(onClick = { onEdit() })
        }
    }
}