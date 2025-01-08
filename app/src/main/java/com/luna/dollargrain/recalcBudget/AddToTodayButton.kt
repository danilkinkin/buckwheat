package com.luna.dollargrain.recalcBudget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.base.DescriptionButton
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.util.getAnnotatedString
import com.luna.dollargrain.util.numberFormat
import java.math.BigDecimal

@Composable
fun AddToTodayButton(
    recalcBudgetViewModel: RecalcBudgetViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onSet: () -> Unit = {},
) {
    val context = LocalContext.current

    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val nextDayBudget by recalcBudgetViewModel.nextDayBudget.observeAsState(BigDecimal.ZERO)
    val budgetPerDayAdd by recalcBudgetViewModel.newDailyBudgetIfAddToday.observeAsState(BigDecimal.ZERO)


    DescriptionButton(
        title = { Text(stringResource(R.string.add_current_day_title)) },
        description = {

            val budgetTodayStr = numberFormat(
                context,
                budgetPerDayAdd,
                currency = currency,
            )
            val nextDaysBudgetStr = numberFormat(
                context,
                nextDayBudget,
                currency = currency,
            )
            val resultStr = stringResource(
                R.string.add_current_day_description,
                budgetTodayStr,
                nextDaysBudgetStr,
            )

            Text(
                getAnnotatedString(
                    resultStr,
                    listOf(
                        Pair(
                            resultStr.indexOf(budgetTodayStr),
                            resultStr.indexOf(budgetTodayStr) + budgetTodayStr.length,
                        ),
                        Pair(
                            resultStr.indexOf(nextDaysBudgetStr),
                            resultStr.indexOf(nextDaysBudgetStr) + nextDaysBudgetStr.length,
                        ),
                    ),
                    listOf(
                        SpanStyle(fontWeight = FontWeight.W900),
                        SpanStyle(fontWeight = FontWeight.W900),
                    ),
                )
            )
        },
        onClick = {
            spendsViewModel.setDailyBudget(budgetPerDayAdd)

            onSet()
        },
    )
}