package com.danilkinkin.buckwheat.recalcBudget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.DescriptionButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.util.numberFormat
import java.math.BigDecimal

@Composable
fun AddToTodayButton(
    recalcBudgetViewModel: RecalcBudgetViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onSet: () -> Unit = {},
) {
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val howMuchNotSpent by recalcBudgetViewModel.howMuchNotSpent.observeAsState(BigDecimal.ZERO)
    val budgetPerDayAdd by recalcBudgetViewModel.newDailyBudgetIfAddToday.observeAsState(BigDecimal.ZERO)


    DescriptionButton(
        title = { Text(stringResource(R.string.add_current_day_title)) },
        description = {
            Text(
                stringResource(
                    R.string.add_current_day_description,
                    numberFormat(
                        howMuchNotSpent + budgetPerDayAdd,
                        currency = currency,
                    ),
                    numberFormat(
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
            spendsViewModel.setDailyBudget(budgetPerDayAdd + howMuchNotSpent)

            onSet()
        },
    )
}