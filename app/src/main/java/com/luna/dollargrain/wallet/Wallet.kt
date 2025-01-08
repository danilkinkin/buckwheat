package com.luna.dollargrain.wallet

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.R
import com.luna.dollargrain.analytics.ANALYTICS_SHEET
import com.luna.dollargrain.base.ButtonRow
import com.luna.dollargrain.base.Divider
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.data.RestedBudgetDistributionMethod
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.di.TUTORS
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.countDaysToToday
import com.luna.dollargrain.util.isSameDay
import com.luna.dollargrain.util.titleCase
import java.math.BigDecimal
import java.util.Currency
import java.util.Date


const val WALLET_SHEET = "wallet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Wallet(
    forceChange: Boolean = false,
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    var budgetCache by remember { mutableStateOf(spendsViewModel.budget.value!!) }
    val budget by spendsViewModel.budget.observeAsState(BigDecimal.ZERO)
    val spent by spendsViewModel.spent.observeAsState(BigDecimal.ZERO)
    val spentFromDailyBudget by spendsViewModel.spentFromDailyBudget.observeAsState(BigDecimal.ZERO)
    val startPeriodDate by spendsViewModel.startPeriodDate.observeAsState(Date())
    val finishPeriodDate by spendsViewModel.finishPeriodDate.observeAsState(Date())
    val dateToValue = remember { mutableStateOf(spendsViewModel.finishPeriodDate.value) }
    val currency by spendsViewModel.currency.observeAsState()
    val spends by spendsViewModel.spends.observeAsState()
    val restedBudgetDistributionMethod by spendsViewModel.restedBudgetDistributionMethod.observeAsState()

    val restBudget =
        (budgetCache - spent - spentFromDailyBudget)

    val openConfirmFinishBudgetDialog = remember { mutableStateOf(false) }

    if (spends === null) return

    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)

    val isChange = (
            budgetCache != budget
                    || dateToValue.value != finishPeriodDate
            )

    var isEdit by remember(startPeriodDate, finishPeriodDate, forceChange) {
        mutableStateOf(
            (finishPeriodDate !== null && isSameDay(
                startPeriodDate.time,
                finishPeriodDate!!.time
            ))
                    || forceChange
        )
    }

    val offset = with(LocalDensity.current) { 50.dp.toPx().toInt() }


    ModalBottomSheet(
        onDismissRequest = {
            openConfirmFinishBudgetDialog.value = false
        },
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha=0.3f)
    ) {
        Column {
            val days = if (dateToValue.value !== null) {
                countDaysToToday(dateToValue.value!!)
            } else {
                0
            }

            // top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // arrow back from edit screen
                if (!forceChange && isEdit) {
                    IconButton(
                        onClick = { isEdit = false },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
                Spacer(Modifier.weight(1F))
                Text(
                    text = if (isChange || isEdit) {
                        "set up a budget"
                    } else {
                        "budget"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1F))
                if (!isEdit) {
                    IconButton(
                        onClick = { isEdit = true },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight)
            ) {
                // text input
                AnimatedContent(
                    targetState = isEdit,
                    transitionSpec = {
                        if (targetState && !initialState) {
                            (slideInHorizontally(
                                tween(durationMillis = 150)
                            ) { offset } + fadeIn(
                                tween(durationMillis = 150)
                            )).togetherWith(slideOutHorizontally(
                                tween(durationMillis = 150)
                            ) { -offset } + fadeOut(
                                tween(durationMillis = 150)
                            ))
                        } else {
                            (slideInHorizontally(
                                tween(durationMillis = 150)
                            ) { -offset } + fadeIn(
                                tween(durationMillis = 150)
                            )).togetherWith(slideOutHorizontally(
                                tween(durationMillis = 150)
                            ) { offset } + fadeOut(
                                tween(durationMillis = 150)
                            ))
                        }.using(
                            SizeTransform(
                                clip = true,
                                sizeAnimationSpec = { _, _ -> tween(durationMillis = 350) }
                            )
                        )
                    },
                    label = ""
                ) { targetIsEdit ->
                    if (targetIsEdit) {
                        // text input
                        BudgetConstructor(
                            forceChange = forceChange,
                            onChange = { newBudget, finishDate ->
                                budgetCache = newBudget
                                dateToValue.value = finishDate
                            }
                        )
                    } else {
                        // nice stats widget thing
                        BudgetSummary(
                            onEdit = {
                                isEdit = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }

                // distribution method
                ButtonRow(
                    icon = painterResource(R.drawable.ic_directions),
                    text = "excess savings?",
                    onClick = {
                        appViewModel.openSheet(PathState(DEFAULT_RECALC_BUDGET_CHOOSER))
                    },
                    endCaption = when (restedBudgetDistributionMethod) {
                        RestedBudgetDistributionMethod.ASK, null -> "always ask"
                        RestedBudgetDistributionMethod.REST -> "distribute"
                        RestedBudgetDistributionMethod.ADD_TODAY -> "add to the next day's budget"
                        RestedBudgetDistributionMethod.ADD_SAVINGS -> "add to savings"
                    },
                )

                // currency selection
                ButtonRow(
                    icon = painterResource(R.drawable.ic_currency),
                    text = "currency",
                    onClick = {
                        appViewModel.openSheet(PathState(CURRENCY_EDITOR))
                    },
                    endCaption = when (currency?.type) {
                        ExtendCurrency.Type.FROM_LIST -> "${
                            Currency.getInstance(
                                currency!!.value
                            ).displayName.titleCase()
                        } (${
                            Currency.getInstance(
                                currency!!.value
                            ).symbol
                        })"
                        ExtendCurrency.Type.CUSTOM -> currency!!.value!!
                        else -> ""
                    },
                )

                // analytics
                AnimatedVisibility(
                    visible = !isChange && !isEdit,
                    enter = fadeIn(
                        tween(durationMillis = 350)
                    ) + expandVertically(
                        expandFrom = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                    exit = fadeOut(
                        tween(durationMillis = 350)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                ) {
                    Column {
                        if (spends!!.isNotEmpty()) {
                            ButtonRow(
                                icon = painterResource(R.drawable.ic_analytics),
                                text = "view analytics",
                                onClick = {
                                    appViewModel.openSheet(PathState(ANALYTICS_SHEET))
                                }
                            )
                        }

                        val exportCSVLaunch = rememberExportCSV(
                            activityResultRegistryOwner = activityResultRegistryOwner
                        )

                        ButtonRow(
                            icon = painterResource(R.drawable.ic_file_download),
                            text = "export to csv",
                            onClick = { exportCSVLaunch() }
                        )

                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.error
                        ) {
                            ButtonRow(
                                icon = painterResource(R.drawable.ic_close),
                                text = "finish early",
                                onClick = {
                                    openConfirmFinishBudgetDialog.value = true
                                }
                            )
                        }
                    }
                }

                // edit budget screen (after pencil icon is pressed)
                AnimatedVisibility(
                    visible = isChange || isEdit,
                    enter = fadeIn(
                        tween(durationMillis = 350)
                    ) + expandVertically(
                        expandFrom = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                    exit = fadeOut(
                        tween(durationMillis = 350)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                ) {
                    Column {
                        Divider()
                        Total(
                            budget = budgetCache,
                            restBudget = restBudget,
                            days = days,
                            currency = currency!!,
                        )
                        Button(
                            onClick = {
                                spendsViewModel.changeDisplayCurrency(currency!!)

                                if (spends!!.isNotEmpty() && !forceChange) {
                                    spendsViewModel.changeBudget(budgetCache, dateToValue.value!!)
                                } else {
                                    spendsViewModel.setBudget(budgetCache, dateToValue.value!!)
                                    appViewModel.activateTutorial(TUTORS.OPEN_WALLET)
                                }

                                onClose()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(60.dp)
                                .padding(horizontal = 16.dp),
                            enabled = dateToValue.value !== null && countDaysToToday(dateToValue.value!!) > 0 && budgetCache > BigDecimal(
                                0
                            )
                        ) {
                            Text(
                                text = if (spends!!.isNotEmpty() && !forceChange) {
                                    "change budget"
                                } else {
                                    "apply"
                                },
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
        if (openConfirmFinishBudgetDialog.value) {
            ConfirmFinishEarlyDialog(
                onConfirm = {
                    spendsViewModel.finishBudget()

                    onClose()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onClose = { openConfirmFinishBudgetDialog.value = false },
            )
        }
    }
}

@Preview
@Composable
fun PreviewWallet() {
    DollargrainTheme {
        Wallet()
    }
}
