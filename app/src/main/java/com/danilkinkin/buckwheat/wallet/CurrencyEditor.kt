package com.danilkinkin.buckwheat.wallet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

private enum class CurrencyScreen { HOME, ALL }

/**
 * Short list shown first. The device's own currency is prepended at runtime, so the first chip
 * is almost always the right one.
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
    var screen by remember { mutableStateOf(CurrencyScreen.HOME) }
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

    fun applyCurrency(newCurrency: ExtendCurrency) {
        spendsViewModel.changeDisplayCurrency(newCurrency)
        onClose()
    }

    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
        ) {
            AnimatedContent(
                targetState = screen,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val forward = targetState == CurrencyScreen.ALL
                    val enter = if (forward) {
                        slideInHorizontally(tween(220)) { width -> width } + fadeIn(tween(220))
                    } else {
                        slideInHorizontally(tween(220)) { width -> -width } + fadeIn(tween(220))
                    }
                    val exit = if (forward) {
                        slideOutHorizontally(tween(220)) { width -> -width } + fadeOut(tween(220))
                    } else {
                        slideOutHorizontally(tween(220)) { width -> width } + fadeOut(tween(220))
                    }

                    enter togetherWith exit
                },
                label = "currencyScreen",
            ) { currentScreen ->
                when (currentScreen) {
                    CurrencyScreen.HOME -> CurrencyHomeScreen(
                        currency = currency,
                        selectedCode = selectedCode,
                        popularCurrencies = popularCurrencies,
                        onSelect = { applyCurrency(it) },
                        onOpenAll = { screen = CurrencyScreen.ALL },
                        onOpenCustom = { showCustomEditor = true },
                        onClose = onClose,
                        navigationBarHeight = navigationBarHeight,
                    )

                    CurrencyScreen.ALL -> CurrencyAllScreen(
                        selectedCode = selectedCode,
                        query = query,
                        onQueryChange = { query = it },
                        popularCurrencies = popularCurrencies,
                        otherCurrencies = otherCurrencies,
                        filteredCurrencies = filteredCurrencies,
                        onSelect = { applyCurrency(it) },
                        onBack = { screen = CurrencyScreen.HOME },
                        onClose = onClose,
                        navigationBarHeight = navigationBarHeight,
                    )
                }
            }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurrencyHomeScreen(
    currency: ExtendCurrency,
    selectedCode: String?,
    popularCurrencies: List<Currency>,
    onSelect: (ExtendCurrency) -> Unit,
    onOpenAll: () -> Unit,
    onOpenCustom: () -> Unit,
    onClose: () -> Unit,
    navigationBarHeight: Dp,
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.select_currency_title),
            onClose = onClose,
        )

        Text(
            text = stringResource(R.string.currency_picker_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        )

        CurrencySectionHeader(stringResource(R.string.popular_currencies))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            popularCurrencies.forEach { item ->
                CurrencyChip(
                    currency = item,
                    selected = item.currencyCode == selectedCode,
                    onClick = { onSelect(item.toExtendCurrency()) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Column {
                NavigationRow(
                    icon = painterResource(R.drawable.ic_currency),
                    text = stringResource(R.string.other_currencies),
                    caption = selectedCode?.takeIf { code ->
                        popularCurrencies.none { it.currencyCode == code }
                    },
                    onClick = onOpenAll,
                )
                NavigationRow(
                    icon = painterResource(R.drawable.ic_edit),
                    text = stringResource(R.string.currency_custom),
                    caption = if (currency.type === ExtendCurrency.Type.CUSTOM) {
                        currency.value
                    } else {
                        null
                    },
                    onClick = onOpenCustom,
                )
                ToggleRow(
                    icon = painterResource(R.drawable.ic_close),
                    text = stringResource(R.string.currency_none),
                    selected = currency.type === ExtendCurrency.Type.NONE,
                    onClick = { onSelect(ExtendCurrency.none()) },
                )
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(navigationBarHeight))
    }
}

@Composable
private fun CurrencyAllScreen(
    selectedCode: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    popularCurrencies: List<Currency>,
    otherCurrencies: List<Currency>,
    filteredCurrencies: List<Currency>,
    onSelect: (ExtendCurrency) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    navigationBarHeight: Dp,
) {
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

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.all_currencies),
            onBack = onBack,
            onClose = onClose,
        )

        CurrencySearchField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

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
                                onClick = { onSelect(item.toExtendCurrency()) },
                            )
                        }
                        item("other-header") {
                            CurrencySectionHeader(stringResource(R.string.other_currencies))
                        }
                        items(otherCurrencies, key = { it.currencyCode }) { item ->
                            CurrencyRow(
                                currency = item,
                                selected = item.currencyCode == selectedCode,
                                onClick = { onSelect(item.toExtendCurrency()) },
                            )
                        }
                    } else {
                        items(filteredCurrencies, key = { it.currencyCode }) { item ->
                            CurrencyRow(
                                currency = item,
                                selected = item.currencyCode == selectedCode,
                                onClick = { onSelect(item.toExtendCurrency()) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(navigationBarHeight))
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack !== null) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                )
            }
        } else {
            // Keeps the title optically centred against the close button.
            Spacer(Modifier.width(48.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun CurrencyChip(
    currency: Currency,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Some currencies have no distinct symbol (CHF is just "CHF"); don't repeat it.
            if (currency.symbol != currency.currencyCode) {
                Text(
                    text = currency.symbol,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    softWrap = false,
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = currency.currencyCode,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NavigationRow(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
    caption: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (caption !== null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    icon: Painter,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
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
        modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 8.dp),
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

private fun Currency.toExtendCurrency(): ExtendCurrency =
    ExtendCurrency(value = currencyCode, type = ExtendCurrency.Type.FROM_LIST)

/** All currencies, sorted by localized name so the list reads naturally in any language. */
private fun getCurrencies(): List<Currency> =
    Currency.getAvailableCurrencies()
        .sortedBy { it.displayName.uppercase() }
