package com.luna.dollargrain.recalcBudget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.base.DescriptionButton
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.util.getAnnotatedString
import com.luna.dollargrain.util.numberFormat
import java.math.BigDecimal

@Composable
fun SplitToRestDaysButton(
    recalcBudgetViewModel: RecalcBudgetViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onSet: () -> Unit = {},
) {
    val context = LocalContext.current
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val newDailyBudgetIfSplitPerDay by recalcBudgetViewModel.newDailyBudgetIfSplitPerDay.observeAsState(
        BigDecimal.ZERO
    )

    DescriptionButton(
        title = { Text("split among the remaining days") },
        description = {
            val newDailyBudgetStr = numberFormat(
                context,
                newDailyBudgetIfSplitPerDay,
                currency = currency,
            )
            val resultStr = "get $newDailyBudgetStr per day"

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