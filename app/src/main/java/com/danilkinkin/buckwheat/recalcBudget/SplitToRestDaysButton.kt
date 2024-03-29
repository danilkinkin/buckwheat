package com.danilkinkin.buckwheat.recalcBudget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.DescriptionButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.getAnnotatedString
import com.danilkinkin.buckwheat.util.numberFormat
import java.math.BigDecimal

@Composable
fun SplitToRestDaysButton(
    recalcBudgetViewModel: RecalcBudgetViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onSet: () -> Unit = {},
) {
    val context = LocalContext.current
    val isDebug by appViewModel.isDebug.observeAsState(false)
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val newDailyBudgetIfSplitPerDay by recalcBudgetViewModel.newDailyBudgetIfSplitPerDay.observeAsState(
        BigDecimal.ZERO
    )

    DescriptionButton(
        title = { Text(stringResource(R.string.split_rest_days_title)) },
        description = {
            val newDailyBudgetStr = numberFormat(
                context,
                newDailyBudgetIfSplitPerDay,
                currency = currency,
            )
            val resultStr = stringResource(
                R.string.split_rest_days_description,
                newDailyBudgetStr,
            )

            Text(
                getAnnotatedString(
                    resultStr,
                    listOf(
                        Pair(
                            resultStr.indexOf(newDailyBudgetStr),
                            resultStr.indexOf(newDailyBudgetStr) + newDailyBudgetStr.length,
                        ),
                    ),
                    listOf(
                        SpanStyle(fontWeight = FontWeight.W900),
                    ),
                )
            )
        },
        onClick = {
            spendsViewModel.setDailyBudget(newDailyBudgetIfSplitPerDay)

            onSet()
        },
    )
}