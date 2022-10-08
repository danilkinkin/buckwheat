package com.danilkinkin.buckwheat.wallet

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Wallet(
    forceChange: Boolean = false,
    requestFinishDate: ((presetDate: Date, callback: (finishDate: Date) -> Unit) -> Unit) = { _: Date, _: (finishDate: Date) -> Unit -> },
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    var rawBudget by remember {
        val converted = if (spendsViewModel.budget.value !== BigDecimal(0)) {
            tryConvertStringToNumber(spendsViewModel.budget.value!!.toString())
        } else {
            Triple("", "0", "")
        }
        
        mutableStateOf(converted.first + converted.second)
    }
    var budget by remember { mutableStateOf(spendsViewModel.budget.value!!) }
    val dateToValue = remember { mutableStateOf(spendsViewModel.finishDate) }
    val currency = remember { mutableStateOf(spendsViewModel.currency) }
    val spends by spendsViewModel.getSpends().observeAsState(initial = emptyList())

    val openCurrencyChooserDialog = remember { mutableStateOf(false) }
    val openCustomCurrencyEditorDialog = remember { mutableStateOf(false) }
    val openConfirmChangeBudgetDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val navigationBarHeight = WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)

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
                    text = stringResource(R.string.wallet_title),
                    style = MaterialTheme.typography.titleLarge,
                )
           }
            Divider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                BasicTextField(
                    value = rawBudget,
                    onValueChange = {
                        val converted = tryConvertStringToNumber(it)

                        rawBudget = converted.join(third = false)
                        budget = converted.join().toBigDecimal()
                    },
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    visualTransformation = visualTransformationAsCurrency(
                        currency = ExtendCurrency(type = CurrencyType.NONE),
                        hintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { input ->
                        Column {
                            TextRow(
                                icon = painterResource(R.drawable.ic_money),
                                text = stringResource(R.string.label_budget),
                            )
                            Box(Modifier.padding(start = 56.dp, bottom = 12.dp)) {
                                input()
                            }
                        }
                    },
                )
                Divider()
                ButtonRow(
                    icon = painterResource(R.drawable.ic_calendar),
                    text = if (days > 0) {
                        String.format(
                            pluralStringResource(R.plurals.finish_date_label, 32),
                            prettyDate(dateToValue.value, showTime = false, forceShowDate = true),
                            days,
                        )
                    } else {
                        stringResource(R.string.finish_date_not_select)
                    },
                    onClick = {
                        coroutineScope.launch {
                            requestFinishDate(dateToValue.value) {
                                dateToValue.value = it
                            }
                        }
                    },
                )
                Divider()
                TextRow(
                    icon = painterResource(R.drawable.ic_currency),
                    text = stringResource(R.string.in_currency_label),
                )
                CheckedRow(
                    checked = currency.value.type === CurrencyType.FROM_LIST,
                    onValueChange = { openCurrencyChooserDialog.value = true },
                    text = if (currency.value.type !== CurrencyType.FROM_LIST) {
                        stringResource(R.string.currency_from_list)
                    } else {
                        stringResource(
                            id = R.string.currency_from_list_selected,
                            Currency.getInstance(currency.value.value).symbol
                        )
                    },
                )
                CheckedRow(
                    checked = currency.value.type === CurrencyType.CUSTOM,
                    onValueChange = { openCustomCurrencyEditorDialog.value = true },
                    text = if (currency.value.type !== CurrencyType.CUSTOM) {
                        stringResource(R.string.currency_custom)
                    } else {
                        stringResource(
                            id = R.string.currency_custom_selected,
                            currency.value.value!!
                        )
                    },
                )
                CheckedRow(
                    checked = currency.value.type === CurrencyType.NONE,
                    onValueChange = {
                        if (it) {
                            currency.value = ExtendCurrency(type = CurrencyType.NONE)
                        }
                    },
                    text = stringResource(R.string.currency_none),
                )
                Divider()
                Spacer(Modifier.height(24.dp))
                Total(
                    budget = budget,
                    days = days,
                    currency = currency.value,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (spends.isNotEmpty() && !forceChange) {
                            openConfirmChangeBudgetDialog.value = true
                        } else {
                            spendsViewModel.changeCurrency(currency.value)
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
                        text = if (spends.isNotEmpty() && !forceChange) {
                            stringResource(R.string.change_budget)
                        } else {
                            stringResource(R.string.apply)
                        },
                    )
                }
            }
        }
    }

    if (openCurrencyChooserDialog.value) {
        WorldCurrencyChooser(
            defaultCurrency = if (currency.value.type === CurrencyType.FROM_LIST) {
                Currency.getInstance(currency.value.value)
            } else {
                null
            },
            onSelect = {
                currency.value =
                    ExtendCurrency(type = CurrencyType.FROM_LIST, value = it.currencyCode)
            },
            onClose = { openCurrencyChooserDialog.value = false },
        )
    }

    if (openCustomCurrencyEditorDialog.value) {
        CustomCurrencyEditor(
            defaultCurrency = if (currency.value.type === CurrencyType.CUSTOM) {
                currency.value.value
            } else {
                null
            },
            onChange = {
                currency.value = ExtendCurrency(type = CurrencyType.CUSTOM, value = it)
            },
            onClose = { openCustomCurrencyEditorDialog.value = false },
        )
    }

    if (openConfirmChangeBudgetDialog.value) {
        ConfirmChangeBudgetDialog(
            onConfirm = {
                spendsViewModel.changeCurrency(currency.value)
                spendsViewModel.changeBudget(budget, dateToValue.value)

                onClose()
            },
            onClose = { openConfirmChangeBudgetDialog.value = false },
        )
    }
}

@Composable
fun Total(budget: BigDecimal, days: Int, currency: ExtendCurrency) {
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