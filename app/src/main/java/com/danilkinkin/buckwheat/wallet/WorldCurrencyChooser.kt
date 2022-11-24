package com.danilkinkin.buckwheat.wallet

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.RenderAdaptivePane
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.titleCase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import java.util.*

fun getCurrencies(): MutableList<Currency> {
    val currencies = Currency.getAvailableCurrencies().toMutableList()

    currencies.sortBy { it.displayName.uppercase() }

    return currencies
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldCurrencyChooserContent(
    defaultCurrency: Currency? = null,
    onSelect: (currency: Currency) -> Unit,
    onClose: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val list = remember { getCurrencies() }
    val selectCurrency = remember { mutableStateOf(defaultCurrency) }
    var searchValue by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (defaultCurrency == null) return@LaunchedEffect

        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            val index = list.indexOfFirst { it.currencyCode == defaultCurrency.currencyCode }

            scrollState.scrollToItem(index)
        }
    }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .widthIn(max = 500.dp)
            .padding(36.dp)
            .imePadding()
    ) {
        Column {
            Text(
                text = stringResource(R.string.select_currency_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(24.dp)
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchValue,
                onValueChange = {
                    searchValue = it
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_currency_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.padding(start = 24.dp, end = 8.dp),
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (searchValue.isEmpty()) return@TextField

                   IconButton(
                       modifier = Modifier.padding(end = 8.dp),
                       onClick = { searchValue = "" },
                   ) {
                       Icon(
                           painter = painterResource(R.drawable.ic_close),
                           contentDescription = null,
                       )
                   }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W600),
                singleLine = true,
                shape = RectangleShape,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = combineColors(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant,
                        0.5f,
                    ),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
            )

            val filteredList = list
                .filter {
                    it.currencyCode.lowercase().contains(searchValue.lowercase())
                            || it.displayName.lowercase().contains(searchValue.lowercase())
                }

            if (filteredList.isNotEmpty()) {
                Box(Modifier.weight(1F)) {
                    LazyColumn(
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        filteredList.forEach {
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
            } else {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.currency_not_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(alpha = 0.8f),
                    )
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
            Text(
                text = currency.displayName.titleCase(),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.widthIn(8.dp))
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WorldCurrencyChooser(
    windowSizeClass: WindowWidthSizeClass,
    defaultCurrency: Currency? = null,
    onSelect: (currency: Currency) -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        )
    ) {
        RenderAdaptivePane(windowSizeClass) {
            WorldCurrencyChooserContent(
                defaultCurrency = defaultCurrency,
                onSelect = onSelect,
                onClose = { onClose() }
            )
        }
    }
}

@Preview(name = "default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        WorldCurrencyChooserContent(
            defaultCurrency = null,
            onSelect = { },
            onClose = { }
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        WorldCurrencyChooserContent(
            defaultCurrency = null,
            onSelect = { },
            onClose = { }
        )
    }
}