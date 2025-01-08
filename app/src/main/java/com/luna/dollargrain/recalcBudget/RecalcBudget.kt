package com.luna.dollargrain.recalcBudget

import android.graphics.PointF
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.base.ButtonRow
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.RestedBudgetDistributionMethod
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.numberFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

const val RECALCULATE_DAILY_BUDGET_SHEET = "recalculateDailyBudget"

@Composable
fun RecalcBudget(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    recalcBudgetViewModel: RecalcBudgetViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val localDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding().coerceAtLeast(16.dp)

    val howMuchNotSpent by recalcBudgetViewModel.howMuchNotSpent.observeAsState(BigDecimal.ZERO)
    val isLastDay by recalcBudgetViewModel.isLastDay.observeAsState(false)

    var rememberChoice by remember { mutableStateOf(false) }
    var contentHeight by remember { mutableFloatStateOf(0f) }
    var isSpawned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        recalcBudgetViewModel.calculate()
    }

    BoxWithConstraints(Modifier.fillMaxWidth().padding(top = localBottomSheetScrollState.topPadding)) {
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
                        text = "yay!! you saved ",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = numberFormat(
                            context,
                            howMuchNotSpent,
                            currency = spendsViewModel.currency.value!!,
                        ),
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    if (isLastDay) {
                        Text(
                            text = "today is the last day, distributing does nothing",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(
                            text = "find out how to budget",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(48.dp))
                }
                if (!isLastDay) {
                    ButtonRow(
                        text = "remember this choice",
                        description = "dw you can change it later <3",
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
                }
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isLastDay) {
                        StartLastDayButton {
                            onClose()
                        }
                        Spacer(Modifier.height(24.dp))
                    } else {
                        SplitToRestDaysButton {
                            if (rememberChoice) spendsViewModel.changeRestedBudgetDistributionMethod(
                                RestedBudgetDistributionMethod.REST
                            )

                            onClose()
                        }
                        Spacer(Modifier.height(16.dp))
                        AddToTodayButton {
                            if (rememberChoice) spendsViewModel.changeRestedBudgetDistributionMethod(
                                RestedBudgetDistributionMethod.ADD_TODAY
                            )

                            onClose()
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewRecalcBudget() {
    DollargrainTheme {
        RecalcBudget()
    }
}