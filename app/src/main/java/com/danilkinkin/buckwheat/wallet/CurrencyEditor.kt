package com.danilkinkin.buckwheat.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.titleCase
import java.util.Currency
import java.util.Locale

const val CURRENCY_EDITOR = "currencyEditor"

/**
 * Short list shown before the full alphabetical catalogue. The device's own currency is
 * prepended at runtime, so the list starts with something the user almost certainly wants.
 */
private val popularCurrencyCodes = listOf(
    "INR", "USD", "EUR", "GBP", "JPY", "CNY",
    "AUD", "CAD", "CHF", "SGD", "AED", "RUB",
)

@Composable
fun CurrencyEditor(
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    var query by remember { mutableStateOf("") }
    var showCustomEditor by remember { mutableStateOf(false) }

    val allCurrencies = remember { getCurrencies() }
    val selectedCode = if (currency.type === ExtendCurrency.Type.FROM_LIST) currency.value else null

    val (popularCurrencies, otherCurrencies) = remember {
        val byCode = allCurrencies.associateBy { it.currencyCode }
        val localeCurrencyCode = runCatching {
            Currency.getInstance(Locale.getDefault()).currencyCode
        }.getOrNull()

        val orderedCodes = listOfNotNull(localeCurrencyCode) + popularCurrencyCodes
        val popular = orderedCodes
            .distinct()
            .mapNotNull { byCode[it] }
        val popularCodes = popular.map { it.currencyCode }.toSet()

        popular to allCurrencies.filter { it.currencyCode !in popularCodes }
    }

    val filteredCurrencies = remember(query, allCurrencies) {
        val normalizedQuery = query.trim().lowercase()

        if (normalizedQuery.isEmpty()) {
            allCurrencies
        } else {
            allCurrencies.filter { item ->
                item.currencyCode.lowercase().contains(normalizedQuery) ||
                        item.displayName.lowercase().contains(normalizedQuery) ||
                        item.symbol.lowercase().contains(normalizedQuery)
            }
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val popularIndex = popularCurrencies.indexOfFirst { it.currencyCode == selectedCode }

        when {
            popularIndex >= 0 -> listState.scrollToItem(popularIndex + 1)
            // Not in the short list: jump to the "Other currencies" header so the current
            // selection is one scroll away instead of hidden below the whole catalogue.
            selectedCode != null -> listState.scrollToItem(popularCurrencies.size + 1)
        }
    }

    fun applyCurrency(newCurrency: ExtendCurrency) {
        spendsViewModel.changeDisplayCurrency(newCurrency)
        onClose()
    }

    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        // A picker over ~150 currencies deserves most of the screen; a wrap-content sheet would
        // be clipped on smaller devices once the list is long.
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
        ) {
            Text(
                text = stringResource(R.string.select_currency_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )

            CurrencySearchField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
            )

            OptionRow(
                icon = painterResource(R.drawable.ic_edit),
                text = stringResource(R.string.currency_custom),
                caption = if (currency.type === ExtendCurrency.Type.CUSTOM) currency.value else null,
                selected = currency.type === ExtendCurrency.Type.CUSTOM,
                onClick = { showCustomEditor = true },
            )
            OptionRow(
                icon = painterResource(R.drawable.ic_close),
                text = stringResource(R.string.currency_none),
                selected = currency.type === ExtendCurrency.Type.NONE,
                onClick = { applyCurrency(ExtendCurrency.none()) },
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (filteredCurrencies.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.currency_not_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (query.isBlank()) {
                            item("popular-header") {
                                CurrencySectionHeader(stringResource(R.string.popular_currencies))
                            }
                            items(popularCurrencies, key = { it.currencyCode }) { item ->
                                CurrencyRow(
                                    currency = item,
                                    selected = item.currencyCode == selectedCode,
                                    onClick = { applyWorldCurrency(item) },
                                )
                            }
                            item("other-header") {
                                CurrencySectionHeader(stringResource(R.string.other_currencies))
                            }
                            items(otherCurrencies, key = { it.currencyCode }) { item ->
                                CurrencyRow(
                                    currency = item,
                                    selected = item.currencyCode == selectedCode,
                                    onClick = { applyWorldCurrency(item) },
                                )
                            }
                        } else {
                            items(filteredCurrencies, key = { it.currencyCode }) { item ->
                                CurrencyRow(
                                    currency = item,
                                    selected = item.currencyCode == selectedCode,
                                    onClick = { applyWorldCurrency(item) },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(navigationBarHeight))
        }
    }

    if (showCustomEditor) {
        CustomCurrencyEditor(
            defaultCurrency = if (currency.type === ExtendCurrency.Type.CUSTOM) {
                currency.value
            } else {
                null
            },
            onChange = {
                applyCurrency(ExtendCurrency(type = ExtendCurrency.Type.CUSTOM, value = it))
            },
            onClose = { showCustomEditor = false },
        )
    }
}

private fun applyWorldCurrency(item: Currency): ExtendCurrency =
    ExtendCurrency(value = item.currencyCode, type = ExtendCurrency.Type.FROM_LIST)

@Composable
private fun CurrencySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val containerColor = combineColors(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
        0.5f,
    )

    TextField(
        modifier = modifier.padding(horizontal = 24.dp),
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = stringResource(R.string.search_currency_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (value.isEmpty()) return@TextField

            IconButton(onClick = { onValueChange("") }) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

@Composable
private fun CurrencySectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun CurrencyRow(
    currency: Currency,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = currency.symbol,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = currency.displayName.titleCase(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = currency.currencyCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
            )
        }
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_apply),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun OptionRow(
    icon: Painter,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    caption: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (caption !== null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_apply),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** All currencies, sorted by localized name so the list reads naturally in any language. */
private fun getCurrencies(): List<Currency> =
    Currency.getAvailableCurrencies()
        .sortedBy { it.displayName.uppercase() }
