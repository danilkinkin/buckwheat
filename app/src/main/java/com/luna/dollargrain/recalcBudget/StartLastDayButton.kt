package com.luna.dollargrain.recalcBudget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.base.DescriptionButton
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.util.numberFormat
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
        title = { Text(stringResource(R.string.add_current_day_title)) },
        description = {
            Text(
                stringResource(
                    R.string.start_last_day_description,
                    numberFormat(
                        context,
                        budgetPerDayAdd,
                        currency = currency,
                    ),
                )
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