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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel

const val DEFAULT_RECALC_BUDGET_CHOOSER = "defaultRecalcBudgetChooser"

@Composable
fun DefaultRecalcBudgetChooser(
    windowSizeClass: WindowWidthSizeClass,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val recalcRestBudgetMethod by spendsViewModel.recalcRestBudgetMethod.observeAsState()

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
                    style = MaterialTheme.typography.titleLarge,
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
                    checked = recalcRestBudgetMethod === SpendsViewModel.RecalcRestBudgetMethod.ASK,
                    onValueChange = {
                        spendsViewModel.changeRecalcRestBudgetMethod(SpendsViewModel.RecalcRestBudgetMethod.ASK)
                    },
                    text = stringResource(R.string.always_ask),
                )
                CheckedRow(
                    checked = recalcRestBudgetMethod === SpendsViewModel.RecalcRestBudgetMethod.REST,
                    onValueChange = {
                        spendsViewModel.changeRecalcRestBudgetMethod(SpendsViewModel.RecalcRestBudgetMethod.REST)
                    },
                    text = stringResource(R.string.method_split_to_rest_days_title),
                    description = stringResource(R.string.method_split_to_rest_days_description),
                )
                CheckedRow(
                    checked = recalcRestBudgetMethod === SpendsViewModel.RecalcRestBudgetMethod.ADD_TODAY,
                    onValueChange = {
                        spendsViewModel.changeRecalcRestBudgetMethod(SpendsViewModel.RecalcRestBudgetMethod.ADD_TODAY)
                    },
                    text = stringResource(R.string.method_add_to_current_day_title),
                    description = stringResource(R.string.method_add_to_current_day_description),
                )
            }
        }
    }
}