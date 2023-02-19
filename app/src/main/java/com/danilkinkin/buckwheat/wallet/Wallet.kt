package com.danilkinkin.buckwheat.wallet

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import java.math.BigDecimal
import java.util.*


const val WALLET_SHEET = "wallet"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Wallet(
    forceChange: Boolean = false,
    windowSizeClass: WindowWidthSizeClass,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    var budget by remember { mutableStateOf(spendsViewModel.budget.value!!) }
    val dateToValue = remember { mutableStateOf(spendsViewModel.finishDate.value) }
    val currency by spendsViewModel.currency.observeAsState()
    val spends by spendsViewModel.getSpends().observeAsState()
    val restBudget =
        (spendsViewModel.budget.value!! - spendsViewModel.spent.value!! - spendsViewModel.spentFromDailyBudget.value!!)

    val openConfirmChangeBudgetDialog = remember { mutableStateOf(false) }

    if (spends === null) return

    val navigationBarHeight = WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)

    val isChange = (
            budget != spendsViewModel.budget.value
                    || dateToValue.value != spendsViewModel.finishDate.value
            )

    var isEdit by remember(spendsViewModel.startDate, spendsViewModel.finishDate, forceChange) {
        mutableStateOf(
            (spendsViewModel.finishDate.value !== null && isSameDay(
                spendsViewModel.startDate.value!!.time,
                spendsViewModel.finishDate.value!!.time
            ))
                    || forceChange
        )
    }

    val offset = with(LocalDensity.current) { 50.dp.toPx().toInt() }

    Surface {
        Column {
            val days = if (dateToValue.value !== null) {
                countDays(dateToValue.value!!)
            } else {
                0
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isChange || isEdit) {
                        stringResource(R.string.wallet_edit_title)
                    } else {
                        stringResource(R.string.wallet_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight)
            ) {
                AnimatedContent(
                    targetState = isEdit,
                    transitionSpec = {
                        if (targetState && !initialState) {
                            slideInHorizontally(
                                tween(durationMillis = 150)
                            ) { offset } + fadeIn(
                                tween(durationMillis = 150)
                            ) with slideOutHorizontally(
                                tween(durationMillis = 150)
                            ) { -offset } + fadeOut(
                                tween(durationMillis = 150)
                            )
                        } else {
                            slideInHorizontally(
                                tween(durationMillis = 150)
                            ) { -offset } + fadeIn(
                                tween(durationMillis = 150)
                            ) with slideOutHorizontally(
                                tween(durationMillis = 150)
                            ) { offset } + fadeOut(
                                tween(durationMillis = 150)
                            )
                        }.using(
                            SizeTransform(
                                clip = true,
                                sizeAnimationSpec = { _, _ -> tween(durationMillis = 350) }
                            )
                        )
                    }
                ) { targetIsEdit ->
                    if (targetIsEdit) {
                        BudgetConstructor(
                            onChange = { newBudget, finishDate ->
                                budget = newBudget
                                dateToValue.value = finishDate
                            }
                        )
                    } else {
                        BudgetSummary(
                            onEdit = {
                                isEdit = true
                            }
                        )
                    }
                }
                ButtonRow(
                    icon = painterResource(R.drawable.ic_currency),
                    text = stringResource(R.string.in_currency_label),
                    onClick = {
                        appViewModel.openSheet(PathState(CURRENCY_EDITOR))
                    },
                    endContent = {
                        Text(
                            text = when (currency?.type) {
                                CurrencyType.FROM_LIST -> "${Currency.getInstance(
                                    currency!!.value
                                ).displayName.titleCase()} (${Currency.getInstance(
                                    currency!!.value
                                ).symbol})"
                                CurrencyType.CUSTOM -> currency!!.value!!
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalContentColor.current.copy(alpha = 0.6f),
                        )
                    }
                )
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
                        val exportCSVLaunch = rememberExportCSV()

                        ButtonRow(
                            icon = painterResource(R.drawable.ic_file_download),
                            text = stringResource(R.string.export_to_csv),
                            onClick = { exportCSVLaunch() }
                        )
                    }
                }
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
                        Spacer(Modifier.height(24.dp))
                        Total(
                            budget = budget,
                            restBudget = restBudget,
                            days = days,
                            currency = currency!!,
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (spends!!.isNotEmpty() && !forceChange) {
                                    openConfirmChangeBudgetDialog.value = true
                                } else {
                                    spendsViewModel.changeCurrency(currency!!)
                                    spendsViewModel.changeBudget(budget, dateToValue.value!!)

                                    onClose()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            enabled = dateToValue.value !== null && countDays(dateToValue.value!!) > 0 && budget > BigDecimal(
                                0
                            )
                        ) {
                            Text(
                                text = if (spends!!.isNotEmpty() && !forceChange) {
                                    stringResource(R.string.change_budget)
                                } else {
                                    stringResource(R.string.apply)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (openConfirmChangeBudgetDialog.value) {
        ConfirmChangeBudgetDialog(
            windowSizeClass = windowSizeClass,
            onConfirm = {
                spendsViewModel.changeCurrency(currency!!)
                spendsViewModel.changeBudget(budget, dateToValue.value!!)

                onClose()
            },
            onClose = { openConfirmChangeBudgetDialog.value = false },
        )
    }
}

@Preview
@Composable
fun PreviewWallet() {
    BuckwheatTheme {
        Wallet(windowSizeClass = WindowWidthSizeClass.Compact)
    }
}