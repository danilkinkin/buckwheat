package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import java.util.*

fun getCurrencies(): MutableList<Currency> {
    val currencies = Currency.getAvailableCurrencies().toMutableList()

    currencies.sortBy { it.displayName.uppercase() }

    return currencies
}

@Composable
fun WorldCurrencyChooserContent(
    defaultCurrency: Currency? = null,
    onSelect: (currency: Currency) -> Unit,
    onClose: () -> Unit,
) {
    val selectCurrency = remember { mutableStateOf(defaultCurrency) }

    Card(
        shape = CardDefaults.shape,
        modifier = Modifier
            .widthIn(max = 500.dp)
            .padding(36.dp)
    ) {
        Column() {
            Text(
                text = stringResource(R.string.select_currency_title),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(24.dp)
            )
            Divider()
            Box(Modifier.weight(1F)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxSize()//.heightIn(max = 600.dp)
                ) {
                    getCurrencies().forEach {
                        itemsCurrency(
                            currency = it,
                            selected = selectCurrency.value?.currencyCode === it.currencyCode,
                            onClick = {
                                selectCurrency.value = it
                            },
                        )
                    }
                }
            }
            Divider()
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
                Button(
                    onClick = { onClose() },
                    colors = ButtonDefaults.textButtonColors(),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onSelect(selectCurrency.value!!)
                        onClose()
                    },
                    colors = ButtonDefaults.textButtonColors(),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    enabled = selectCurrency.value !== null,
                ) {
                    Text(text = stringResource(R.string.accept))
                }
            }
        }
    }
}

private fun LazyListScope.itemsCurrency(
    currency: Currency,
    selected: Boolean,
    onClick: () -> Unit,
) {
    item(currency.currencyCode) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = selected,
                    onValueChange = { onClick() },
                    role = Role.Checkbox
                )
                .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Text(text = currency.displayName)
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WorldCurrencyChooser(
    defaultCurrency: Currency? = null,
    onSelect: (currency: Currency) -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        WorldCurrencyChooserContent(
            defaultCurrency = defaultCurrency,
            onSelect = onSelect,
            onClose = { onClose() }
        )
    }
}

@Preview
@Composable
fun PreviewWorldCurrencyChooser() {
    BuckwheatTheme {
        WorldCurrencyChooserContent(
            defaultCurrency = null,
            onSelect = { },
            onClose = { }
        )
    }
}