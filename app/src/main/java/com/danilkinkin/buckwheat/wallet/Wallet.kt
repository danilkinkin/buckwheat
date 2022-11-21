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
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.base.Divider
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
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    var budget by remember { mutableStateOf(spendsViewModel.budget.value!!) }
    val dateToValue = remember { mutableStateOf(spendsViewModel.finishDate.value) }
    var currency by remember { mutableStateOf(spendsViewModel.currency.value!!) }
    val spends by spendsViewModel.getSpends().observeAsState()
    val restBudget =
        (spendsViewModel.budget.value!! - spendsViewModel.spent.value!! - spendsViewModel.spentFromDailyBudget.value!!)

    val openCurrencyChooserDialog = remember { mutableStateOf(false) }
    val openCustomCurrencyEditorDialog = remember { mutableStateOf(false) }
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
            (spendsViewModel.finishDate.value !== null && isSameDay(spendsViewModel.startDate.value!!.time, spendsViewModel.finishDate.value!!.time))
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
            Divider()
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
                Divider()
                TextRow(
                    icon = painterResource(R.drawable.ic_currency),
                    text = stringResource(R.string.in_currency_label),
                )
                CheckedRow(
                    checked = currency.type === CurrencyType.FROM_LIST,
                    onValueChange = { openCurrencyChooserDialog.value = true },
                    text = if (currency.type !== CurrencyType.FROM_LIST) {
                        stringResource(R.string.currency_from_list)
                    } else {
                        "${stringResource(R.string.currency_from_list)} (${Currency.getInstance(currency.value).symbol})"
                    },
                )
                CheckedRow(
                    checked = currency.type === CurrencyType.CUSTOM,
                    onValueChange = { openCustomCurrencyEditorDialog.value = true },
                    text = if (currency.type !== CurrencyType.CUSTOM) {
                        stringResource(R.string.currency_custom)
                    } else {
                        "${stringResource(R.string.currency_custom)} (${currency.value!!})"
                    },
                )
                CheckedRow(
                    checked = currency.type === CurrencyType.NONE,
                    onValueChange = {
                        if (it) {
                            currency = ExtendCurrency(type = CurrencyType.NONE)

                            spendsViewModel.changeCurrency(currency)
                        }
                    },
                    text = stringResource(R.string.currency_none),
                )
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
                            currency = currency,
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (spends!!.isNotEmpty() && !forceChange) {
                                    openConfirmChangeBudgetDialog.value = true
                                } else {
                                    spendsViewModel.changeCurrency(currency)
                                    spendsViewModel.changeBudget(budget, dateToValue.value!!)

                                    onClose()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            enabled = dateToValue.value !== null && countDays(dateToValue.value!!) > 0 && budget > BigDecimal(0)
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

    if (openCurrencyChooserDialog.value) {
        WorldCurrencyChooser(
            windowSizeClass = windowSizeClass,
            defaultCurrency = if (currency.type === CurrencyType.FROM_LIST) {
                Currency.getInstance(currency.value)
            } else {
                null
            },
            onSelect = {
                currency = ExtendCurrency(type = CurrencyType.FROM_LIST, value = it.currencyCode)

                spendsViewModel.changeCurrency(currency)
            },
            onClose = { openCurrencyChooserDialog.value = false },
        )
    }

    if (openCustomCurrencyEditorDialog.value) {
        CustomCurrencyEditor(
            windowSizeClass = windowSizeClass,
            defaultCurrency = if (currency.type === CurrencyType.CUSTOM) {
                currency.value
            } else {
                null
            },
            onChange = {
                currency = ExtendCurrency(type = CurrencyType.CUSTOM, value = it)

                spendsViewModel.changeCurrency(currency)
            },
            onClose = { openCustomCurrencyEditorDialog.value = false },
        )
    }

    if (openConfirmChangeBudgetDialog.value) {
        ConfirmChangeBudgetDialog(
            windowSizeClass = windowSizeClass,
            onConfirm = {
                spendsViewModel.changeCurrency(currency)
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