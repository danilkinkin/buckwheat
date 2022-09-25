package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun Wallet(
    requestFinishDate: ((presetDate: Date, callback: (finishDate: Date) -> Unit) -> Unit) = { _: Date, _: (finishDate: Date) -> Unit -> },
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val budget = remember { mutableStateOf(spendsViewModel.budget.value!!) }
    val dateToValue = remember { mutableStateOf(spendsViewModel.finishDate) }
    val currency = remember { mutableStateOf(spendsViewModel.currency) }

    val openCurrencyChooserDialog = remember { mutableStateOf(false) }
    val openCustomCurrencyEditorDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

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
                TextRow(
                    icon = painterResource(R.drawable.ic_money),
                    text = stringResource(R.string.label_budget),
                )
                TextField(
                    modifier = Modifier
                        .padding(start = 56.dp)
                        .fillMaxWidth(),
                    value = budget.value.toString(),
                    onValueChange = {
                        try {
                            budget.value = BigDecimal(it)
                        } catch (E: Exception) {
                            budget.value = BigDecimal(0)
                        }
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.displaySmall,
                    visualTransformation = visualTransformationAsCurrency(
                        currency = ExtendCurrency(type = CurrencyType.NONE)
                    )
                )
                Divider()
                ButtonRow(
                    icon = painterResource(R.drawable.ic_calendar),
                    text = String.format(
                        pluralStringResource(R.plurals.finish_date_label, 32),
                        prettyDate(dateToValue.value, showTime = false, forceShowDate = true),
                        days,
                    ),
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
                Text(
                    text = stringResource(
                        R.string.per_day,
                        prettyCandyCanes(
                            if (days != 0) {
                                (budget.value / days.toBigDecimal()).setScale(0, RoundingMode.FLOOR)
                            } else {
                                budget.value
                            },
                            currency = currency.value,
                        ),
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 56.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        spendsViewModel.changeCurrency(currency.value)
                        spendsViewModel.changeBudget(budget.value, dateToValue.value)

                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = countDays(dateToValue.value) > 0 && budget.value > BigDecimal(0)
                ) {
                    Text(text = stringResource(id = R.string.apply))
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
}

@Preview
@Composable
fun PreviewWallet() {
    BuckwheatTheme {
        Wallet()
    }
}