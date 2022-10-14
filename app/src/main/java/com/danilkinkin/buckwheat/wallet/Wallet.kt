package com.danilkinkin.buckwheat.wallet

import androidx.compose.animation.*
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.viewpager.widget.ViewPager
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.TextWithLabel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun Wallet(
    forceChange: Boolean = false,
    requestFinishDate: ((presetDate: Date, callback: (finishDate: Date) -> Unit) -> Unit) = { _: Date, _: (finishDate: Date) -> Unit -> },
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    var budget by remember { mutableStateOf(spendsViewModel.budget.value!!) }
    val dateToValue = remember { mutableStateOf(spendsViewModel.finishDate) }
    var currency by remember { mutableStateOf(spendsViewModel.currency) }
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
                    || dateToValue.value != spendsViewModel.finishDate
            )

    var isEdit by remember(spendsViewModel.startDate, spendsViewModel.finishDate, forceChange) {
        mutableStateOf(
            isSameDay(spendsViewModel.startDate.time, spendsViewModel.finishDate.time)
                    || forceChange
        )
    }

    val offset = with(LocalDensity.current) { 50.dp.toPx().toInt() }

    Surface {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            val days = countDays(dateToValue.value)

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
                        requestFinishDate = requestFinishDate,
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
            Column(Modifier.verticalScroll(rememberScrollState())) {
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
                        stringResource(
                            id = R.string.currency_from_list_selected,
                            Currency.getInstance(currency.value).symbol
                        )
                    },
                )
                CheckedRow(
                    checked = currency.type === CurrencyType.CUSTOM,
                    onValueChange = { openCustomCurrencyEditorDialog.value = true },
                    text = if (currency.type !== CurrencyType.CUSTOM) {
                        stringResource(R.string.currency_custom)
                    } else {
                        stringResource(
                            id = R.string.currency_custom_selected,
                            currency.value!!
                        )
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
                                    spendsViewModel.changeBudget(budget, dateToValue.value)

                                    onClose()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            enabled = countDays(dateToValue.value) > 0 && budget > BigDecimal(0)
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
            onConfirm = {
                spendsViewModel.changeCurrency(currency)
                spendsViewModel.changeBudget(budget, dateToValue.value)

                onClose()
            },
            onClose = { openConfirmChangeBudgetDialog.value = false },
        )
    }
}

@Composable
fun Total(
    budget: BigDecimal,
    restBudget: BigDecimal,
    days: Int,
    currency: ExtendCurrency,
) {
    val textColor = LocalContentColor.current

    Column {
        if (budget > BigDecimal(0) && days > 0) {
            Text(
                text = stringResource(R.string.total_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 56.dp, end = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.per_day,
                    prettyCandyCanes(
                        if (days != 0) {
                            (budget / days.toBigDecimal()).setScale(0, RoundingMode.FLOOR)
                        } else {
                            budget
                        },
                        currency,
                    ),
                ),
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 56.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                    ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Box(
                        modifier = Modifier.heightIn(24.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = stringResource(id = R.string.unable_calc_budget),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    if (budget <= BigDecimal(0)) {
                        Text(
                            text = "- " + stringResource(id = R.string.budget_must_greater_zero),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.6f),
                        )
                    }
                    if (days <= 0) {
                        Text(
                            text = "- " + stringResource(id = R.string.days_must_greater_zero),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewWallet() {
    BuckwheatTheme {
        Wallet()
    }
}