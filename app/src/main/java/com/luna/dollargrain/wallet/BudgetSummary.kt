package com.luna.dollargrain.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.analytics.RestAndSpentBudgetCard
import com.luna.dollargrain.analytics.WholeBudgetCard
import com.luna.dollargrain.data.ExtendCurrency

// widget in the wallet sheet

@Composable
fun BudgetSummary(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onEdit: () -> Unit = {},
) {
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val wholeBudget = spendsViewModel.budget.value!!

    Column(Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)) {
        RestAndSpentBudgetCard(
            modifier = Modifier,
            bigVariant = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            WholeBudgetCard(
                modifier = Modifier.weight(1f),
                bigVariant = false,
                budget = wholeBudget,
                currency = currency,
                startDate = spendsViewModel.startPeriodDate.value!!,
                finishDate = spendsViewModel.finishPeriodDate.value!!,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
            )
            Spacer(modifier = Modifier.width(16.dp))
            DaysLeftCard(
                startDate = spendsViewModel.startPeriodDate.value!!,
                finishDate = spendsViewModel.finishPeriodDate.value!!,
                colors = CardDefaults.elevatedCardColors()
            )
        }
        EditButton(onClick = { onEdit() })
    }
}
