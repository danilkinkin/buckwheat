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
import androidx.compose.runtime.LaunchedEffect
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
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.TransactionType
import java.math.BigDecimal
import java.util.*
import com.danilkinkin.buckwheat.analytics.categoriesChart.CategoriesChartCard
import com.danilkinkin.buckwheat.analytics.categoriesChart.SpendCategoriesCard
import com.danilkinkin.buckwheat.data.categories.SpendCategoriesViewModel
import com.danilkinkin.buckwheat.patterns.PATTERN_INSIGHTS_SHEET
import com.danilkinkin.buckwheat.settings.CategoriesManagementViewModel
import com.danilkinkin.buckwheat.settings.CategoryCapsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.countDaysToToday
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
    val transactions by spendsViewModel.periodTransactions.observeAsState(emptyList())
    val spends by spendsViewModel.periodSpends.observeAsState(emptyList())
    val wholeBudget by spendsViewModel.budget.observeAsState(BigDecimal.ZERO)
    val dailyBudget by spendsViewModel.dailyBudget.observeAsState(BigDecimal.ZERO)
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val startPeriodDate by spendsViewModel.startPeriodDate.observeAsState(Date())
    val finishPeriodDate by spendsViewModel.finishPeriodDate.observeAsState(Date())
    val budgetPeriods by spendsViewModel.budgetPeriods.observeAsState(emptyList())
    val archivedTransactions by spendsViewModel.archivedTransactions.observeAsState(emptyList())
    val scrollState = rememberScrollState()

    val previousPeriod = remember(budgetPeriods, startPeriodDate) {
        findPreviousPeriod(budgetPeriods, startPeriodDate)
    }
    val elapsedDays = remember(startPeriodDate) {
        countDaysToToday(startPeriodDate).coerceAtLeast(1)
    }
    val currentSpent = remember(spends) {
        spends.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.value }
    }

    val spendCategoriesViewModel: SpendCategoriesViewModel = hiltViewModel()
    val isCategorizing by spendCategoriesViewModel.isCategorizing.observeAsState(false)

    val categoriesViewModel: CategoriesManagementViewModel = hiltViewModel()
    val allCategories by categoriesViewModel.allCategories.observeAsState(emptyList())
    val categoryEmojis = remember(allCategories) {
        allCategories.associate { it.name to it.emoji }
    }

    val categoryCapsViewModel: CategoryCapsViewModel = hiltViewModel()
    val categoryCaps by categoryCapsViewModel.caps.observeAsState(emptyMap())

    LaunchedEffect(spends) {
        spendCategoriesViewModel.categorizeUncategorized()
    }

    val finishPeriodActualDate by spendsViewModel.finishPeriodActualDate.observeAsState(null)

    // Need to hide calendar after migration to transactions,
    // because after migration can't restore some transactions like INCOME & SET_DAILY_BUDGET
    val afterMigrationToTransactions =
        remember(transactions) { mutableStateOf(transactions.none { it.type == TransactionType.INCOME }) }


    val navigationBarHeight =
        LocalWindowInsets.current.calculateBottomPadding().coerceAtLeast(16.dp)

    Surface(Modifier.height(IntrinsicSize.Min)) {
        Column {
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
                            currency = currency,
                            startDate = startPeriodDate,
                            finishDate = finishPeriodDate,
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
                                        startDate = startPeriodDate,
                                        finishDate = finishPeriodDate,
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
                                    currency = currency,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                MinMaxSpentCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isMin = false,
                                    spends = spends,
                                    currency = currency,
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
                                    startDate = startPeriodDate,
                                    finishDate = finishPeriodDate!!,
                                    actualFinishDate = finishPeriodActualDate,
                                    currency = currency,
                                    dailyBudget = dailyBudget,
                                    onDayClick = { date ->
                                        appViewModel.openSheet(
                                            PathState(
                                                name = VIEWER_HISTORY_SHEET,
                                                args = mapOf("onlyDay" to date),
                                            )
                                        )
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.height(36.dp))
                            SpendCategoriesCard(
                                modifier = Modifier.fillMaxWidth(),
                                spends = spends,
                                currency = currency,
                                isCategorizing = isCategorizing,
                                categoryEmojis = categoryEmojis,
                                caps = categoryCaps,
                                onCategoryClick = { key ->
                                    appViewModel.openSheet(
                                        PathState(
                                            name = CATEGORY_HISTORY_SHEET,
                                            args = mapOf("onlyCategoryKey" to key),
                                        )
                                    )
                                },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CategoriesChartCard(
                                modifier = Modifier.fillMaxWidth(),
                                spends = spends,
                                currency = currency,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SpendsTrendCard(
                                modifier = Modifier.fillMaxWidth(),
                                spends = spends,
                                startDate = startPeriodDate,
                                finishDate = finishPeriodDate!!,
                                currency = currency,
                                periods = budgetPeriods,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SpendsWeekdayCard(
                                modifier = Modifier.fillMaxWidth(),
                                spends = spends,
                                startDate = startPeriodDate,
                                finishDate = finishPeriodDate!!,
                                currency = currency,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CompareToLastPeriodCard(
                                modifier = Modifier.fillMaxWidth(),
                                currentSpent = currentSpent,
                                archivedTransactions = archivedTransactions,
                                previousPeriod = previousPeriod,
                                elapsedDays = elapsedDays,
                                currency = currency,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                if (spends.isNotEmpty()) {
                    val exportCSVLaunch = rememberExportCSV(
                        activityResultRegistryOwner = activityResultRegistryOwner
                    )

                    val shareSummaryLaunch = rememberShareSummary(
                        appViewModel = appViewModel,
                        spendsViewModel = spendsViewModel,
                        categoryEmojis = categoryEmojis,
                    )

                    ButtonRow(
                        icon = painterResource(R.drawable.ic_equalizer),
                        text = stringResource(R.string.patterns_analyze_button),
                        onClick = {
                            appViewModel.openSheet(PathState(PATTERN_INSIGHTS_SHEET))
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ButtonRow(
                        icon = painterResource(R.drawable.ic_file_download),
                        text = stringResource(R.string.export_to_csv),
                        onClick = { exportCSVLaunch() },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ButtonRow(
                        icon = painterResource(R.drawable.ic_share),
                        text = stringResource(R.string.share_summary),
                        onClick = { shareSummaryLaunch() },
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
