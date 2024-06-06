package com.danilkinkin.buckwheat.analytics

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.analytics.categoriesChart.CategoriesChartCard
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.wallet.DaysLeftCard
import com.danilkinkin.buckwheat.wallet.rememberExportCSV

const val ANALYTICS_SHEET = "finishPeriod"

data class Size(val width: Dp, val height: Dp)

@Composable
fun Analytics(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    onCreateNewPeriod: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val periodFinished by spendsViewModel.periodFinished.observeAsState(false)
    val transactions by spendsViewModel.transactions.observeAsState(emptyList())
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val wholeBudget = spendsViewModel.budget.value!!
    val scrollState = rememberScrollState()

    val finishPeriodActualDate by spendsViewModel.finishPeriodActualDate.observeAsState(null)

    // Need to hide calendar after migration to transactions,
    // because after migration can't restore some transactions like INCOME & SET_DAILY_BUDGET
    val afterMigrationToTransactions =
        remember(transactions) { mutableStateOf(transactions.none { it.type == TransactionType.INCOME }) }


    val navigationBarHeight =
        LocalWindowInsets.current.calculateBottomPadding().coerceAtLeast(16.dp)

    Surface(Modifier.height(IntrinsicSize.Min)) {
        Column(
        ) {
            if (!periodFinished) {
                MiddlePeriodAnalyticsHeader(
                    onClose = onClose,
                )
            }
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (periodFinished) {
                        FinishedPeriodHeader(
                            scrollState = scrollState,
                            hasSpends = spends.isNotEmpty(),
                        )
                    }
                    Column(Modifier.fillMaxWidth()) {
                        WholeBudgetCard(
                            budget = wholeBudget,
                            currency = spendsViewModel.currency.value!!,
                            startDate = spendsViewModel.startPeriodDate.value!!,
                            finishDate = spendsViewModel.finishPeriodDate.value!!,
                            actualFinishDate = finishPeriodActualDate,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (spends.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                            ) {
                                RestAndSpentBudgetCard(modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(16.dp))
                                if (periodFinished) {
                                    FillCircleStub()
                                } else {
                                    DaysLeftCard(
                                        startDate = spendsViewModel.startPeriodDate.value!!,
                                        finishDate = spendsViewModel.finishPeriodDate.value!!,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                            ) {
                                MinMaxSpentCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isMin = true,
                                    spends = spends,
                                    currency = spendsViewModel.currency.value!!,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                MinMaxSpentCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isMin = false,
                                    spends = spends,
                                    currency = spendsViewModel.currency.value!!,
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            SpendsCountCard(
                                modifier = Modifier.fillMaxWidth(),
                                count = spends.size,
                            )
                            if (!afterMigrationToTransactions.value) {
                                Spacer(modifier = Modifier.height(36.dp))
                                SpendsCalendar(
                                    modifier = Modifier.zIndex(-1f),
                                    budget = wholeBudget,
                                    transactions = transactions,
                                    startDate = spendsViewModel.startPeriodDate.value!!,
                                    finishDate = spendsViewModel.finishPeriodDate.value!!,
                                    currency = spendsViewModel.currency.value!!,
                                )
                            }
                            Spacer(modifier = Modifier.height(36.dp))
                            CategoriesChartCard(
                                modifier = Modifier.fillMaxWidth(),
                                spends = spends,
                                currency = spendsViewModel.currency.value!!,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                if (spends.isNotEmpty()) {
                    val exportCSVLaunch = rememberExportCSV(
                        activityResultRegistryOwner = activityResultRegistryOwner
                    )

                    ButtonRow(
                        icon = painterResource(R.drawable.ic_file_download),
                        text = stringResource(R.string.export_to_csv),
                        onClick = { exportCSVLaunch() },
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (periodFinished) {
                    Spacer(
                        Modifier
                            .height(60.dp + navigationBarHeight)
                            .fillMaxWidth()
                    )
                }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navigationBarHeight),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (periodFinished) {
                Button(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(60.dp)
                    .padding(horizontal = 16.dp),
                    onClick = {
                        onCreateNewPeriod()
                        onClose()
                    }) {
                    Text(
                        text = stringResource(R.string.new_period_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_forward),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Analytics()
    }
}
