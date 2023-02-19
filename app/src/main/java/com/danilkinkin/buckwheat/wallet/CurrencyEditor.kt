package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.CurrencyType
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.titleCase
import java.util.*

const val CURRENCY_EDITOR = "currencyEditor"

@Composable
fun CurrencyEditor(
    windowSizeClass: WindowWidthSizeClass,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    var currency by remember { mutableStateOf(spendsViewModel.currency.value!!) }
    val openCurrencyChooserDialog = remember { mutableStateOf(false) }
    val openCustomCurrencyEditorDialog = remember { mutableStateOf(false) }

    val navigationBarHeight = WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)

    Surface {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.select_currency_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight)
            ) {
                Text(
                    text = stringResource(R.string.select_currency_description),
                    style = MaterialTheme.typography.bodyMedium
                        .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
                    softWrap = true,
                    modifier = Modifier
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 16.dp,
                        )
                )
                CheckedRow(
                    checked = currency.type === CurrencyType.FROM_LIST,
                    onValueChange = { openCurrencyChooserDialog.value = true },
                    text = stringResource(R.string.currency_from_list),
                    endCaption = if (currency.type === CurrencyType.FROM_LIST) {
                        "${
                            Currency.getInstance(
                                currency.value
                            ).displayName.titleCase()
                        } (${
                            Currency.getInstance(
                                currency.value
                            ).symbol
                        })"
                    } else {
                        ""
                    },
                )
                CheckedRow(
                    checked = currency.type === CurrencyType.CUSTOM,
                    onValueChange = { openCustomCurrencyEditorDialog.value = true },
                    text = stringResource(R.string.currency_custom),
                    endCaption = if (currency.type === CurrencyType.CUSTOM) {
                        currency.value!!
                    } else {
                        ""
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
}