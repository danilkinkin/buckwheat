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
import com.danilkinkin.buckwheat.data.RestedBudgetDistributionMethod
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.util.numberFormat
import java.math.BigDecimal

@Composable
fun SplitToRestDaysButton(
    recalcBudgetViewModel: RecalcBudgetViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onSet: () -> Unit = {},
) {
    val isDebug by appViewModel.isDebug.observeAsState(false)
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val newDailyBudgetIfSplitPerDay by recalcBudgetViewModel.newDailyBudgetIfSplitPerDay.observeAsState(
        BigDecimal.ZERO
    )

    DescriptionButton(
        title = { Text(stringResource(R.string.split_rest_days_title)) },
        description = {
            Text(
                stringResource(
                    R.string.split_rest_days_description,
                    numberFormat(
                        newDailyBudgetIfSplitPerDay,
                        currency = currency,
                    ),
                )
            )
        },
        /* secondDescription = if (isDebug) {
            { Text("($restBudget + ${spendsViewModel.dailyBudget.value!!} - ${spendsViewModel.spentFromDailyBudget.value!!}) / $restDays = $whatBudgetForDay") }
        } else null, */
        onClick = {
            spendsViewModel.setDailyBudget(newDailyBudgetIfSplitPerDay)

            onSet()
        },
    )
}