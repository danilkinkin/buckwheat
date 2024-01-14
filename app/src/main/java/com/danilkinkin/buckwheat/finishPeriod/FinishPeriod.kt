package com.danilkinkin.buckwheat.finishPeriod

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.finishPeriod.categoriesChart.CategoriesChartCard
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.wallet.rememberExportCSV

const val FINISH_PERIOD_SHEET = "finishPeriod"

data class Size(val width: Dp, val height: Dp)

@Composable
fun FinishPeriod(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    onCreateNewPeriod: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val localDensity = LocalDensity.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    val transactions by spendsViewModel.transactions.observeAsState(emptyList())
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val wholeBudget = spendsViewModel.budget.value!!
    val scrollState = rememberScrollState()
    // Need to hide calendar after migration to transactions,
    // because after migration can't restore some transactions like INCOME & SET_DAILY_BUDGET
    val afterMigrationToTransactions = remember(transactions) { mutableStateOf(transactions.none { it.type == TransactionType.INCOME }) }

    val scroll = with(localDensity) { scrollState.value.toDp() }

    val navigationBarHeight =
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp)

    Surface(Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                var headerSize by remember { mutableStateOf(Size(0.dp, 0.dp)) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = localBottomSheetScrollState.topPadding.coerceAtLeast(36.dp))
                        .onGloballyPositioned {
                            headerSize = Size(
                                width = with(localDensity) { it.size.width.toDp() },
                                height = with(localDensity) { it.size.height.toDp() }
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val halfWidth = headerSize.width / 2
                    val halfHeight = headerSize.height / 2

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .absoluteOffset(y = scroll * 0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(36.dp))
                        Text(
                            text = stringResource(R.string.finish_period_title),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        if (spends.isEmpty()) {
                            Text(
                                text = stringResource(R.string.period_summary_no_spends_title),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.period_summary_title),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(Modifier.height(64.dp))
                    }

                    val starColor = combineColors(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.surface,
                        0.5f,
                    )

                    val angleStar1 by rememberInfiniteTransition("angleStar1").animateFloat(
                        label = "angleStar1",
                        initialValue = -20f,
                        targetValue = 20f,
                        animationSpec = infiniteRepeatable(tween(10000), RepeatMode.Reverse)
                    )

                    val angleStar2 by rememberInfiniteTransition("angleStar2").animateFloat(
                        label = "angleStar2",
                        initialValue = -50f,
                        targetValue = 50f,
                        animationSpec = infiniteRepeatable(tween(18000), RepeatMode.Reverse)
                    )

                    Icon(
                        modifier = Modifier
                            .requiredSize(256.dp)
                            .absoluteOffset(x = halfWidth * 0.7f, y = -halfHeight * 0.6f + scroll * 0.35f)
                            .rotate(angleStar1)
                            .zIndex(-1f),
                        painter = painterResource(R.drawable.shape_soft_star_1),
                        tint = starColor,
                        contentDescription = null,
                    )
                    Icon(
                        modifier = Modifier
                            .requiredSize(256.dp)
                            .absoluteOffset(x = -halfWidth * 0.7f, y = halfHeight * 0.6f + scroll * 0.6f)
                            .rotate(angleStar2)
                            .zIndex(-1f),
                        painter = painterResource(R.drawable.shape_soft_star_2),
                        tint = starColor,
                        contentDescription = null,
                    )
                }
                Column(Modifier.fillMaxWidth()) {
                    WholeBudgetCard(
                        budget = wholeBudget,
                        currency = spendsViewModel.currency.value!!,
                        startDate = spendsViewModel.startPeriodDate.value!!,
                        finishDate = spendsViewModel.finishPeriodDate.value!!,
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
                            FillCircleStub()
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


            Spacer(Modifier.height(60.dp + navigationBarHeight).fillMaxWidth())
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = navigationBarHeight),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(60.dp)
                    .padding(horizontal = 16.dp),
                onClick = {
                    onCreateNewPeriod()
                    onClose()
                }
            ) {
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

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        FinishPeriod()
    }
}
