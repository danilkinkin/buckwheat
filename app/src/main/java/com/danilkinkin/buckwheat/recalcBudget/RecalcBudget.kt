package com.danilkinkin.buckwheat.recalcBudget

import android.graphics.PointF
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.DescriptionButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.RestedBudgetDistributionMethod
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val RECALCULATE_DAILY_BUDGET_SHEET = "recalculateDailyBudget"

@Composable
fun RecalcBudget(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val localDensity = LocalDensity.current
    val isDebug = appViewModel.isDebug.observeAsState(false)
    val coroutineScope = rememberCoroutineScope()

    var rememberChoice by remember { mutableStateOf(false) }
    val restDays = countDaysToToday(spendsViewModel.finishDate.value!!)

    val restBudget =
        (spendsViewModel.budget.value!! - spendsViewModel.spent.value!!) - spendsViewModel.dailyBudget.value!!

    val requireDistributeBudget = spendsViewModel.howMuchNotSpent()
    val budgetPerDaySplit = spendsViewModel.whatBudgetForDay()
    val budgetPerDayAdd = spendsViewModel.whatBudgetForDay(excludeCurrentDay = true)
    val budgetPerDayAddDailyBudget = budgetPerDayAdd + requireDistributeBudget

    val navigationBarHeight = WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)

    var contentHeight by remember { mutableStateOf(0f) }
    var isSpawned by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val rootHeight = constraints.maxHeight.toFloat()

        Surface(
            Modifier.onGloballyPositioned {
                contentHeight = it.size.height.toFloat() - with(localDensity) { 90.dp.toPx() }
            }
        ) {
            DisposableEffect(contentHeight) {
                if (contentHeight != 0f && !isSpawned) {
                    isSpawned = true

                    val top = (rootHeight - contentHeight).coerceAtLeast(rootHeight * 0.3f)

                    coroutineScope.launch {
                        delay(300)

                        appViewModel.confettiController.spawn(
                            ejectPoint = PointF(constraints.maxWidth.toFloat(), top),
                            ejectVector = PointF(-100f, -100f),
                            ejectAngle = 140,
                            ejectForceCoefficient = 7f,
                            count = 120 to 150,
                            lifetime = Pair(1000L, 3000L),
                        )

                        appViewModel.confettiController.spawn(
                            ejectPoint = PointF(0f, top),
                            ejectVector = PointF(100f, -100f),
                            ejectAngle = 140,
                            ejectForceCoefficient = 7f,
                            count = 120 to 150,
                            lifetime = Pair(1000L, 3000L),
                        )

                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                onDispose {}
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.new_day_title),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = prettyCandyCanes(
                            requireDistributeBudget,
                            currency = spendsViewModel.currency.value!!,
                        ),
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.recalc_budget),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(48.dp))
                }
                ButtonRow(
                    text = stringResource(R.string.remember_choice),
                    description = stringResource(R.string.remember_choice_reacalc_budget_description),
                    wrapMainText = true,
                    iconInset = false,
                    onClick = {
                        rememberChoice = !rememberChoice
                    },
                    endContent = {
                        Switch(
                            checked = rememberChoice,
                            onCheckedChange = {
                                rememberChoice = !rememberChoice
                            },
                        )

                    }
                )
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DescriptionButton(
                        title = { Text(stringResource(R.string.split_rest_days_title)) },
                        description = {
                            Text(
                                stringResource(
                                    R.string.split_rest_days_description,
                                    prettyCandyCanes(
                                        budgetPerDaySplit,
                                        currency = spendsViewModel.currency.value!!,
                                    ),
                                )
                            )
                        },
                        secondDescription = if (isDebug.value) {
                            { Text("($restBudget + ${spendsViewModel.dailyBudget.value!!} - ${spendsViewModel.spentFromDailyBudget.value!!}) / $restDays = $budgetPerDaySplit") }
                        } else null,
                        onClick = {
                            spendsViewModel.setDailyBudget(budgetPerDaySplit)
                            if (rememberChoice) spendsViewModel.changeRestedBudgetDistributionMethod(
                                RestedBudgetDistributionMethod.REST
                            )

                            onClose()
                        },
                    )
                    Spacer(Modifier.height(16.dp))
                    DescriptionButton(
                        title = { Text(stringResource(R.string.add_current_day_title)) },
                        description = {
                            Text(
                                stringResource(
                                    R.string.add_current_day_description,
                                    prettyCandyCanes(
                                        requireDistributeBudget + budgetPerDayAdd,
                                        currency = spendsViewModel.currency.value!!,
                                    ),
                                    prettyCandyCanes(
                                        budgetPerDayAdd,
                                        currency = spendsViewModel.currency.value!!,
                                    ),
                                )
                            )
                        },
                        secondDescription = if (isDebug.value) {
                            {
                                Text(
                                    "$restBudget / $restDays = $budgetPerDayAdd " +
                                            "\n${budgetPerDayAdd} + $requireDistributeBudget = $budgetPerDayAddDailyBudget"
                                )
                            }
                        } else null,
                        onClick = {
                            spendsViewModel.setDailyBudget(budgetPerDayAdd + requireDistributeBudget)
                            if (rememberChoice) spendsViewModel.changeRestedBudgetDistributionMethod(
                                RestedBudgetDistributionMethod.ADD_TODAY
                            )

                            onClose()
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewRecalcBudget() {
    BuckwheatTheme {
        RecalcBudget()
    }
}