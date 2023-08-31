package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.RestedBudgetDistributionMethod
import com.danilkinkin.buckwheat.data.SpendsViewModel

const val DEFAULT_RECALC_BUDGET_CHOOSER = "defaultRecalcBudgetChooser"

@Composable
fun DefaultRecalcBudgetChooser(
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val restedBudgetDistributionMethod by spendsViewModel.restedBudgetDistributionMethod.observeAsState()

    val navigationBarHeight = WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)

    Surface {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.choose_recalc_budget_method_label),
                    style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight)
            ) {
                Text(
                    text = stringResource(R.string.choose_recalc_budget_method_description),
                    style = MaterialTheme.typography.bodyMedium
                        .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
                    softWrap = true,
                    modifier = Modifier
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 16.dp,
                        )
                )
                CheckedRow(
                    checked = restedBudgetDistributionMethod === RestedBudgetDistributionMethod.ASK,
                    onValueChange = {
                        spendsViewModel.changeRestedBudgetDistributionMethod(RestedBudgetDistributionMethod.ASK)
                        onClose()
                    },
                    text = stringResource(R.string.always_ask),
                )
                CheckedRow(
                    checked = restedBudgetDistributionMethod === RestedBudgetDistributionMethod.REST,
                    onValueChange = {
                        spendsViewModel.changeRestedBudgetDistributionMethod(RestedBudgetDistributionMethod.REST)
                        onClose()
                    },
                    text = stringResource(R.string.method_split_to_rest_days_title),
                    description = stringResource(R.string.method_split_to_rest_days_description),
                )
                CheckedRow(
                    checked = restedBudgetDistributionMethod === RestedBudgetDistributionMethod.ADD_TODAY,
                    onValueChange = {
                        spendsViewModel.changeRestedBudgetDistributionMethod(RestedBudgetDistributionMethod.ADD_TODAY)
                        onClose()
                    },
                    text = stringResource(R.string.method_add_to_current_day_title),
                    description = stringResource(R.string.method_add_to_current_day_description),
                )
            }
        }
    }
}