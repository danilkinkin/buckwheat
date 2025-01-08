package com.luna.dollargrain.recalcBudget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.base.DescriptionButton
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.SpendsViewModel
import java.math.BigDecimal

@Composable
fun StartLastDayButton(
    recalcBudgetViewModel: RecalcBudgetViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onSet: () -> Unit = {},
) {
    val context = LocalContext.current

    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val budgetPerDayAdd by recalcBudgetViewModel.newDailyBudgetIfAddToday.observeAsState(BigDecimal.ZERO)


    DescriptionButton(
        title = { Text("leave for today") },
        description = {
            Text(
                "its the last day! you can spend $budgetPerDayAdd"
            )
        },
        /* secondDescription = if (isDebug.value) {
            {
                Text(
                    "$restBudget / $restDays = $budgetPerDayAdd " +
                            "\n${budgetPerDayAdd} + $howMuchNotSpent = $budgetPerDayAddDailyBudget"
                )
            }
        } else null, */
        onClick = {
            spendsViewModel.setDailyBudget(budgetPerDayAdd)

            onSet()
        },
    )
}