package com.danilkinkin.buckwheat.finishPeriod

import androidx.activity.result.ActivityResultRegistryOwner
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
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
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
    val transactions by spendsViewModel.transactions.observeAsState(emptyList())
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val wholeBudget = spendsViewModel.budget.value!!
    val scrollState = rememberScrollState()

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
                        modifier = Modifier.padding(horizontal = 16.dp),
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

                    Icon(
                        modifier = Modifier
                            .requiredSize(256.dp)
                            .absoluteOffset(x = halfWidth * 0.7f, y = -halfHeight * 0.6f)
                            .zIndex(-1f),
                        painter = painterResource(R.drawable.shape_soft_star_1),
                        tint = starColor,
                        contentDescription = null,
                    )
                    Icon(
                        modifier = Modifier
                            .requiredSize(256.dp)
                            .absoluteOffset(x = -halfWidth * 0.7f, y = halfHeight * 0.6f)
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
                        Spacer(modifier = Modifier.height(36.dp))
                        SpendsCalendar(
                            modifier = Modifier.zIndex(-1f),
                            budget = wholeBudget,
                            transactions = transactions,
                            startDate = spendsViewModel.startPeriodDate.value!!,
                            finishDate = spendsViewModel.finishPeriodDate.value!!,
                            currency = spendsViewModel.currency.value!!,
                        )
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
            }

            val navigationBarHeight =
                WindowInsets.systemBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp)

            Spacer(Modifier.height(120.dp + navigationBarHeight))
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
                detached = false,//scrollState.maxValue - scrollState.value != 0,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            ) togetherWith fadeOut(
                snap(delayMillis = 250)
            )
        } else {
            fadeIn(
                snap()
            ) togetherWith fadeOut(
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
