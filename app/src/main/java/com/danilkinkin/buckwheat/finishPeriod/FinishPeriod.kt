package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.animation.*
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.wallet.rememberExportCSV

const val FINISH_PERIOD_SHEET = "finishPeriod"

@Composable
fun FinishPeriod(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onCreateNewPeriod: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val spends by spendsViewModel.getSpends().observeAsState(initial = emptyList())
    val wholeBudget = spendsViewModel.budget.value!!
    val restBudget =
        (spendsViewModel.budget.value!! - spendsViewModel.spent.value!! - spendsViewModel.spentFromDailyBudget.value!!)
    val scrollState = rememberScrollState()

    Surface(Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.finish_period_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
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
                Spacer(Modifier.height(24.dp))
                Column(Modifier.fillMaxWidth()) {
                    WholeBudgetCard(
                        budget = wholeBudget,
                        currency = spendsViewModel.currency.value!!,
                        startDate = spendsViewModel.startDate.value!!,
                        finishDate = spendsViewModel.finishDate.value!!,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (spends.isNotEmpty()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            RestAndSpentBudgetCard(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(16.dp))
                            FillCircleStub()
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth()) {
                            MinMaxSpentCard(
                                modifier = Modifier.weight(1f),
                                isMin = true,
                                spends = spends,
                                currency = spendsViewModel.currency.value!!,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            MinMaxSpentCard(
                                modifier = Modifier.weight(1f),
                                isMin = false,
                                spends = spends,
                                currency = spendsViewModel.currency.value!!,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth()) {
                            SpendsCountCard(
                                modifier = Modifier,
                                count = spends.size,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            AverageSpendCard(
                                modifier = Modifier.weight(1f),
                                spends = spends,
                                currency = spendsViewModel.currency.value!!,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OverspendingInfoCard(
                            modifier = Modifier.fillMaxWidth(),
                            budget = wholeBudget,
                            spends = spends,
                            startDate = spendsViewModel.startDate.value!!,
                            finishDate = spendsViewModel.finishDate.value!!,
                            currency = spendsViewModel.currency.value!!,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (spends.isNotEmpty()) {
                val exportCSVLaunch = rememberExportCSV()

                ButtonRow(
                    icon = painterResource(R.drawable.ic_file_download),
                    text = stringResource(R.string.export_to_csv),
                    onClick = { exportCSVLaunch() },
                )
            }

            Spacer(Modifier.height(120.dp))
        }

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Footer(
                title = { Text(stringResource(R.string.new_period_title)) },
                onClick = {
                    onCreateNewPeriod()
                    onClose()
                },
                detached = scrollState.maxValue - scrollState.value != 0,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun Footer(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    detached: Boolean,
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    )
    val content = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .weight(weight = 1F, fill = true)
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                    title()
                }
            }
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .padding(end = 8.dp),
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
            )
        }
    }

    val navigationBarHeight =
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp)

    AnimatedContent(targetState = detached, transitionSpec = {
        if (targetState && !initialState) {
            fadeIn(
                tween(durationMillis = 250)
            ) with fadeOut(
                snap(delayMillis = 250)
            )
        } else {
            fadeIn(
                snap()
            ) with fadeOut(
                tween(durationMillis = 250)
            )
        }.using(
            SizeTransform(clip = false)
        )
    }) { targetDetached ->
        if (targetDetached) {
            Card(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = colors,
            ) {
                Box(Modifier.padding(start = 16.dp, end = 16.dp, bottom = navigationBarHeight)) {
                    content()
                }
            }
        } else {
            Card(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = navigationBarHeight),
                shape = MaterialTheme.shapes.extraLarge,
                colors = colors,
            ) {
                content()
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