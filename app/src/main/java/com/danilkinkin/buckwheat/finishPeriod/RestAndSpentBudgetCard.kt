package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.ExtendCurrency

@Composable
fun RestAndSpentBudgetCard(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val showRestBudgetCard by appViewModel.showRestBudgetCardByDefault.observeAsState(true)

    val wholeBudget = spendsViewModel.budget.value!!
    val restBudget = spendsViewModel.calcResetBudget()

    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .clickable { appViewModel.setShowRestBudgetCardByDefault(!showRestBudgetCard) }
    ) {
        if (showRestBudgetCard) {
            RestBudgetCard(
                rest = restBudget,
                budget = wholeBudget,
                currency = currency!!,
            )
        } else {
            SpendsBudgetCard(
                spend = wholeBudget - restBudget,
                budget = wholeBudget,
                currency = currency!!,
            )
        }
        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = if (showRestBudgetCard) 1f else 0.5f),
                        shape = CircleShape,
                    )
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = if (!showRestBudgetCard) 1f else 0.5f),
                        shape = CircleShape,
                    )
            )
        }
    }
}