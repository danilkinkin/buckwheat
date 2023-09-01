package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.AnimatedNumber
import com.danilkinkin.buckwheat.util.HarmonizedColorPalette

@Composable
fun ValueLabel(
    harmonizedColor: HarmonizedColorPalette,
    restBudgetPillViewModel: RestBudgetPillViewModel = hiltViewModel(),
) {
    val budgetState by restBudgetPillViewModel.state.observeAsState(DaileBudgetState.NORMAL)
    val todayBudget by restBudgetPillViewModel.todayBudget.observeAsState("")
    val newDailyBudget by restBudgetPillViewModel.newDailyBudget.observeAsState("")

    AnimatedContent(
        label = "Budget animated content",
        targetState = budgetState
    ) { targetState ->
        when (targetState) {
            DaileBudgetState.NORMAL, null -> {
                AnimatedNumber(
                    value = todayBudget,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize
                    ),
                )
            }
            DaileBudgetState.BUDGET_END, DaileBudgetState.NOT_SET -> {}
            DaileBudgetState.OVERDRAFT -> {
                AnimatedNumber(
                    value = newDailyBudget,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize
                    ),
                )
            }
        }
    }
    Spacer(modifier = Modifier.width(16.dp))
}